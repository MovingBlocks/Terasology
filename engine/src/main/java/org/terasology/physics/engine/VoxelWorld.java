// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.physics.engine;

import com.badlogic.gdx.physics.bullet.collision.VoxelCollisionAlgorithmWrapper;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import org.joml.Vector3ic;
import org.terasology.physics.bullet.shapes.BulletCollisionShape;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

public abstract class VoxelWorld {

    private final boolean[] registered = new boolean[Short.MAX_VALUE];
    private VoxelCollisionAlgorithmWrapper wrapper;
    private BlockManager blockManager;

    public VoxelWorld(VoxelCollisionAlgorithmWrapper wrapper, BlockManager blockManager) {
        this.wrapper = wrapper;
        this.blockManager = blockManager;
    }

    /**
     * update voxel info for the wrapper for the associated block id
     *
     * @param block the block
     */
    private void register(Block block) {
        btCollisionShape shape = ((BulletCollisionShape) block.getCollisionShape()).underlyingShape;
        btVoxelInfo info = new btVoxelInfo(shape != null && block.isTargetable(),
                shape != null && !block.isPenetrable(), block.getId(), shape, block.getCollisionOffset(),
                block.getFriction(), block.getRestitution(), block.getFriction());
        wrapper.setVoxelInfo(info);
    }

    /**
     * try to update voxel info for the wrapper for the associated block id. nothing if block already registred
     *
     * @param id block id
     */
    public void tryRegister(short id) {
        if (!registered[id]) {
            register(blockManager.getBlock(id));
            registered[id] = true;
        }
    }

    /**
     * try to update voxel info for the wrapper for the associated block. nothing if block already registred
     *
     * @param block the block
     */
    public void tryRegister(Block block) {
        if (!registered[block.getId()]) {
            register(block);
            registered[block.getId()] = true;
        }
    }

    public void setRegion(int x, int y, int z, java.nio.ShortBuffer input) {
        wrapper.setRegion(x, y, z, input);
    }

    public void setBlock(int x, int y, int z, short id) {
        wrapper.setBlock(x, y, z, id);
    }

    public void freeRegion(int x, int y, int z) {
        wrapper.freeRegion(x, y, z);
    }

    public void setBlock(Vector3ic blockPosition, Block block) {
        tryRegister(block);
        setBlock(blockPosition.x(), blockPosition.y(), blockPosition.z(), block.getId());

    }
}
