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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Vector3f;

import org.terasology.logic.manager.Config;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.TeraArray;
import org.terasology.model.structures.TeraSmartArray;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.liquid.LiquidData;

import com.google.common.base.Objects;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by its dimensions. They are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 * <p/>
 * Chunks are tessellated on creation and saved to vertex arrays. From those VBOs are generated
 * which are then used for the actual rendering process.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Chunk implements Externalizable {
    public static final long serialVersionUID = 79881925217704826L;

    public enum State {
        ADJACENCY_GENERATION_PENDING,
        INTERNAL_LIGHT_GENERATION_PENDING,
        LIGHT_PROPAGATION_PENDING,
        FULL_LIGHT_CONNECTIVITY_PENDING,
        COMPLETE
    }

    /* PUBLIC CONSTANT VALUES */
    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;
    public static final int INNER_CHUNK_POS_FILTER_X = TeraMath.ceilPowerOfTwo(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = TeraMath.ceilPowerOfTwo(SIZE_Z) - 1;
    public static final int POWER_X = TeraMath.sizeOfPower(SIZE_X);
    public static final int POWER_Z = TeraMath.sizeOfPower(SIZE_Z);
    public static final int VERTICAL_SEGMENTS = Config.getInstance().getVerticalChunkMeshSegments();
    public static final byte MAX_LIGHT = 0x0f;
    public static final byte MAX_LIQUID_DEPTH = 0x07;

    public static final Vector3i CHUNK_POWER = new Vector3i(POWER_X, 0, POWER_Z);
    public static final Vector3i CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3i INNER_CHUNK_POS_FILTER = new Vector3i(INNER_CHUNK_POS_FILTER_X, 0, INNER_CHUNK_POS_FILTER_Z);

    private final Vector3i pos = new Vector3i();

    private final TeraArray blocks;
    private final TeraSmartArray sunlight;
    private final TeraSmartArray light;
    private final TeraSmartArray liquid;

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
        blocks = new TeraArray(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        sunlight = new TeraSmartArray(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        light = new TeraSmartArray(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());
        liquid = new TeraSmartArray(getChunkSizeX(), getChunkSizeY(), getChunkSizeZ());

        setDirty(true);
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
        blocks = new TeraArray(other.blocks);
        sunlight = new TeraSmartArray(other.sunlight);
        light = new TeraSmartArray(other.light);
        liquid = new TeraSmartArray(other.liquid);
        chunkState = other.chunkState;
        dirty = true;
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

    public Block getBlock(Vector3i pos) {
        return BlockManager.getInstance().getBlock(blocks.get(pos.x, pos.y, pos.z));
    }

    public Block getBlock(int x, int y, int z) {
        return BlockManager.getInstance().getBlock(blocks.get(x, y, z));
    }

    public boolean setBlock(int x, int y, int z, Block block) {
        byte oldValue = blocks.set(x, y, z, block.getId());
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
            if (blocks.set(x, y, z, newBlock.getId(), oldBlock.getId())) {
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
        return sunlight.get(pos.x, pos.y, pos.z);
    }

    public byte getSunlight(int x, int y, int z) {
        return sunlight.get(x, y, z);
    }

    public boolean setSunlight(Vector3i pos, byte amount) {
        return setSunlight(pos.x, pos.y, pos.z, amount);
    }

    public boolean setSunlight(int x, int y, int z, byte amount) {
        byte oldValue = sunlight.set(x, y, z, amount);
        return oldValue != amount;
    }

    public byte getLight(Vector3i pos) {
        return light.get(pos.x, pos.y, pos.z);
    }

    public byte getLight(int x, int y, int z) {
        return light.get(x, y, z);
    }

    public boolean setLight(Vector3i pos, byte amount) {
        return setLight(pos.x, pos.y, pos.z, amount);
    }

    public boolean setLight(int x, int y, int z, byte amount) {
        byte oldValue = light.set(x, y, z, amount);
        return (oldValue != amount);
    }

    public boolean setLiquid(Vector3i pos, LiquidData newState, LiquidData oldState) {
        return setLiquid(pos.x, pos.y, pos.z, newState, oldState);
    }

    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        byte expected = oldState.toByte();
        byte newValue = newState.toByte();
        return liquid.set(x, y, z, newValue, expected) == expected;
    }

    public void setLiquid(int x, int y, int z, LiquidData newState) {
        byte newValue = newState.toByte();
        liquid.set(x, y, z, newValue);
    }

    public LiquidData getLiquid(Vector3i pos) {
        return getLiquid(pos.x, pos.y, pos.z);
    }

    public LiquidData getLiquid(int x, int y, int z) {
        return new LiquidData((liquid.get(x, y, z)));
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

        for (int i = 0; i < blocks.size(); i++)
            out.writeByte(blocks.getRawByte(i));

        for (int i = 0; i < sunlight.sizePacked(); i++)
            out.writeByte(sunlight.getRawByte(i));

        for (int i = 0; i < light.sizePacked(); i++)
            out.writeByte(light.getRawByte(i));

        for (int i = 0; i < liquid.sizePacked(); i++)
            out.writeByte(liquid.getRawByte(i));
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        pos.x = in.readInt();
        pos.y = in.readInt();
        pos.z = in.readInt();

        // Parse the flags...
        setDirty(true);
        chunkState = (State) in.readObject();

        for (int i = 0; i < blocks.size(); i++)
            blocks.setRawByte(i, in.readByte());

        for (int i = 0; i < sunlight.sizePacked(); i++)
            sunlight.setRawByte(i, in.readByte());

        for (int i = 0; i < light.sizePacked(); i++)
            light.setRawByte(i, in.readByte());

        for (int i = 0; i < liquid.sizePacked(); i++)
            liquid.setRawByte(i, in.readByte());
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
