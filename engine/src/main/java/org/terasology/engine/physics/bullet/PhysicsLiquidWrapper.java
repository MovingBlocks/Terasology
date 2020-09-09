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
 */
public class PhysicsLiquidWrapper implements VoxelPhysicsWorld {
    private WorldProvider world;

    public PhysicsLiquidWrapper(WorldProvider world) {
        this.world = world;
    }

    @Override
    public VoxelInfo getCollisionShapeAt(int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return new LiquidVoxelInfo(block, new Vector3i(x, y, z));
    }

    public void dispose() {
        world = null;
    }

    private static class LiquidVoxelInfo implements VoxelInfo {

        private final boolean colliding;
        private final boolean blocking;
        private final CollisionShape shape;
        private final Vector3i position;
        private final Vector3f offset;

         LiquidVoxelInfo(Block block, Vector3i position) {
            this.shape = block.getCollisionShape();
            this.offset = block.getCollisionOffset();
            this.colliding = block.isLiquid();
            this.blocking = false;
            this.position = position;
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
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float getRestitution() {
            // TODO Auto-generated method stub
            return 0;
        }
    }
}
