/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.AABB;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.world.chunks.deflate.TeraDeflator;
import org.terasology.world.chunks.deflate.TeraStandardDeflator;

import java.text.DecimalFormat;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by its dimensions. They are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 * <br><br>
 * Chunks are tessellated on creation and saved to vertex arrays. From those VBOs are generated
 * which are then used for the actual rendering process.
 */
public class ChunkImpl implements Chunk {

    private static final Logger logger = LoggerFactory.getLogger(ChunkImpl.class);

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,###");

    private final Vector3i chunkPos = new Vector3i();

    private BlockManager blockManager;
    private BiomeManager biomeManager;

    private TeraArray sunlightData;
    private TeraArray sunlightRegenData;
    private TeraArray lightData;

    private TeraArray blockData;
    private volatile TeraArray blockDataSnapshot;
    private TeraArray biomeData;
    private volatile TeraArray biomeDataSnapshot;
    private TeraArray[] extraData;
    private volatile TeraArray[] extraDataSnapshots;

    private AABB aabb;
    private Region3i region;

    private boolean disposed;
    private boolean ready;
    private volatile boolean dirty;
    private boolean animated;

    // Rendering
    private ChunkMesh activeMesh;
    private ChunkMesh pendingMesh;
    private boolean adjacentChunksReady;

    public ChunkImpl(int x, int y, int z, BlockManager blockManager, BiomeManager biomeManager, ExtraBlockDataManager extraDataManager) {
        this(new Vector3i(x, y, z), blockManager, biomeManager, extraDataManager);
    }

