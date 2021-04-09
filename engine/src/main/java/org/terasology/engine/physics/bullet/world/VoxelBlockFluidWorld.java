// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet.world;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import org.terasology.engine.physics.bullet.BulletPhysics;
import org.terasology.engine.physics.bullet.shapes.BulletCollisionShape;
import org.terasology.engine.world.block.Block;

public class VoxelBlockFluidWorld extends BulletVoxelBlockWorld {
    private final boolean[] registered = new boolean[Short.MAX_VALUE];

    public VoxelBlockFluidWorld(BulletPhysics physics) {
        super(physics);
    }

    @Override
    public void registerBlock(Block block) {
        short id = block.getId();
        if (!registered[id]) {
            btCollisionShape shape = ((BulletCollisionShape) block.getCollisionShape()).underlyingShape;
            btVoxelInfo info = new btVoxelInfo(shape != null && block.isLiquid(),
                    shape != null && block.isLiquid(), id, shape, block.getCollisionOffset(),
                    block.getFriction(), block.getRestitution(), block.getFriction());
            wrapper.setVoxelInfo(info);
            registered[id] = true;
        }
    }
}
