/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import org.terasology.config.AdvancedConfig;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArrays;
import org.terasology.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.world.chunks.deflate.TeraDeflator;
import org.terasology.world.chunks.deflate.TeraStandardDeflator;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
public class Chunk implements Externalizable {
    protected static final Logger logger = LoggerFactory.getLogger(Chunk.class);

    public static final long serialVersionUID = 79881925217704826L;

    public static enum State {
        ADJACENCY_GENERATION_PENDING(ChunksProtobuf.State.ADJACENCY_GENERATION_PENDING),
        INTERNAL_LIGHT_GENERATION_PENDING(ChunksProtobuf.State.INTERNAL_LIGHT_GENERATION_PENDING),
        LIGHT_PROPAGATION_PENDING(ChunksProtobuf.State.LIGHT_PROPAGATION_PENDING),
        FULL_LIGHT_CONNECTIVITY_PENDING(ChunksProtobuf.State.FULL_LIGHT_CONNECTIVITY_PENDING),
        COMPLETE(ChunksProtobuf.State.COMPLETE);

        private final ChunksProtobuf.State protobufState;

        private static final Map<ChunksProtobuf.State, State> lookup;

        static {
            lookup = Maps.newHashMap();
            for (State s : State.values()) {
                lookup.put(s.protobufState, s);
            }
        }

        private State(ChunksProtobuf.State protobufState) {
            this.protobufState = Preconditions.checkNotNull(protobufState);
        }

        public final ChunksProtobuf.State getProtobufState() {
            return protobufState;
        }

        public static final State lookup(ChunksProtobuf.State state) {
            State result = lookup.get(Preconditions.checkNotNull(state, "The parameter 'state' must not be null"));
            if (result == null)
                throw new IllegalStateException("Unable to lookup the supplied state: " + state);
            return result;
        }
    }

    /* PUBLIC CONSTANT VALUES */
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

    private final Vector3i pos = new Vector3i();

    private TeraArray blockData;
    private TeraArray sunlightData;
    private TeraArray lightData;
    private TeraArray extraData;

    private State chunkState = State.ADJACENCY_GENERATION_PENDING;
    private boolean dirty;
    private boolean animated;
    private AABB aabb;

    // Rendering
    private ChunkMesh[] mesh;
    private ChunkMesh[] pendingMesh;
    private AABB[] subMeshAABB = null;

    private ReentrantLock lock = new ReentrantLock();
    private boolean disposed = false;


