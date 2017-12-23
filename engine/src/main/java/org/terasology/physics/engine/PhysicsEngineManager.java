/*
 * Copyright 2017 MovingBlocks
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

import com.bulletphysics.BulletGlobals;
import org.terasology.context.Context;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.physics.bullet.shapes.BulletCollisionShapeFactory;
import org.terasology.physics.shapes.CollisionShapeFactory;
import org.terasology.world.WorldProvider;

public final class PhysicsEngineManager {
    public static final CollisionShapeFactory COLLISION_SHAPE_FACTORY = new BulletCollisionShapeFactory();
    public static final float EPSILON = BulletGlobals.SIMD_EPSILON;

    public static PhysicsEngine getNewPhysicsEngine(Context context) {
        return new BulletPhysics(context.get(WorldProvider.class));
    }
}
