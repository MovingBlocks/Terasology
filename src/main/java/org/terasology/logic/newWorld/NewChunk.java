/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.newWorld;

import com.bulletphysics.dynamics.RigidBody;
import com.google.common.base.Objects;
import org.terasology.logic.manager.Config;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.TeraArray;
import org.terasology.model.structures.TeraSmartArray;
import org.terasology.rendering.primitives.ChunkMesh;

import javax.vecmath.Vector3d;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
 */
public class NewChunk implements Externalizable {
    public static final long serialVersionUID = 79881925217704826L;

    public enum State {
        Awaiting2ndPass,
        AwaitingFullLighting,
        Complete
    }

    /* PUBLIC CONSTANT VALUES */
    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;
    public static final int VERTICAL_SEGMENTS = Config.getInstance().getVerticalChunkMeshSegments();
    public static final byte MAX_LIGHT = 0x0f;

    private final Vector3i pos = new Vector3i();

    private final TeraArray blocks;
    private final TeraSmartArray sunlight, light, states;

    private State chunkState = State.Awaiting2ndPass;
    private boolean dirty;
    private AABB aabb;

    // Rendering
    private ChunkMesh[] mesh;
    private ChunkMesh[] pendingMesh;
    private AABB[] subMeshAABB = null;

    // Physics
    private RigidBody rigidBody = null;

    private ReentrantLock lock = new ReentrantLock();


    public NewChunk() {
        blocks = new TeraArray(SIZE_X, SIZE_Y, SIZE_Z);
        sunlight = new TeraSmartArray(SIZE_X, SIZE_Y, SIZE_Z);
        light = new TeraSmartArray(SIZE_X, SIZE_Y, SIZE_Z);
        states = new TeraSmartArray(SIZE_X, SIZE_Y, SIZE_Z);

        setDirty(true);
    }

    public NewChunk(int x, int y, int z) {
        this();
        pos.x = x;
        pos.y = y;
        pos.z = z;
    }

    public NewChunk(Vector3i pos) {
        this(pos.x, pos.y, pos.z);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public Vector3i getPos() {
        return new Vector3i(pos);
    }

    public boolean isInBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < SIZE_X && y < SIZE_Y && z < SIZE_Z;
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

    public byte getBlockId(Vector3i pos) {
        return blocks.get(pos.x, pos.y, pos.z);
    }

    public byte getBlockId(int x, int y, int z) {
        return blocks.get(x, y, z);
    }

    public Block getBlock(Vector3i pos) {
        return BlockManager.getInstance().getBlock(blocks.get(pos.x, pos.y, pos.z));
    }

    public Block getBlock(int x, int y, int z) {
        return BlockManager.getInstance().getBlock(blocks.get(x, y, z));
    }

    public boolean setBlock(int x, int y, int z, byte blockId) {
        byte oldValue = blocks.set(x, y, z, blockId);
        if (oldValue != blockId) {
            setDirty(true);
            return true;
        }
        return false;
    }

    public boolean setBlock(int x, int y, int z, byte newBlockId, byte oldBlockId) {
        return blocks.set(x, y, z, newBlockId, oldBlockId);
    }

    public boolean setBlock(int x, int y, int z, Block block) {
        return setBlock(x, y, z, block.getId());
    }

    public boolean setBlock(int x, int y, int z, Block newBlock, Block oldBlock) {
        return setBlock(x, y, z, newBlock.getId(), oldBlock.getId());
    }

    public boolean setBlock(Vector3i pos, byte blockId) {
        return setBlock(pos.x, pos.y, pos.z, blockId);
    }

    public boolean setBlock(Vector3i pos, byte blockId, byte oldBlockId) {
        return setBlock(pos.x, pos.y, pos.z, blockId, oldBlockId);
    }

    public boolean setBlock(Vector3i pos, Block block) {
        return setBlock(pos.x, pos.y, pos.z, block.getId());
    }

    public boolean setBlock(Vector3i pos, Block block, Block oldBlock) {
        return setBlock(pos.x, pos.y, pos.z, block.getId(), oldBlock.getId());
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

    public boolean setState(Vector3i pos, byte state, byte oldState) {
        return setState(pos.x, pos.y, pos.z, state, oldState);
    }

    public boolean setState(int x, int y, int z, byte state, byte oldState) {
        byte prev = states.set(x, y, z, state, oldState);
        return prev == oldState;
    }

    public byte getState(Vector3i pos) {
        return states.get(pos.x, pos.y, pos.z);
    }

    public byte getState(int x, int y, int z) {
        return states.get(x, y, z);
    }

    public Vector3i getChunkWorldPos() {
        return new Vector3i(getChunkWorldPosX(), getChunkWorldPosY(), getChunkWorldPosZ());
    }

    public int getChunkWorldPosX() {
        return pos.x * SIZE_X;
    }

    public int getChunkWorldPosY() {
        return pos.y * SIZE_Y;
    }

    public int getChunkWorldPosZ() {
        return pos.z * SIZE_Z;
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
            Vector3d dimensions = new Vector3d(SIZE_X / 2.0, SIZE_Y / 2.0, SIZE_Z / 2.0);
            Vector3d position = new Vector3d(getChunkWorldPosX() + dimensions.x - 0.5f, dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
            aabb = new AABB(position, dimensions);
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

        for (int i = 0; i < states.sizePacked(); i++)
            out.writeByte(states.getRawByte(i));
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

        for (int i = 0; i < states.sizePacked(); i++)
            states.setRawByte(i, in.readByte());
    }

    @Override
    public String toString() {
        return "Chunk" + pos.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof NewChunk) {
            NewChunk other = (NewChunk) o;
            return Objects.equal(pos, other.pos);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

    public void setMesh(ChunkMesh[] mesh) {
        this.mesh = mesh;
        if (rigidBody != null) {
            rigidBody.destroy();
            rigidBody = null;
        }
    }

    public void setPendingMesh(ChunkMesh[] mesh) {
        this.pendingMesh = mesh;
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
                Vector3d dimensions = new Vector3d(8, heightHalf, 8);
                Vector3d position = new Vector3d(getChunkWorldPosX() + dimensions.x - 0.5f, (i * heightHalf * 2) + dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
                subMeshAABB[i] = new AABB(position, dimensions);
            }
        }

        return subMeshAABB[subMesh];
    }

    public RigidBody getRigidBody() {
        return rigidBody;
    }

    public void setRigidBody(RigidBody rigidBody) {
        this.rigidBody = rigidBody;
    }

    public void dispose() {
        if (rigidBody != null) {
            rigidBody.destroy();
            rigidBody = null;
        }
        if (mesh != null) {
            for (ChunkMesh chunkMesh : mesh) {
                chunkMesh.dispose();
            }
            mesh = null;
        }
    }
}
