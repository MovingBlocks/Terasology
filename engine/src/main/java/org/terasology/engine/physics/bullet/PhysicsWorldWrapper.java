// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet;

import org.terasology.engine.physics.bullet.shapes.BulletCollisionShape;
import org.terasology.engine.physics.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.voxel.VoxelInfo;
import com.bulletphysics.collision.shapes.voxel.VoxelPhysicsWorld;
import org.terasology.engine.math.VecMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;

/**
 * This class links Terasology's voxel world with the physics engine, providing it with the collision information for each block location.
 *
 */
public class PhysicsWorldWrapper implements VoxelPhysicsWorld {

    private WorldProvider world;

    public PhysicsWorldWrapper(WorldProvider world) {
        this.world = world;
    }

    @Override
    public VoxelInfo getCollisionShapeAt(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return new TeraVoxelInfo(block, block.isTargetable(), !block.isPenetrable(), new Vector3i(x, y, z));
    }

    public void dispose() {
        world = null;
    }

    private static class TeraVoxelInfo implements VoxelInfo {

        private final boolean colliding;
        private final boolean blocking;
        private final CollisionShape shape;
        private final Vector3i position;
        private final Vector3f offset;
        private final float friction;
        private final float restitution;

         TeraVoxelInfo(Block block, boolean colliding, boolean blocking, Vector3i position) {
            this.shape = block.getCollisionShape();
            this.offset = block.getCollisionOffset();
            this.colliding = shape != null && colliding;
            this.blocking = shape != null && blocking;
            this.position = position;
            this.friction = block.getFriction();
            this.restitution = block.getRestitution();
        }

        @Override
        public boolean isColliding() {
            return colliding;
        }

        @Override
        public Object getUserData() {
            return position;
        }

        @Override
        public com.bulletphysics.collision.shapes.CollisionShape getCollisionShape() {
            return ((BulletCollisionShape) shape).underlyingShape;
        }

        @Override
        public javax.vecmath.Vector3f getCollisionOffset() {
            return VecMath.to(offset);
        }

        @Override
        public boolean isBlocking() {
            return blocking;
        }

        @Override
        public float getFriction() {
             return friction;
        }

        @Override
        public float getRestitution() {
            return restitution;
        }
    }
}