    public ChunkImpl(Vector3i chunkPos, BlockManager blockManager, BiomeManager biomeManager, ExtraBlockDataManager extraDataManager) {
        this(chunkPos, new TeraDenseArray16Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z),
                new TeraDenseArray8Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z),
                extraDataManager.makeDataArrays(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z),
                blockManager, biomeManager);
    }

    public ChunkImpl(Vector3i chunkPos, TeraArray blocks, TeraArray biome, TeraArray[] extra, BlockManager blockManager,
                     BiomeManager biomeManager) {
        this.chunkPos.set(Preconditions.checkNotNull(chunkPos));
        this.blockData = Preconditions.checkNotNull(blocks);
        this.biomeData = Preconditions.checkNotNull(biome);
        this.extraData = Preconditions.checkNotNull(extra);
        sunlightData = new TeraDenseArray8Bit(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        sunlightRegenData = new TeraDenseArray8Bit(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        lightData = new TeraDenseArray8Bit(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        dirty = true;
        this.blockManager = blockManager;
        this.biomeManager = biomeManager;
        region = Region3i.createFromMinAndSize(new Vector3i(chunkPos.x * ChunkConstants.SIZE_X, chunkPos.y * ChunkConstants.SIZE_Y, chunkPos.z * ChunkConstants.SIZE_Z),
                ChunkConstants.CHUNK_SIZE);
        ChunkMonitor.fireChunkCreated(this);
    }

    @Override
    public Vector3i getPosition() {
        return new Vector3i(chunkPos);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        int extraDataSize = 0;
        for (int i = 0; i < extraData.length; i++) {
            extraDataSize += extraData[i].getEstimatedMemoryConsumptionInBytes();
        }
        return blockData.getEstimatedMemoryConsumptionInBytes()
                + sunlightData.getEstimatedMemoryConsumptionInBytes()
                + sunlightRegenData.getEstimatedMemoryConsumptionInBytes()
                + lightData.getEstimatedMemoryConsumptionInBytes()
                + biomeData.getEstimatedMemoryConsumptionInBytes()
                + extraDataSize;
    }

    @Override
    public final Block getBlock(BaseVector3i pos) {
        short id = (short) blockData.get(pos.x(), pos.y(), pos.z());
        return blockManager.getBlock(id);
    }

    @Override
    public final Block getBlock(int x, int y, int z) {
        short id = (short) blockData.get(x, y, z);
        return blockManager.getBlock(id);
    }

    // This could be made to check for and clear extraData fields as appropriate,
    // but that could take an excessive amount of time,
    // so whatever sets a block to something extraData sensitive should also initialise the extra data.
    @Override
    public Block setBlock(int x, int y, int z, Block block) {
        if (blockData == blockDataSnapshot) {
            blockData = blockData.copy();
        }
        int oldValue = blockData.set(x, y, z, block.getId());
        return blockManager.getBlock((short) oldValue);
    }

    @Override
    public Block setBlock(BaseVector3i pos, Block block) {
        return setBlock(pos.x(), pos.y(), pos.z(), block);
    }

    @Override
    public byte getSunlight(BaseVector3i pos) {
        return getSunlight(pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        return (byte) sunlightData.get(x, y, z);
    }

    @Override
    public boolean setSunlight(BaseVector3i pos, byte amount) {
        return setSunlight(pos.x(), pos.y(), pos.z(), amount);
    }

    @Override
    public boolean setSunlight(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= ChunkConstants.MAX_SUNLIGHT);
        return sunlightData.set(x, y, z, amount) != amount;
    }

    @Override
    public byte getSunlightRegen(BaseVector3i pos) {
        return getSunlightRegen(pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getSunlightRegen(int x, int y, int z) {
        return (byte) sunlightRegenData.get(x, y, z);
    }

    @Override
    public boolean setSunlightRegen(BaseVector3i pos, byte amount) {
        return setSunlightRegen(pos.x(), pos.y(), pos.z(), amount);
    }

    @Override
    public boolean setSunlightRegen(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= ChunkConstants.MAX_SUNLIGHT_REGEN);
        return sunlightRegenData.set(x, y, z, amount) != amount;
    }

    @Override
    public byte getLight(BaseVector3i pos) {
        return getLight(pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getLight(int x, int y, int z) {
        return (byte) lightData.get(x, y, z);
    }

    @Override
    public boolean setLight(BaseVector3i pos, byte amount) {
        return setLight(pos.x(), pos.y(), pos.z(), amount);
    }

    @Override
    public boolean setLight(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= ChunkConstants.MAX_LIGHT);
        return lightData.set(x, y, z, amount) != amount;
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return biomeManager.getBiomeByShortId((short) biomeData.get(x, y, z));
    }

    @Override
    public Biome getBiome(BaseVector3i pos) {
        return getBiome(pos.x(), pos.y(), pos.z());
    }

    @Override
    public Biome setBiome(int x, int y, int z, Biome biome) {
        if (biomeData == biomeDataSnapshot) {
            biomeData = biomeData.copy();
        }
        short shortId = biomeManager.getBiomeShortId(biome);
        short previousShortId = (short) biomeData.set(x, y, z, shortId);
        return biomeManager.getBiomeByShortId(previousShortId);
    }

    @Override
    public Biome setBiome(BaseVector3i pos, Biome biome) {
        return setBiome(pos.x(), pos.y(), pos.z(), biome);
    }
    
    @Override
    public int getExtraData(int index, int x, int y, int z) {
        return extraData[index].get(x, y, z);
    }
    
    @Override
    public int getExtraData(int index, BaseVector3i pos) {
        return getExtraData(index, pos.x(), pos.y(), pos.z());
    }
    
    @Override
    public void setExtraData(int index, int x, int y, int z, int value) {
        if (extraDataSnapshots != null && extraData[index] == extraDataSnapshots[index]) {
            extraData[index] = extraData[index].copy();
        }
        extraData[index].set(x, y, z, value);
    }
    
    @Override
    public void setExtraData(int index, BaseVector3i pos, int value) {
        setExtraData(index, pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public Vector3i getChunkWorldOffset() {
        return new Vector3i(getChunkWorldOffsetX(), getChunkWorldOffsetY(), getChunkWorldOffsetZ());
    }

    @Override
    public int getChunkWorldOffsetX() {
        return chunkPos.x * getChunkSizeX();
    }

    @Override
    public int getChunkWorldOffsetY() {
        return chunkPos.y * getChunkSizeY();
    }

    @Override
    public int getChunkWorldOffsetZ() {
        return chunkPos.z * getChunkSizeZ();
    }

    @Override
    public Vector3i chunkToWorldPosition(BaseVector3i blockPos) {
        return chunkToWorldPosition(blockPos.x(), blockPos.y(), blockPos.z());
    }

    @Override
    public Vector3i chunkToWorldPosition(int x, int y, int z) {
        return new Vector3i(chunkToWorldPositionX(x), chunkToWorldPositionY(y), chunkToWorldPositionZ(z));
    }

    @Override
    public int chunkToWorldPositionX(int x) {
        return x + getChunkWorldOffsetX();
    }

    @Override
    public int chunkToWorldPositionY(int y) {
        return y + getChunkWorldOffsetY();
    }

    @Override
    public int chunkToWorldPositionZ(int z) {
        return z + getChunkWorldOffsetZ();
    }

    @Override
    public AABB getAABB() {
        if (aabb == null) {
            Vector3f min = getChunkWorldOffset().toVector3f();
            Vector3f max = ChunkConstants.CHUNK_SIZE.toVector3f();
            max.add(min);
            aabb = AABB.createMinMax(min, max);
        }

        return aabb;
    }

    @Override
    public void deflate() {
        final TeraDeflator def = new TeraStandardDeflator();
        if (logger.isDebugEnabled()) {
            int blocksSize = blockData.getEstimatedMemoryConsumptionInBytes();
            int sunlightSize = sunlightData.getEstimatedMemoryConsumptionInBytes();
            int sunlightRegenSize = sunlightRegenData.getEstimatedMemoryConsumptionInBytes();
            int lightSize = lightData.getEstimatedMemoryConsumptionInBytes();
            int biomeSize = biomeData.getEstimatedMemoryConsumptionInBytes();
            int extraSize = 0;
            for (int i = 0; i < extraData.length; i++) {
                extraSize += extraData[i].getEstimatedMemoryConsumptionInBytes();
            }
            int totalSize = blocksSize + sunlightRegenSize + sunlightSize + lightSize + biomeSize + extraSize;

            blockData = def.deflate(blockData);
            lightData = def.deflate(lightData);
            biomeData = def.deflate(biomeData);
            for (int i = 0; i < extraData.length; i++) {
                extraData[i] = def.deflate(extraData[i]);
            }

            int blocksReduced = blockData.getEstimatedMemoryConsumptionInBytes();
            int lightReduced = lightData.getEstimatedMemoryConsumptionInBytes();
            int biomeReduced = biomeData.getEstimatedMemoryConsumptionInBytes();
            int extraReduced = 0;
            for (int i = 0; i < extraData.length; i++) {
                extraReduced += extraData[i].getEstimatedMemoryConsumptionInBytes();
            }
            int totalReduced = blocksReduced + sunlightRegenSize + sunlightSize + lightReduced + biomeReduced + extraReduced;

            double blocksPercent = 100d - (100d / blocksSize * blocksReduced);
            double lightPercent = 100d - (100d / lightSize * lightReduced);
            double biomePercent = 100d - (100d / biomeSize * biomeReduced);
            double extraPercent = 100d - (100d / extraSize * extraReduced);
            double totalPercent = 100d - (100d / totalSize * totalReduced);

            logger.debug("chunk {}: " +
                            "size-before: {} " +
                            "bytes, size-after: {} " +
                            "bytes, total-deflated-by: {}%, " +
                            "blocks-deflated-by={}%, " +
                            "light-deflated-by={}%, " +
                            "biome-deflated-by={}%, " +
                            "extra-data-deflated-by={}%",
                    chunkPos,
                    SIZE_FORMAT.format(totalSize),
                    SIZE_FORMAT.format(totalReduced),
                    PERCENT_FORMAT.format(totalPercent),
                    PERCENT_FORMAT.format(blocksPercent),
                    PERCENT_FORMAT.format(lightPercent),
                    PERCENT_FORMAT.format(biomePercent),
                    PERCENT_FORMAT.format(extraPercent));
            ChunkMonitor.fireChunkDeflated(this, totalSize, totalReduced);
        } else {
            final int oldSize = getEstimatedMemoryConsumptionInBytes();
            blockData = def.deflate(blockData);
            lightData = def.deflate(lightData);
            biomeData = def.deflate(biomeData);
            for (int i = 0; i < extraData.length; i++) {
                extraData[i] = def.deflate(extraData[i]);
            }
            ChunkMonitor.fireChunkDeflated(this, oldSize, getEstimatedMemoryConsumptionInBytes());
        }
    }

    @Override
    public void deflateSunlight() {
        final TeraDeflator def = new TeraStandardDeflator();
        if (logger.isDebugEnabled()) {
            int blocksSize = blockData.getEstimatedMemoryConsumptionInBytes();
            int sunlightSize = sunlightData.getEstimatedMemoryConsumptionInBytes();
            int sunlightRegenSize = sunlightRegenData.getEstimatedMemoryConsumptionInBytes();
            int lightSize = lightData.getEstimatedMemoryConsumptionInBytes();
            int totalSize = blocksSize + sunlightRegenSize + sunlightSize + lightSize;

            sunlightData = def.deflate(sunlightData);
            sunlightRegenData = def.deflate(sunlightRegenData);

            int sunlightReduced = sunlightData.getEstimatedMemoryConsumptionInBytes();
            int sunlightRegenReduced = sunlightRegenData.getEstimatedMemoryConsumptionInBytes();
            int totalReduced = blocksSize + sunlightRegenReduced + sunlightReduced + lightSize;

            double sunlightPercent = 100d - (100d / sunlightSize * sunlightReduced);
            double sunlightRegenPercent = 100d - (100d / sunlightRegenSize * sunlightRegenReduced);
            double totalPercent = 100d - (100d / totalSize * totalReduced);

            logger.debug("chunk {}: " +
                            "size-before: {} " +
                            "bytes, size-after: {} " +
                            "bytes, total-deflated-by: {}%, " +
                            "sunlight-deflated-by={}%, " +
                            "sunlight-regen-deflated-by={}%, " +
                            chunkPos,
                    SIZE_FORMAT.format(totalSize),
                    SIZE_FORMAT.format(totalReduced),
                    PERCENT_FORMAT.format(totalPercent),
                    PERCENT_FORMAT.format(sunlightPercent),
                    PERCENT_FORMAT.format(sunlightRegenPercent));
            ChunkMonitor.fireChunkDeflated(this, totalSize, totalReduced);
        } else {
            final int oldSize = getEstimatedMemoryConsumptionInBytes();
            sunlightData = def.deflate(sunlightData);
            sunlightRegenData = def.deflate(sunlightRegenData);
            ChunkMonitor.fireChunkDeflated(this, oldSize, getEstimatedMemoryConsumptionInBytes());
        }
    }

    @Override
    public String toString() {
        return "Chunk" + chunkPos.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chunkPos);
    }

    @Override
    public boolean equals(Object obj) {
        // According to hashCode() two ChunkImpls are not equal when their
        // position differs. The default equals() compares object instances.
        // The same instance has the same chunkPos, so this is valid.
        return super.equals(obj);
    }

    @Override
    public void setMesh(ChunkMesh mesh) {
        this.activeMesh = mesh;
    }

    @Override
    public void setPendingMesh(ChunkMesh mesh) {
        this.pendingMesh = mesh;
    }

    @Override
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    @Override
    public boolean isAnimated() {
        return animated;
    }

    @Override
    public boolean hasMesh() {
        return activeMesh != null;
    }

    @Override
    public boolean hasPendingMesh() {
        return pendingMesh != null;
    }

    @Override
    public ChunkMesh getMesh() {
        return activeMesh;
    }

    @Override
    public ChunkMesh getPendingMesh() {
        return pendingMesh;
    }

    @Override
    public void markReady() {
        ready = true;
    }

    @Override
    public void prepareForReactivation() {
        if (disposed) {
            disposed = false;
            sunlightData = new TeraDenseArray8Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
            sunlightRegenData = new TeraDenseArray8Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
            lightData = new TeraDenseArray8Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        ready = false;
        disposeMesh();
        /*
         * Explicitly do not clear data, so that background threads that work with the chunk can finish.
         */
        ChunkMonitor.fireChunkDisposed(this);
    }

    @Override
    public void disposeMesh() {
        if (activeMesh != null) {
            activeMesh.dispose();
            activeMesh = null;
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public Region3i getRegion() {
        return region;
    }

    @Override
    public int getChunkSizeX() {
        return ChunkConstants.SIZE_X;
    }

    @Override
    public int getChunkSizeY() {
        return ChunkConstants.SIZE_Y;
    }

    @Override
    public int getChunkSizeZ() {
        return ChunkConstants.SIZE_Z;
    }

    @Override
    public ChunkBlockIterator getBlockIterator() {
        return new ChunkBlockIteratorImpl(blockManager, getChunkWorldOffset(), blockData);
    }

    @Override
    public EntityData.ChunkStore.Builder encode() {
        return ChunkSerializer.encode(chunkPos, blockData, biomeData, extraData);
    }

    /**
     * Calling this method results in a (cheap) snapshot to be taken of the current state of the chunk.
     * This snapshot can then be obtained and rleased by calling {@link #encodeAndReleaseSnapshot()}.
     */
    public void createSnapshot() {
        this.blockDataSnapshot = this.blockData;
        this.biomeDataSnapshot = this.biomeData;
        this.extraDataSnapshots = new TeraArray[extraData.length];
        System.arraycopy(extraData, 0, extraDataSnapshots, 0, extraData.length);
    }

    /**
     * This method can only be
     * called once after {@link #createSnapshot()} has been called. It can be called from a different thread than
     * {@link #createSnapshot()}, but it must be made sure that neither method is still running when the other gets
     * called.
     *
     * @return an encoded version of the snapshot taken with {@link #createSnapshot()}.
     */
    public EntityData.ChunkStore.Builder encodeAndReleaseSnapshot() {
        EntityData.ChunkStore.Builder result = ChunkSerializer.encode(chunkPos, blockDataSnapshot, biomeDataSnapshot, extraDataSnapshots);
        this.blockDataSnapshot = null;
        this.biomeDataSnapshot = null;
        this.extraDataSnapshots = null;
        return result;
    }

}
