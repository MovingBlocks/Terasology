/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.physics.engine;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btVoxelContentProvider;
import com.badlogic.gdx.physics.bullet.collision.btVoxelInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * This class links Terasology's voxel world with the physics engine, providing it with the collision information for each block location.
 *
 */


public class PhysicsWorldWrapper extends btVoxelContentProvider {
    private static final Logger logger = LoggerFactory.getLogger(BulletPhysics.class);

    private WorldProvider world;

    public PhysicsWorldWrapper(WorldProvider world)
    {
        super();
        this.world = world;
    }

    @Override
    public void getVoxel(int x, int y, int z, btVoxelInfo info) {
        Block block = world.getBlock(x, y, z);
        btCollisionShape shape =  block.getCollisionShape();
        btVector3 offset = new btVector3(block.getCollisionOffset().x,block.getCollisionOffset().y,block.getCollisionOffset().z);

        info.setTracable(shape != null && block.isTargetable());
        info.setBlocking(shape != null && !block.isPenetrable());
        info.setVoxelTypeId(block.getId());
        info.setX(x);
        info.setY(y);
        info.setZ(z);
        info.setCollisionShape(block.getCollisionShape());
        info.setCollisionOffset(offset);
        info.setFriction(0);
        info.setRestitution(0);
        info.setRollingFriction(0);
        info.dispose();
        offset.dispose();
    }

}
