/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArrays;
import org.terasology.world.chunks.deflate.TeraDeflator;
import org.terasology.world.chunks.deflate.TeraStandardDeflator;
import org.terasology.world.chunks.internal.ChunkBlockIteratorImpl;
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
public class Chunk {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;
    public static final int INNER_CHUNK_POS_FILTER_X = TeraMath.ceilPowerOfTwo(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = TeraMath.ceilPowerOfTwo(SIZE_Z) - 1;
    public static final int POWER_X = TeraMath.sizeOfPower(SIZE_X);
    public static final int POWER_Z = TeraMath.sizeOfPower(SIZE_Z);
    public static final int VERTICAL_SEGMENTS = CoreRegistry.get(Config.class).getSystem().getVerticalChunkMeshSegments();
    public static final byte MAX_LIGHT = 0x0f;
    public static final byte MAX_LIQUID_DEPTH = 0x07;

    public static final Vector3i CHUNK_POWER = new Vector3i(POWER_X, 0, POWER_Z);
    public static final Vector3i CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3i INNER_CHUNK_POS_FILTER = new Vector3i(INNER_CHUNK_POS_FILTER_X, 0, INNER_CHUNK_POS_FILTER_Z);

    private static final Logger logger = LoggerFactory.getLogger(Chunk.class);

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.##");
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,###");

    public static enum State {
        ADJACENCY_GENERATION_PENDING(EntityData.ChunkState.ADJACENCY_GENERATION_PENDING),
        INTERNAL_LIGHT_GENERATION_PENDING(EntityData.ChunkState.INTERNAL_LIGHT_GENERATION_PENDING),
        LIGHT_PROPAGATION_PENDING(EntityData.ChunkState.LIGHT_PROPAGATION_PENDING),
        FULL_LIGHT_CONNECTIVITY_PENDING(EntityData.ChunkState.FULL_LIGHT_CONNECTIVITY_PENDING),
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

        public final EntityData.ChunkState getProtobufState() {
            return protobufState;
        }

        public static State lookup(EntityData.ChunkState state) {
            State result = LOOKUP.get(Preconditions.checkNotNull(state, "The parameter 'state' must not be null"));
            if (result == null) {
                throw new IllegalStateException("Unable to lookup the supplied state: " + state);
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

    // Rendering
    private ChunkMesh[] activeMesh;
    private ChunkMesh[] pendingMesh;
    private AABB[] subMeshAABB;

    private ReentrantLock lock = new ReentrantLock();
    private boolean disposed;

    private boolean ready;

    protected Chunk() {
        final Chunks c = Chunks.getInstance();
        blockData = c.getBlockDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        sunlightData = c.getSunlightDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        lightData = c.getLightDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        extraData = c.getExtraDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        dirty = true;
        blockManager = CoreRegistry.get(BlockManager.class);
    }

    public Chunk(int x, int y, int z) {
        this();
        chunkPos.x = x;
        chunkPos.y = y;
        chunkPos.z = z;
        ChunkMonitor.fireChunkCreated(this);
    }

    public Chunk(Vector3i chunkPos) {
        this(chunkPos.x, chunkPos.y, chunkPos.z);
    }

    public Chunk(Vector3i chunkPos, State chunkState, TeraArray blocks, TeraArray sunlight, TeraArray light, TeraArray liquid, boolean loaded) {
        this.chunkPos.set(Preconditions.checkNotNull(chunkPos));
        this.blockData = Preconditions.checkNotNull(blocks);
        this.sunlightData = Preconditions.checkNotNull(sunlight);
        this.lightData = Preconditions.checkNotNull(light);
        this.extraData = Preconditions.checkNotNull(liquid);
        this.chunkState = Preconditions.checkNotNull(chunkState);
        dirty = true;
        blockManager = CoreRegistry.get(BlockManager.class);
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

    public Block getBlock(Vector3i pos) {
        return blockManager.getBlock((byte) blockData.get(pos.x, pos.y, pos.z));
    }

    public Block getBlock(int x, int y, int z) {
        return blockManager.getBlock((byte) blockData.get(x, y, z));
    }

    public void setBlock(int x, int y, int z, Block block) {
        int oldValue = blockData.set(x, y, z, block.getId());
        if (oldValue != block.getId()) {
            if (!block.isLiquid()) {
                setLiquid(x, y, z, new LiquidData());
            }
        }
    }

    public void setBlock(Vector3i pos, Block block) {
        setBlock(pos.x, pos.y, pos.z, block);
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

    public void setLiquid(Vector3i pos, LiquidData state) {
        setLiquid(pos.x, pos.y, pos.z, state);
    }

    public void setLiquid(int x, int y, int z, LiquidData newState) {
        byte newValue = newState.toByte();
        extraData.set(x, y, z, newValue);
    }

    public LiquidData getLiquid(Vector3i pos) {
        return getLiquid(pos.x, pos.y, pos.z);
    }

    public LiquidData getLiquid(int x, int y, int z) {
        return new LiquidData((byte) extraData.get(x, y, z));
    }

    public Vector3i getChunkWorldPos() {
        return new Vector3i(getChunkWorldPosX(), getChunkWorldPosY(), getChunkWorldPosZ());
    }

    public int getChunkWorldPosX() {
        return chunkPos.x * getChunkSizeX();
    }

    public int getChunkWorldPosY() {
        return chunkPos.y * getChunkSizeY();
    }

    public int getChunkWorldPosZ() {
        return chunkPos.z * getChunkSizeZ();
    }

    public Vector3i getBlockWorldPos(Vector3i blockPos) {
        return getBlockWorldPos(blockPos.x, blockPos.y, blockPos.z);
    }

    public Vector3i getBlockWorldPos(int x, int y, int z) {
        return new Vector3i(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z));
    }

    public int getBlockWorldPosX(int x) {
        return x + getChunkWorldPosX();
    }

    public int getBlockWorldPosY(int y) {
        return y + getChunkWorldPosY();
    }

    public int getBlockWorldPosZ(int z) {
        return z + getChunkWorldPosZ();
    }

    public AABB getAABB() {
        if (aabb == null) {
            Vector3f min = getChunkWorldPos().toVector3f();
            Vector3f max = CHUNK_SIZE.toVector3f();
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
            subMeshAABB = new AABB[VERTICAL_SEGMENTS];

            int heightHalf = SIZE_Y / VERTICAL_SEGMENTS / 2;

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
        disposed = false;
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

    public int getChunkSizeX() {
        return SIZE_X;
    }

    public int getChunkSizeY() {
        return SIZE_Y;
    }

    public int getChunkSizeZ() {
        return SIZE_Z;
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

        public EntityData.ChunkStore.Builder encode(Chunk chunk, boolean coreOnly) {
            Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
            final TeraArrays t = TeraArrays.getInstance();
            final EntityData.ChunkStore.Builder b = EntityData.ChunkStore.newBuilder()
                    .setX(chunk.chunkPos.x).setY(chunk.chunkPos.y).setZ(chunk.chunkPos.z)
                    .setState(chunk.chunkState.protobufState)
                    .setBlockData(t.encode(chunk.blockData));
            if (!coreOnly) {
                b.setSunlightData(t.encode(chunk.sunlightData))
                        .setLightData(t.encode(chunk.lightData))
                        .setLiquidData(t.encode(chunk.extraData));
            }
            return b;
        }

        public Chunk decode(EntityData.ChunkStore message) {
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
            TeraArray sunlightData;
            if (message.hasSunlightData()) {
                sunlightData = t.decode(message.getSunlightData());
            } else {
                sunlightData = c.getSunlightDataEntry().factory.create(SIZE_X, SIZE_Y, SIZE_Z);
                if (state == State.COMPLETE || state == State.FULL_LIGHT_CONNECTIVITY_PENDING || state == State.LIGHT_PROPAGATION_PENDING) {
                    state = State.INTERNAL_LIGHT_GENERATION_PENDING;
                }
            }
            TeraArray lightData;
            if (message.hasLightData()) {
                lightData = t.decode(message.getLightData());
            } else {
                lightData = c.getLightDataEntry().factory.create(SIZE_X, SIZE_Y, SIZE_Z);
                if (state == State.COMPLETE || state == State.FULL_LIGHT_CONNECTIVITY_PENDING || state == State.LIGHT_PROPAGATION_PENDING) {
                    state = State.INTERNAL_LIGHT_GENERATION_PENDING;
                }
            }
            TeraArray extraData;
            if (message.hasLiquidData()) {
                extraData = t.decode(message.getLiquidData());
            } else {
                extraData = c.getExtraDataEntry().factory.create(SIZE_X, SIZE_Y, SIZE_Z);
            }
            return new Chunk(pos, state, blockData, sunlightData, lightData, extraData, state == State.COMPLETE);
        }
    }
}
