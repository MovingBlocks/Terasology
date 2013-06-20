/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.game.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.ChunksProtobuf.ModData;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.perBlockStorage.PerBlockStorageManager;
import org.terasology.world.chunks.perBlockStorage.TeraArray;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;

import java.util.List;
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
    protected static final Logger logger = LoggerFactory.getLogger(Chunk.class);

    /* PUBLIC CONSTANT VALUES */
    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;
    public static final int INNER_CHUNK_POS_FILTER_X = TeraMath.ceilPowerOfTwo(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = TeraMath.ceilPowerOfTwo(SIZE_Z) - 1;
    public static final int POWER_X = TeraMath.sizeOfPower(SIZE_X);
    public static final int POWER_Z = TeraMath.sizeOfPower(SIZE_Z);
    public static final int VERTICAL_SEGMENTS = CoreRegistry.get(Config.class).getSystem()
        .getVerticalChunkMeshSegments();
    public static final byte MAX_LIGHT = 0x0f;
    public static final byte MAX_LIQUID_DEPTH = 0x07;

    public static final Vector3i CHUNK_POWER = new Vector3i(POWER_X, 0, POWER_Z);
    public static final Vector3i CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3i INNER_CHUNK_POS_FILTER = new Vector3i(INNER_CHUNK_POS_FILTER_X, 0,
        INNER_CHUNK_POS_FILTER_Z);

    private ChunkState chunkState = ChunkState.ADJACENCY_GENERATION_PENDING;
    private final Vector3i pos = new Vector3i();

    private TeraArray blockData;
    private TeraArray sunlightData;
    private TeraArray lightData;
    private TeraArray extraData;
    private Map<String, TeraArray> extensionData;

    private boolean dirty;
    private boolean animated;
    private AABB aabb;

    // Rendering
    private ChunkMesh[] mesh;
    private ChunkMesh[] pendingMesh;
    private AABB[] subMeshAABB = null;

    private ReentrantLock lock = new ReentrantLock();
    private boolean disposed = false;


    public Chunk(int x, int y, int z) {
        this.pos.set(x, y, z);
        final PerBlockStorageManager manager = CoreRegistry.get(PerBlockStorageManager.class);
        this.blockData = manager.createBlockStorage(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        this.sunlightData = manager.createSunlightStorage(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        this.lightData = manager.createLightStorage(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        this.extraData = manager.createExtraStorage(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        this.extensionData = Maps.newConcurrentMap();
        this.dirty = true;
        ChunkMonitor.fireChunkCreated(this);
    }

    public Chunk(Vector3i pos) {
        this(pos.x, pos.y, pos.z);
    }

    public Chunk(Chunk other) {
        this.pos.set(other.pos);
        this.blockData = other.blockData.copy();
        this.sunlightData = other.sunlightData.copy();
        this.lightData = other.lightData.copy();
        this.extraData = other.extraData.copy();
        this.chunkState = other.chunkState;
        this.extensionData = Maps.newConcurrentMap();
        for (Map.Entry<String, TeraArray> e : other.extensionData.entrySet()) {
            this.extensionData.put(e.getKey(), e.getValue().copy());
        }
        this.dirty = true;
        ChunkMonitor.fireChunkCreated(this);
    }

    public Chunk(Vector3i pos, ChunkState chunkState, TeraArray blocks, TeraArray sunlight, TeraArray light,
                 TeraArray liquid, Map<String, TeraArray> extensionData) {
        this.pos.set(Preconditions.checkNotNull(pos));
        this.blockData = Preconditions.checkNotNull(blocks);
        this.sunlightData = Preconditions.checkNotNull(sunlight);
        this.lightData = Preconditions.checkNotNull(light);
        this.extraData = Preconditions.checkNotNull(liquid);
        this.chunkState = Preconditions.checkNotNull(chunkState);
        if (extensionData == null)
            this.extensionData = Maps.newConcurrentMap();
        else
            this.extensionData = extensionData;
        this.dirty = true;
        ChunkMonitor.fireChunkCreated(this);
    }

    /**
     * Deflator implements chunk runtime compression.
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     */
    public static class Deflator {
        
        private final PerBlockStorageManager manager;
        
        public Deflator(PerBlockStorageManager manager) {
            this.manager = Preconditions.checkNotNull(manager, "The parameter 'manager' must not be null");
        }
        
        public void deflate(Chunk chunk) {
            chunk.lock();
            try {
                final boolean loggingEnabled = manager.isChunkDeflationLoggingEnabled();
                long bytesBefore = 0;
                if (loggingEnabled) 
                    bytesBefore = chunk.getEstimatedMemoryConsumptionInBytes();
                chunk.blockData = manager.deflate(chunk.blockData);
                chunk.sunlightData = manager.deflate(chunk.sunlightData);
                chunk.lightData = manager.deflate(chunk.lightData);
                chunk.extraData = manager.deflate(chunk.extraData);
                if (chunk.extensionData.size() > 0) {
                    final Map<String, TeraArray> deflatedExtensions = Maps.newConcurrentMap();
                    for (final Map.Entry<String, TeraArray> extension : chunk.extensionData.entrySet()) {
                        deflatedExtensions.put(extension.getKey(), manager.deflate(extension.getValue()));
                    }
                    chunk.extensionData = deflatedExtensions;
                }
                if (loggingEnabled) {
                    long bytesAfter = chunk.getEstimatedMemoryConsumptionInBytes();
                    long bytesSaved = bytesBefore - bytesAfter;
                    double percentSaved = Math.round((100.0 / bytesBefore * bytesSaved) * 100.0) / 100.0;
                    logger.info("Runtime chunk compression {}: {} % saved (bytes saved = {}, compressed size = {}, uncompressed size = {})", chunk.pos, percentSaved, bytesSaved, bytesAfter, bytesBefore);
                }
            } finally {
                chunk.unlock();
            }
        }
    }
    
    /**
     * ProtobufHandler implements support for encoding/decoding chunks into/from protobuf messages.
     *
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * TODO: Add support for chunk data extensions.
     */
    public static class ProtobufHandler implements org.terasology.io.ProtobufHandler<Chunk, ChunksProtobuf.Chunk> {

        private final PerBlockStorageManager manager;
        
        public ProtobufHandler(PerBlockStorageManager manager) {
            this.manager = Preconditions.checkNotNull(manager, "The parameter 'manager' must not be null");
        }
        
        @Override
        public ChunksProtobuf.Chunk encode(Chunk chunk) {
            Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
            final ChunksProtobuf.Chunk.Builder b = ChunksProtobuf.Chunk.newBuilder();
            b.setX(chunk.pos.x).setY(chunk.pos.y).setZ(chunk.pos.z)
            .setState(chunk.chunkState.id)
            .setBlockData(manager.encode(chunk.blockData))
            .setSunlightData(manager.encode(chunk.sunlightData))
            .setLightData(manager.encode(chunk.lightData))
            .setExtraData(manager.encode(chunk.extraData));
            for (Map.Entry<String, TeraArray> e : chunk.extensionData.entrySet()) {
                final ChunksProtobuf.ModData.Builder mb = ChunksProtobuf.ModData.newBuilder();
                mb.setData(manager.encode(e.getValue()))
                .setId(e.getKey());
                b.addModData(mb);
            }
            return b.build();
        }

        @Override
        public Chunk decode(ChunksProtobuf.Chunk message) {
            Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
            if (!message.hasX()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing x coordinate.");
            }
            if (!message.hasY()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing y coordinate.");
            }
            if (!message.hasZ()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing z coordinate.");
            }

            final Vector3i pos = new Vector3i(message.getX(), message.getY(), message.getZ());

            if (!message.hasState()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing chunk state.");
            }

            final ChunkState state = ChunkState.getStateById(message.getState());

            if (state == null) {
                throw new IllegalArgumentException("Illformed protobuf message. Unknown chunk state: "
                    + message.getState());
            }
            if (!message.hasBlockData()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing block data.");
            }
            if (!message.hasSunlightData()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing sunlight data.");
            }
            if (!message.hasLightData()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing light data.");
            }
            if (!message.hasExtraData()) {
                throw new IllegalArgumentException("Illformed protobuf message. Missing extra data.");
            }
            final TeraArray blockData = manager.decode(message.getBlockData());
            final TeraArray sunlightData = manager.decode(message.getSunlightData());
            final TeraArray lightData = manager.decode(message.getLightData());
            final TeraArray extraData = manager.decode(message.getExtraData());
            final Map<String, TeraArray> extensionData = Maps.newConcurrentMap();
            final List<ModData> modDataList = message.getModDataList();
            if (modDataList != null)
                for (ModData modData : modDataList) {
                    if (!modData.hasId() || modData.getId().trim().isEmpty())
                        throw new IllegalArgumentException("Illformed protobuf message. Missing mod data id.");
                    if (!modData.hasData())
                        throw new IllegalArgumentException("Illformed protobuf message. Missing mod data.");
                    final TeraArray data = manager.decode(modData.getData());
                    extensionData.put(modData.getId(), data);
                }
            return new Chunk(pos, state, blockData, sunlightData, lightData, extraData, extensionData);
        }

        @Override
        public void decode(org.terasology.protobuf.ChunksProtobuf.Chunk message, Chunk value) {
            throw new UnsupportedOperationException();
        }
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
        return new Vector3i(pos);
    }

    public boolean isInBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < getChunkSizeX() && y < getChunkSizeY() && z < getChunkSizeZ();
    }

    public ChunkState getChunkState() {
        return chunkState;
    }

    public void setChunkState(ChunkState chunkState) {
        Preconditions.checkNotNull(chunkState);
        if (this.chunkState != chunkState) {
            final ChunkState old = this.chunkState;
            this.chunkState = chunkState;
            ChunkMonitor.fireStateChanged(this, old);
        }
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
        int size = blockData.getEstimatedMemoryConsumptionInBytes() + sunlightData.getEstimatedMemoryConsumptionInBytes()
            + lightData.getEstimatedMemoryConsumptionInBytes() + extraData.getEstimatedMemoryConsumptionInBytes();
        for (final TeraArray ext : extensionData.values())
            size += ext.getEstimatedMemoryConsumptionInBytes();
        return size;
    }

    public Block getBlock(Vector3i pos) {
        return BlockManager.getInstance().getBlock((short) blockData.get(pos.x, pos.y, pos.z));
    }

    public Block getBlock(int x, int y, int z) {
        return BlockManager.getInstance().getBlock((short) blockData.get(x, y, z));
    }

    public boolean setBlock(int x, int y, int z, Block block) {
        int oldValue = blockData.set(x, y, z, block.getId());
        if (oldValue != block.getId()) {
            if (!block.isLiquid()) {
                setLiquid(x, y, z, new LiquidData());
            }
            return true;
        }
        return false;
    }

    public boolean setBlock(int x, int y, int z, Block newBlock, Block oldBlock) {
        if (newBlock != oldBlock) {
            if (blockData.set(x, y, z, newBlock.getId(), oldBlock.getId())) {
                if (!newBlock.isLiquid()) {
                    setLiquid(x, y, z, new LiquidData());
                }
                return true;
            }
        }
        return false;
    }

    public boolean setBlock(Vector3i pos, Block block) {
        return setBlock(pos.x, pos.y, pos.z, block);
    }

    public boolean setBlock(Vector3i pos, Block block, Block oldBlock) {
        return setBlock(pos.x, pos.y, pos.z, block, oldBlock);
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
        return lightData.set(x, y, z, amount) != amount;
    }

    public boolean setLiquid(Vector3i pos, LiquidData newState, LiquidData oldState) {
        return setLiquid(pos.x, pos.y, pos.z, newState, oldState);
    }

    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        byte expected = oldState.toByte();
        byte newValue = newState.toByte();
        return extraData.set(x, y, z, newValue, expected);
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
        return pos.x * getChunkSizeX();
    }

    public int getChunkWorldPosY() {
        return pos.y * getChunkSizeY();
    }

    public int getChunkWorldPosZ() {
        return pos.z * getChunkSizeZ();
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
            Vector3f dimensions = new Vector3f(0.5f * getChunkSizeX(), 0.5f * getChunkSizeY(), 0.5f * getChunkSizeZ());
            Vector3f position = new Vector3f(getChunkWorldPosX() + dimensions.x - 0.5f, dimensions.y - 0.5f,
                getChunkWorldPosZ() + dimensions.z - 0.5f);
            aabb = AABB.createCenterExtent(position, dimensions);
        }

        return aabb;
    }

    @Override
    public String toString() {
        return "Chunk" + pos.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

    public void setMesh(ChunkMesh[] mesh) {
        this.mesh = mesh;
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
        return mesh;
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
                Vector3f position = new Vector3f(getChunkWorldPosX() + dimensions.x - 0.5f,
                    (i * heightHalf * 2) + dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
                subMeshAABB[i] = AABB.createCenterExtent(position, dimensions);
            }
        }

        return subMeshAABB[subMesh];
    }

    public void dispose() {
        disposed = true;
        if (mesh != null) {
            for (ChunkMesh chunkMesh : mesh) {
                chunkMesh.dispose();
            }
            mesh = null;
        }
        ChunkMonitor.fireChunkDisposed(this);
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
}
