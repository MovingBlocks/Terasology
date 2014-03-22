/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.math.Vector3i;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.world.chunks.deflate.TeraDeflator;
import org.terasology.world.chunks.deflate.TeraStandardDeflator;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by its dimensions. They are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 * <p/>
 * Chunks are tessellated on creation and saved to vertex arrays. From those VBOs are generated
 * which are then used for the actual rendering process.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public class ChunkImpl implements Chunk {

    private static final Logger logger = LoggerFactory.getLogger(ChunkImpl.class);

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,###");

    private final Vector3i chunkPos = new Vector3i();

    private BlockManager blockManager;

    private TeraArray blockData;
    private TeraArray sunlightData;
    private TeraArray sunlightRegenData;
    private TeraArray lightData;
    private TeraArray extraData;

    private AABB aabb;
    private Region3i region;

    private ReentrantLock lock = new ReentrantLock();

    private boolean disposed;
    private boolean ready;
    private boolean dirty;
    private boolean animated;

    // Rendering
    private ChunkMesh[] activeMesh;
    private ChunkMesh[] pendingMesh;
    private AABB[] subMeshAABB;

    public ChunkImpl(int x, int y, int z) {
        this(new Vector3i(x, y, z));
    }

    public ChunkImpl(Vector3i chunkPos) {
        this(chunkPos, new TeraDenseArray16Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z),
                new TeraDenseArray8Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z));
    }

    public ChunkImpl(Vector3i chunkPos, TeraArray blocks, TeraArray liquid) {
        this.chunkPos.set(Preconditions.checkNotNull(chunkPos));
        this.blockData = Preconditions.checkNotNull(blocks);
        this.extraData = Preconditions.checkNotNull(liquid);
        sunlightData = new TeraDenseArray8Bit(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        sunlightRegenData = new TeraDenseArray8Bit(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        lightData = new TeraDenseArray8Bit(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        dirty = true;
        blockManager = CoreRegistry.get(BlockManager.class);
        region = Region3i.createFromMinAndSize(new Vector3i(chunkPos.x * ChunkConstants.SIZE_X, chunkPos.y * ChunkConstants.SIZE_Y, chunkPos.z * ChunkConstants.SIZE_Z),
                ChunkConstants.CHUNK_SIZE);
        ChunkMonitor.fireChunkCreated(this);
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
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
        lock();
        try {
            this.dirty = dirty;
        } finally {
            unlock();
        }
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        return blockData.getEstimatedMemoryConsumptionInBytes()
                + sunlightData.getEstimatedMemoryConsumptionInBytes()
                + sunlightRegenData.getEstimatedMemoryConsumptionInBytes()
                + lightData.getEstimatedMemoryConsumptionInBytes()
                + extraData.getEstimatedMemoryConsumptionInBytes();
    }

    @Override
    public final Block getBlock(Vector3i pos) {
        short id = (short) blockData.get(pos.x, pos.y, pos.z);
        return blockManager.getBlock(id);
    }

    @Override
    public final Block getBlock(int x, int y, int z) {
        short id = (short) blockData.get(x, y, z);
        return blockManager.getBlock(id);
    }

    @Override
    public Block setBlock(int x, int y, int z, Block block) {
        int oldValue = blockData.set(x, y, z, block.getId());
        if (oldValue != block.getId()) {
            if (!block.isLiquid()) {
                setLiquid(x, y, z, new LiquidData());
            }
        }
        return blockManager.getBlock((short) oldValue);
    }

    @Override
    public Block setBlock(Vector3i pos, Block block) {
        return setBlock(pos.x, pos.y, pos.z, block);
    }

    @Override
    public byte getSunlight(Vector3i pos) {
        return getSunlight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        return (byte) sunlightData.get(x, y, z);
    }

    @Override
    public boolean setSunlight(Vector3i pos, byte amount) {
        return setSunlight(pos.x, pos.y, pos.z, amount);
    }

    @Override
    public boolean setSunlight(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= ChunkConstants.MAX_SUNLIGHT);
        return sunlightData.set(x, y, z, amount) != amount;
    }

    @Override
    public byte getSunlightRegen(Vector3i pos) {
        return getSunlightRegen(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getSunlightRegen(int x, int y, int z) {
        return (byte) sunlightRegenData.get(x, y, z);
    }

    @Override
    public boolean setSunlightRegen(Vector3i pos, byte amount) {
        return setSunlightRegen(pos.x, pos.y, pos.z, amount);
    }

    @Override
    public boolean setSunlightRegen(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= ChunkConstants.MAX_SUNLIGHT_REGEN);
        return sunlightRegenData.set(x, y, z, amount) != amount;
    }

    @Override
    public byte getLight(Vector3i pos) {
        return getLight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getLight(int x, int y, int z) {
        return (byte) lightData.get(x, y, z);
    }

    @Override
    public boolean setLight(Vector3i pos, byte amount) {
        return setLight(pos.x, pos.y, pos.z, amount);
    }

    @Override
    public boolean setLight(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= ChunkConstants.MAX_LIGHT);
        return lightData.set(x, y, z, amount) != amount;
    }

    @Override
    public void setLiquid(Vector3i pos, LiquidData state) {
        setLiquid(pos.x, pos.y, pos.z, state);
    }

    @Override
    public void setLiquid(int x, int y, int z, LiquidData newState) {
        byte newValue = newState.toByte();
        extraData.set(x, y, z, newValue);
    }

    @Override
    public LiquidData getLiquid(Vector3i pos) {
        return getLiquid(pos.x, pos.y, pos.z);
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        return new LiquidData((byte) extraData.get(x, y, z));
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
    public Vector3i chunkToWorldPosition(Vector3i blockPos) {
        return chunkToWorldPosition(blockPos.x, blockPos.y, blockPos.z);
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
            int liquidSize = extraData.getEstimatedMemoryConsumptionInBytes();
            int totalSize = blocksSize + sunlightRegenSize + sunlightSize + lightSize + liquidSize;

            blockData = def.deflate(blockData);
            lightData = def.deflate(lightData);
            extraData = def.deflate(extraData);

            int blocksReduced = blockData.getEstimatedMemoryConsumptionInBytes();
            int lightReduced = lightData.getEstimatedMemoryConsumptionInBytes();
            int liquidReduced = extraData.getEstimatedMemoryConsumptionInBytes();
            int totalReduced = blocksReduced + sunlightRegenSize + sunlightSize + lightReduced + liquidReduced;

            double blocksPercent = 100d - (100d / blocksSize * blocksReduced);
            double lightPercent = 100d - (100d / lightSize * lightReduced);
            double liquidPercent = 100d - (100d / liquidSize * liquidReduced);
            double totalPercent = 100d - (100d / totalSize * totalReduced);

            logger.debug("chunk {}: " +
                    "size-before: {} " +
                    "bytes, size-after: {} " +
                    "bytes, total-deflated-by: {}%, " +
                    "blocks-deflated-by={}%, " +
                    "light-deflated-by={}%, " +
                    "liquid-deflated-by={}%",
                    chunkPos,
                    SIZE_FORMAT.format(totalSize),
                    SIZE_FORMAT.format(totalReduced),
                    PERCENT_FORMAT.format(totalPercent),
                    PERCENT_FORMAT.format(blocksPercent),
                    PERCENT_FORMAT.format(lightPercent),
                    PERCENT_FORMAT.format(liquidPercent));
            ChunkMonitor.fireChunkDeflated(this, totalSize, totalReduced);
        } else {
            final int oldSize = getEstimatedMemoryConsumptionInBytes();
            blockData = def.deflate(blockData);
            lightData = def.deflate(lightData);
            extraData = def.deflate(extraData);
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
            int liquidSize = extraData.getEstimatedMemoryConsumptionInBytes();
            int totalSize = blocksSize + sunlightRegenSize + sunlightSize + lightSize + liquidSize;

            sunlightData = def.deflate(sunlightData);
            sunlightRegenData = def.deflate(sunlightRegenData);

            int sunlightReduced = sunlightData.getEstimatedMemoryConsumptionInBytes();
            int sunlightRegenReduced = sunlightRegenData.getEstimatedMemoryConsumptionInBytes();
            int totalReduced = blocksSize + sunlightRegenReduced + sunlightReduced + lightSize + liquidSize;

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
    public void setMesh(ChunkMesh[] mesh) {
        this.activeMesh = mesh;
    }

    @Override
    public void setPendingMesh(ChunkMesh[] mesh) {
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
    public ChunkMesh[] getMesh() {
        return activeMesh;
    }

    @Override
    public ChunkMesh[] getPendingMesh() {
        return pendingMesh;
    }

    @Override
    public AABB getSubMeshAABB(int subMesh) {
        if (subMeshAABB == null) {
            subMeshAABB = new AABB[ChunkConstants.VERTICAL_SEGMENTS];

            int heightHalf = ChunkConstants.SIZE_Y / ChunkConstants.VERTICAL_SEGMENTS / 2;

            for (int i = 0; i < subMeshAABB.length; i++) {
                Vector3f dimensions = new Vector3f(8, heightHalf, 8);
                Vector3f position = new Vector3f(getChunkWorldOffsetX() - 0.5f, (i * heightHalf * 2) - 0.5f, getChunkWorldOffsetZ() - 0.5f);
                position.add(dimensions);
                subMeshAABB[i] = AABB.createCenterExtent(position, dimensions);
            }
        }

        return subMeshAABB[subMesh];
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
        if (activeMesh != null) {
            for (ChunkMesh chunkMesh : activeMesh) {
                chunkMesh.dispose();
            }
            activeMesh = null;
        }
        lightData = null;
        sunlightData = null;
        sunlightRegenData = null;
        ChunkMonitor.fireChunkDisposed(this);
    }

    @Override
    public void disposeMesh() {
        if (activeMesh != null) {
            for (ChunkMesh chunkMesh : activeMesh) {
                chunkMesh.dispose();
            }
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
        return ChunkSerializer.encode(chunkPos, blockData, extraData);
    }
}
