// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet.world;

import com.badlogic.gdx.physics.bullet.collision.VoxelCollisionAlgorithmWrapper;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import com.badlogic.gdx.physics.bullet.collision.btVoxelShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.physics.bullet.BulletPhysics;
import org.terasology.engine.physics.bullet.shapes.BulletCollisionShape;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;

import java.nio.ShortBuffer;

import static org.terasology.engine.physics.bullet.BulletPhysics.AABB_SIZE;

public class VoxelBlockWorld implements VoxelWorld {
    private final boolean[] registered = new boolean[Short.MAX_VALUE];
    private final VoxelCollisionAlgorithmWrapper wrapper;
    private final btRigidBody.btRigidBodyConstructionInfo blockConsInf;
    private final btVoxelShape worldShape;
    private final btRigidBody rigidBody;

    public VoxelBlockWorld(BulletPhysics physics) {
        btDiscreteDynamicsWorld discreteDynamicsWorld = ((BulletPhysics) physics).getDiscreteDynamicsWorld();

        wrapper = new VoxelCollisionAlgorithmWrapper(Chunks.SIZE_X, Chunks.SIZE_Y,
                Chunks.SIZE_Z);
        worldShape = new btVoxelShape(wrapper, new Vector3f(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE),
                new Vector3f(AABB_SIZE, AABB_SIZE, AABB_SIZE));

        Matrix4f matrix4f = new Matrix4f();
        btDefaultMotionState blockMotionState = new btDefaultMotionState(matrix4f);

        blockConsInf = new btRigidBody.btRigidBodyConstructionInfo(0, blockMotionState, worldShape, new Vector3f());
        rigidBody = new btRigidBody(blockConsInf);
        rigidBody.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | rigidBody.getCollisionFlags()); // voxel world is added to static collision flag
        short mask = (short) (~(StandardCollisionGroup.STATIC.getFlag() | StandardCollisionGroup.LIQUID.getFlag())); // interacts with anything but static and liquid
        discreteDynamicsWorld.addRigidBody(rigidBody, physics.combineGroups(StandardCollisionGroup.WORLD), mask); // adds rigid body to world

    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        registerBlock(block);
        wrapper.setBlock(x, y, z, block.getId());
    }

    @Override
    public void registerBlock(Block block) {
        short id = block.getId();
        if (!registered[id]) {
            btCollisionShape shape = ((BulletCollisionShape) block.getCollisionShape()).underlyingShape;
            btVoxelInfo info = new btVoxelInfo(shape != null && block.isTargetable(),
                    shape != null && !block.isPenetrable(), id, shape, block.getCollisionOffset(),
                    block.getFriction(), block.getRestitution(), block.getFriction());
            wrapper.setVoxelInfo(info);
            registered[id] = true;
        }
    }

    @Override
    public void loadChunk(Chunk chunk, ShortBuffer buffer) {
        Vector3ic chunkPos = chunk.getPosition();
        wrapper.setRegion(chunkPos.x(), chunkPos.y(), chunkPos.z(), buffer);
    }

    @Override
    public void unloadChunk(Vector3ic position) {
        wrapper.freeRegion(position.x(), position.y(), position.z());
    }
}
