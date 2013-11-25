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
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArrays;
import org.terasology.world.chunks.deflate.TeraDeflator;
import org.terasology.world.chunks.deflate.TeraStandardDeflator;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;
import java.text.DecimalFormat;
import java.util.Map;
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


    public static enum State {
        ADJACENCY_GENERATION_PENDING(EntityData.ChunkState.ADJACENCY_GENERATION_PENDING),
        INTERNAL_LIGHT_GENERATION_PENDING(EntityData.ChunkState.INTERNAL_LIGHT_GENERATION_PENDING),
        COMPLETE(EntityData.ChunkState.COMPLETE);

        private static final Map<EntityData.ChunkState, State> LOOKUP;

        private final EntityData.ChunkState protobufState;

        static {
            LOOKUP = Maps.newHashMap();
            for (State s : State.values()) {
                LOOKUP.put(s.protobufState, s);
            }
        }

        private State(EntityData.ChunkState protobufState) {
            this.protobufState = Preconditions.checkNotNull(protobufState);
        }

        public static State lookup(EntityData.ChunkState state) {
            State result = LOOKUP.get(Preconditions.checkNotNull(state, "The parameter 'state' must not be null"));
            if (result == null) {
                return INTERNAL_LIGHT_GENERATION_PENDING;
            }
            return result;
        }
    }


    private final Vector3i chunkPos = new Vector3i();

    private BlockManager blockManager;

    private TeraArray blockData;
    private TeraArray sunlightData;
    private TeraArray lightData;
    private TeraArray extraData;

    private boolean initialGenerationComplete;
    private State chunkState = State.ADJACENCY_GENERATION_PENDING;
    private boolean dirty;
    private boolean animated;
    private AABB aabb;
    private Region3i region;

    // Rendering
    private ChunkMesh[] activeMesh;
    private ChunkMesh[] pendingMesh;
    private AABB[] subMeshAABB;

    private ReentrantLock lock = new ReentrantLock();
    private boolean disposed;

    private boolean ready;

    protected ChunkImpl() {
        final Chunks c = Chunks.getInstance();
        blockData = c.getBlockDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        sunlightData = c.getSunlightDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        lightData = c.getLightDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        extraData = c.getExtraDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        dirty = true;
        blockManager = CoreRegistry.get(BlockManager.class);
    }

    public ChunkImpl(int x, int y, int z) {
        this();
        chunkPos.x = x;
        chunkPos.y = y;
        chunkPos.z = z;
        region = Region3i.createFromMinAndSize(new Vector3i(x * ChunkConstants.SIZE_X, y * ChunkConstants.SIZE_Y, z * ChunkConstants.SIZE_Z), ChunkConstants.CHUNK_SIZE);
        ChunkMonitor.fireChunkCreated(this);
    }

    public ChunkImpl(Vector3i chunkPos) {
        this(chunkPos.x, chunkPos.y, chunkPos.z);
    }

    public ChunkImpl(Vector3i chunkPos, State chunkState, TeraArray blocks, TeraArray sunlight, TeraArray light, TeraArray liquid, boolean loaded) {
        this.chunkPos.set(Preconditions.checkNotNull(chunkPos));
        this.blockData = Preconditions.checkNotNull(blocks);
        this.sunlightData = Preconditions.checkNotNull(sunlight);
        this.lightData = Preconditions.checkNotNull(light);
        this.extraData = Preconditions.checkNotNull(liquid);
        this.chunkState = Preconditions.checkNotNull(chunkState);
        dirty = true;
        blockManager = CoreRegistry.get(BlockManager.class);
        region = Region3i.createFromMinAndSize(new Vector3i(chunkPos.x * ChunkConstants.SIZE_X, chunkPos.y * ChunkConstants.SIZE_Y, chunkPos.z * ChunkConstants.SIZE_Z), ChunkConstants.CHUNK_SIZE);
        initialGenerationComplete = loaded;
        ChunkMonitor.fireChunkCreated(this);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public Vector3i getPos() {
        return new Vector3i(chunkPos);
    }

    public boolean isInBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < getChunkSizeX() && y < getChunkSizeY() && z < getChunkSizeZ();
    }

    public State getChunkState() {
        return chunkState;
    }

    public void setChunkState(State chunkState) {
        Preconditions.checkNotNull(chunkState);
        if (chunkState != this.chunkState) {
            State old = this.chunkState;
            this.chunkState = chunkState;
            ChunkMonitor.fireStateChanged(this, old);
        }

    }

    public boolean isInitialGenerationComplete() {
        return initialGenerationComplete;
    }

    public void setInitialGenerationComplete() {
        initialGenerationComplete = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        lock();
        try {
            this.dirty = dirty;
        } finally {
            unlock();
        }
    }

    public int getEstimatedMemoryConsumptionInBytes() {
        return blockData.getEstimatedMemoryConsumptionInBytes()
                + sunlightData.getEstimatedMemoryConsumptionInBytes()
                + lightData.getEstimatedMemoryConsumptionInBytes()
                + extraData.getEstimatedMemoryConsumptionInBytes();
    }

    @Override
    public Block getBlock(Vector3i pos) {
        return blockManager.getBlock((byte) blockData.get(pos.x, pos.y, pos.z));
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return blockManager.getBlock((byte) blockData.get(x, y, z));
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

    public byte getSunlight(Vector3i pos) {
        return getSunlight(pos.x, pos.y, pos.z);
    }

    public byte getSunlight(int x, int y, int z) {
        return (byte) sunlightData.get(x, y, z);
    }

    public boolean setSunlight(Vector3i pos, byte amount) {
        return setSunlight(pos.x, pos.y, pos.z, amount);
    }

    public boolean setSunlight(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= 15);
        return sunlightData.set(x, y, z, amount) != amount;
    }

    public byte getLight(Vector3i pos) {
        return getLight(pos.x, pos.y, pos.z);
    }

    public byte getLight(int x, int y, int z) {
        return (byte) lightData.get(x, y, z);
    }

    public boolean setLight(Vector3i pos, byte amount) {
        return setLight(pos.x, pos.y, pos.z, amount);
    }

    public boolean setLight(int x, int y, int z, byte amount) {
        Preconditions.checkArgument(amount >= 0 && amount <= 15);
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
    public Vector3i getChunkWorldPos() {
        return new Vector3i(getChunkWorldPosX(), getChunkWorldPosY(), getChunkWorldPosZ());
    }

    @Override
    public int getChunkWorldPosX() {
        return chunkPos.x * getChunkSizeX();
    }

    @Override
    public int getChunkWorldPosY() {
        return chunkPos.y * getChunkSizeY();
    }

    @Override
    public int getChunkWorldPosZ() {
        return chunkPos.z * getChunkSizeZ();
    }

    @Override
    public Vector3i getBlockWorldPos(Vector3i blockPos) {
        return getBlockWorldPos(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public Vector3i getBlockWorldPos(int x, int y, int z) {
        return new Vector3i(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z));
    }

    @Override
    public int getBlockWorldPosX(int x) {
        return x + getChunkWorldPosX();
    }

    @Override
    public int getBlockWorldPosY(int y) {
        return y + getChunkWorldPosY();
    }

    @Override
    public int getBlockWorldPosZ(int z) {
        return z + getChunkWorldPosZ();
    }

    public AABB getAABB() {
        if (aabb == null) {
            Vector3f min = getChunkWorldPos().toVector3f();
            Vector3f max = ChunkConstants.CHUNK_SIZE.toVector3f();
            max.add(min);
            aabb = AABB.createMinMax(min, max);
        }

        return aabb;
    }

    public void deflate() {
        lock();
        try {
            final TeraDeflator def = new TeraStandardDeflator();
            if (logger.isDebugEnabled()) {
                int blocksSize = blockData.getEstimatedMemoryConsumptionInBytes();
                int sunlightSize = sunlightData.getEstimatedMemoryConsumptionInBytes();
                int lightSize = lightData.getEstimatedMemoryConsumptionInBytes();
                int liquidSize = extraData.getEstimatedMemoryConsumptionInBytes();
                int totalSize = blocksSize + sunlightSize + lightSize + liquidSize;

                blockData = def.deflate(blockData);
                sunlightData = def.deflate(sunlightData);
                lightData = def.deflate(lightData);
                extraData = def.deflate(extraData);

                int blocksReduced = blockData.getEstimatedMemoryConsumptionInBytes();
                int sunlightReduced = sunlightData.getEstimatedMemoryConsumptionInBytes();
                int lightReduced = lightData.getEstimatedMemoryConsumptionInBytes();
                int liquidReduced = extraData.getEstimatedMemoryConsumptionInBytes();
                int totalReduced = blocksReduced + sunlightReduced + lightReduced + liquidReduced;

                double blocksPercent = 100d - (100d / blocksSize * blocksReduced);
                double sunlightPercent = 100d - (100d / sunlightSize * sunlightReduced);
                double lightPercent = 100d - (100d / lightSize * lightReduced);
                double liquidPercent = 100d - (100d / liquidSize * liquidReduced);
                double totalPercent = 100d - (100d / totalSize * totalReduced);

                logger.debug("chunk {}: " +
                        "size-before: {} " +
                        "bytes, size-after: {} " +
                        "bytes, total-deflated-by: {}%, " +
                        "blocks-deflated-by={}%, " +
                        "sunlight-deflated-by={}%, " +
                        "light-deflated-by={}%, " +
                        "liquid-deflated-by={}%",
                        chunkPos,
                        SIZE_FORMAT.format(totalSize),
                        SIZE_FORMAT.format(totalReduced),
                        PERCENT_FORMAT.format(totalPercent),
                        PERCENT_FORMAT.format(blocksPercent),
                        PERCENT_FORMAT.format(sunlightPercent),
                        PERCENT_FORMAT.format(lightPercent),
                        PERCENT_FORMAT.format(liquidPercent));
                ChunkMonitor.fireChunkDeflated(this, totalSize, totalReduced);
            } else {
                final int oldSize = getEstimatedMemoryConsumptionInBytes();
                blockData = def.deflate(blockData);
                sunlightData = def.deflate(sunlightData);
                lightData = def.deflate(lightData);
                extraData = def.deflate(extraData);
                ChunkMonitor.fireChunkDeflated(this, oldSize, getEstimatedMemoryConsumptionInBytes());
            }
        } finally {
            unlock();
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

    public void setMesh(ChunkMesh[] mesh) {
        this.activeMesh = mesh;
    }

    public void setPendingMesh(ChunkMesh[] mesh) {
        this.pendingMesh = mesh;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean getAnimated() {
        return animated;
    }


    public ChunkMesh[] getMesh() {
        return activeMesh;
    }

    public ChunkMesh[] getPendingMesh() {
        return pendingMesh;
    }

    public AABB getSubMeshAABB(int subMesh) {
        if (subMeshAABB == null) {
            subMeshAABB = new AABB[ChunkConstants.VERTICAL_SEGMENTS];

            int heightHalf = ChunkConstants.SIZE_Y / ChunkConstants.VERTICAL_SEGMENTS / 2;

            for (int i = 0; i < subMeshAABB.length; i++) {
                Vector3f dimensions = new Vector3f(8, heightHalf, 8);
                Vector3f position = new Vector3f(getChunkWorldPosX() - 0.5f, (i * heightHalf * 2) - 0.5f, getChunkWorldPosZ() - 0.5f);
                position.add(dimensions);
                subMeshAABB[i] = AABB.createCenterExtent(position, dimensions);
            }
        }

        return subMeshAABB[subMesh];
    }

    public void markReady() {
        ready = true;
    }

    public void prepareForReactivation() {
        if (disposed) {
            disposed = false;
            Chunks c = Chunks.getInstance();
            sunlightData = c.getSunlightDataEntry().factory.create(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
            lightData = c.getLightDataEntry().factory.create(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
        }
    }

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
        ChunkMonitor.fireChunkDisposed(this);
    }

    public void disposeMesh() {
        if (activeMesh != null) {
            for (ChunkMesh chunkMesh : activeMesh) {
                chunkMesh.dispose();
            }
            activeMesh = null;
        }
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isDisposed() {
        return disposed;
    }

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

    public ChunkBlockIterator getBlockIterator() {
        return new ChunkBlockIteratorImpl(blockManager, getChunkWorldPos(), blockData);
    }

    /**
     * ProtobufHandler implements support for encoding/decoding chunks into/from protobuf messages.
     *
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * @todo Add support for chunk data extensions.
     */
    public static class ProtobufHandler {

        public EntityData.ChunkStore.Builder encode(ChunkImpl chunk, boolean coreOnly) {
            Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
            final TeraArrays t = TeraArrays.getInstance();
            final EntityData.ChunkStore.Builder b = EntityData.ChunkStore.newBuilder()
                    .setX(chunk.chunkPos.x).setY(chunk.chunkPos.y).setZ(chunk.chunkPos.z)
                    .setState(chunk.chunkState.protobufState)
                    .setBlockData(t.encode(chunk.blockData));
            if (!coreOnly) {
                b.setLiquidData(t.encode(chunk.extraData));
            }
            return b;
        }

        public ChunkImpl decode(EntityData.ChunkStore message) {
            Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
            if (!message.hasX() || !message.hasY() || !message.hasZ()) {
                throw new IllegalArgumentException("Ill-formed protobuf message. Missing chunk position.");
            }
            Vector3i pos = new Vector3i(message.getX(), message.getY(), message.getZ());
            if (!message.hasState()) {
                throw new IllegalArgumentException("Ill-formed protobuf message. Missing chunk state.");
            }
            State state = State.lookup(message.getState());
            if (!message.hasBlockData()) {
                throw new IllegalArgumentException("Ill-formed protobuf message. Missing block data.");
            }

            final TeraArrays t = TeraArrays.getInstance();
            final TeraArray blockData = t.decode(message.getBlockData());

            Chunks c = Chunks.getInstance();
            TeraArray sunlightData = c.getSunlightDataEntry().factory.create(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
            TeraArray lightData = c.getLightDataEntry().factory.create(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
            TeraArray extraData;
            if (message.hasLiquidData()) {
                extraData = t.decode(message.getLiquidData());
            } else {
                extraData = c.getExtraDataEntry().factory.create(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
            }

            boolean previouslyComplete = state == State.COMPLETE;
            if (previouslyComplete) {
                state = State.INTERNAL_LIGHT_GENERATION_PENDING;
            }
            return new ChunkImpl(pos, state, blockData, sunlightData, lightData, extraData, previouslyComplete);
        }
    }
}
