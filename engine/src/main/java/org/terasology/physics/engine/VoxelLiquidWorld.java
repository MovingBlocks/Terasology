// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.physics.engine;

import com.badlogic.gdx.physics.bullet.collision.VoxelCollisionAlgorithmWrapper;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btVoxelShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunks;

import static org.terasology.physics.bullet.BulletPhysics.AABB_SIZE;

public class VoxelLiquidWorld extends VoxelWorld {

    private VoxelLiquidWorld(VoxelCollisionAlgorithmWrapper wrapper, BlockManager blockManager) {
        super(wrapper, blockManager);
    }

    public static VoxelLiquidWorld create(BulletPhysics bulletPhysics, BlockManager blockManager) {
        btDiscreteDynamicsWorld discreteDynamicsWorld = bulletPhysics.getDiscreteDynamicsWorld();

        VoxelCollisionAlgorithmWrapper wrapper = new VoxelCollisionAlgorithmWrapper(Chunks.SIZE_X, Chunks.SIZE_Y,
                Chunks.SIZE_Z);
        btVoxelShape worldShape = new btVoxelShape(wrapper, new Vector3f(-AABB_SIZE, -AABB_SIZE, -AABB_SIZE),
                new Vector3f(AABB_SIZE, AABB_SIZE, AABB_SIZE));

        Matrix4f matrix4f = new Matrix4f();
        btDefaultMotionState blockMotionState = new btDefaultMotionState(matrix4f);

        btRigidBody.btRigidBodyConstructionInfo blockConsInf = new btRigidBody.btRigidBodyConstructionInfo(0,
                blockMotionState, worldShape, new Vector3f());
        btRigidBody rigidBody = new btRigidBody(blockConsInf);
        rigidBody.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT | rigidBody.getCollisionFlags()); // voxel world is added to static collision flag
        short mask = (short) StandardCollisionGroup.LIQUID.getFlag(); // interacts with liquid only
        discreteDynamicsWorld.addRigidBody(rigidBody, bulletPhysics.combineGroups(StandardCollisionGroup.LIQUID),
                mask); // adds rigid body to world
        return new VoxelLiquidWorld(wrapper, blockManager);
    }
}