    public Chunk() {
        final Chunks c = Chunks.getInstance();
        blockData = c.getBlockDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        sunlightData = c.getSunlightDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        lightData = c.getLightDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        extraData = c.getExtraDataEntry().factory.create(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        dirty = true;
    }

    public Chunk(int x, int y, int z) {
        this();
        pos.x = x;
        pos.y = y;
        pos.z = z;
    }

    public Chunk(Vector3i pos) {
        this(pos.x, pos.y, pos.z);
    }

    public Chunk(Chunk other) {
        pos.set(other.pos);
        blockData = other.blockData.copy();
        sunlightData = other.sunlightData.copy();
        lightData = other.lightData.copy();
        extraData = other.extraData.copy();
        chunkState = other.chunkState;
        dirty = true;
    }

    public Chunk(Vector3i pos, State chunkState, TeraArray blocks, TeraArray sunlight, TeraArray light, TeraArray liquid) {
        this.pos.set(Preconditions.checkNotNull(pos));
        this.blockData = Preconditions.checkNotNull(blocks);
        this.sunlightData = Preconditions.checkNotNull(sunlight);
        this.lightData = Preconditions.checkNotNull(light);
        this.extraData = Preconditions.checkNotNull(liquid);
        this.chunkState = Preconditions.checkNotNull(chunkState);
        dirty = true;
    }

    /**
     * ProtobufHandler implements support for encoding/decoding chunks into/from protobuf messages.
     *
     * @author Manuel Brotz <manu.brotz@gmx.ch>
     * @todo Add support for chunk data extensions.
     */
    public static class ProtobufHandler implements org.terasology.io.ProtobufHandler<Chunk, ChunksProtobuf.Chunk> {

        @Override
        public ChunksProtobuf.Chunk encode(Chunk chunk) {
            Preconditions.checkNotNull(chunk, "The parameter 'chunk' must not be null");
            final TeraArrays t = TeraArrays.getInstance();
            final ChunksProtobuf.Chunk.Builder b = ChunksProtobuf.Chunk.newBuilder()
                    .setX(chunk.pos.x).setY(chunk.pos.y).setZ(chunk.pos.z)
                    .setState(chunk.chunkState.protobufState)
                    .setBlockData(t.encode(chunk.blockData))
                    .setSunlightData(t.encode(chunk.sunlightData))
                    .setLightData(t.encode(chunk.lightData))
                    .setExtraData(t.encode(chunk.extraData));
            return b.build();
        }

        @Override
        public Chunk decode(ChunksProtobuf.Chunk message) {
            Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
            if (!message.hasX())
                throw new IllegalArgumentException("Illformed protobuf message. Missing x coordinate.");
            if (!message.hasY())
                throw new IllegalArgumentException("Illformed protobuf message. Missing y coordinate.");
            if (!message.hasZ())
                throw new IllegalArgumentException("Illformed protobuf message. Missing z coordinate.");
            final Vector3i pos = new Vector3i(message.getX(), message.getY(), message.getZ());
            if (!message.hasState())
                throw new IllegalArgumentException("Illformed protobuf message. Missing chunk state.");
            final State state = State.lookup(message.getState());
            if (!message.hasBlockData())
                throw new IllegalArgumentException("Illformed protobuf message. Missing block data.");
            if (!message.hasSunlightData())
                throw new IllegalArgumentException("Illformed protobuf message. Missing sunlight data.");
            if (!message.hasLightData())
                throw new IllegalArgumentException("Illformed protobuf message. Missing light data.");
            if (!message.hasExtraData())
                throw new IllegalArgumentException("Illformed protobuf message. Missing extra data.");
            final TeraArrays t = TeraArrays.getInstance();
            final TeraArray blockData = t.decode(message.getBlockData());
            final TeraArray sunlightData = t.decode(message.getSunlightData());
            final TeraArray lightData = t.decode(message.getLightData());
            final TeraArray extraData = t.decode(message.getExtraData());
            return new Chunk(pos, state, blockData, sunlightData, lightData, extraData);
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

    public State getChunkState() {
        return chunkState;
    }

    public void setChunkState(State chunkState) {
        Preconditions.checkNotNull(chunkState);
        this.chunkState = chunkState;
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
        return blockData.getEstimatedMemoryConsumptionInBytes() + sunlightData.getEstimatedMemoryConsumptionInBytes() + lightData.getEstimatedMemoryConsumptionInBytes() + extraData.getEstimatedMemoryConsumptionInBytes();
    }

    public Block getBlock(Vector3i pos) {
        return BlockManager.getInstance().getBlock((byte) blockData.get(pos.x, pos.y, pos.z));
    }

    public Block getBlock(int x, int y, int z) {
        return BlockManager.getInstance().getBlock((byte) blockData.get(x, y, z));
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
            Vector3f position = new Vector3f(getChunkWorldPosX() + dimensions.x - 0.5f, dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
            aabb = AABB.createCenterExtent(position, dimensions);
        }

        return aabb;
    }

    // TODO: Protobuf instead???
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(pos.x);
        out.writeInt(pos.y);
        out.writeInt(pos.z);
        out.writeObject(chunkState);
        out.writeObject(blockData);
        out.writeObject(sunlightData);
        out.writeObject(lightData);
        out.writeObject(extraData);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        pos.x = in.readInt();
        pos.y = in.readInt();
        pos.z = in.readInt();
        setDirty(true);
        chunkState = (State) in.readObject();
        blockData = (TeraArray) in.readObject();
        sunlightData = (TeraArray) in.readObject();
        lightData = (TeraArray) in.readObject();
        extraData = (TeraArray) in.readObject();
    }

    private static DecimalFormat fpercent = new DecimalFormat("0.##");
    private static DecimalFormat fsize = new DecimalFormat("#,###");

    public void deflate() {
        if (getChunkState() != State.COMPLETE) {
            logger.warn("Before deflation the state of the chunk ({}, {}, {}) should be set to State.COMPLETE but is now State.{}", getPos().x, getPos().y, getPos().z, getChunkState().toString());
        }
        lock();
        try {
            AdvancedConfig config = CoreRegistry.get(org.terasology.config.Config.class).getAdvanced();
            final TeraDeflator def = new TeraStandardDeflator();

            if (config.isChunkDeflationLoggingEnabled()) {
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

                logger.info(String.format("chunk (%d, %d, %d): size-before: %s bytes, size-after: %s bytes, total-deflated-by: %s%%, blocks-deflated-by=%s%%, sunlight-deflated-by=%s%%, light-deflated-by=%s%%, liquid-deflated-by=%s%%", pos.x, pos.y, pos.z, fsize.format(totalSize), fsize.format(totalReduced), fpercent.format(totalPercent), fpercent.format(blocksPercent), fpercent.format(sunlightPercent), fpercent.format(lightPercent), fpercent.format(liquidPercent)));
            } else {
                blockData = def.deflate(blockData);
                sunlightData = def.deflate(sunlightData);
                lightData = def.deflate(lightData);
                extraData = def.deflate(extraData);
            }
        } finally {
            unlock();
        }
    }

    public void inflate() {
        lock();
        try {
            if (!(blockData instanceof TeraDenseArray8Bit))
                blockData = new TeraDenseArray8Bit(blockData);
            if (!(sunlightData instanceof TeraDenseArray4Bit))
                sunlightData = new TeraDenseArray4Bit(sunlightData);
            if (!(lightData instanceof TeraDenseArray4Bit))
                lightData = new TeraDenseArray4Bit(lightData);
            if (!(extraData instanceof TeraDenseArray4Bit))
                extraData = new TeraDenseArray4Bit(extraData);
        } finally {
            unlock();
        }
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
                Vector3f position = new Vector3f(getChunkWorldPosX() + dimensions.x - 0.5f, (i * heightHalf * 2) + dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
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
