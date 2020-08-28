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

import org.terasology.context.Context;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.physics.bullet.shapes.BulletCollisionShapeFactory;
import org.terasology.physics.shapes.CollisionShape;
import org.terasology.physics.shapes.CollisionShapeFactory;

/**
 * Centralizes the various components of the physics engine. To change the physics engine used, this class
 * should ideally be the only one modified.
 */
public final class PhysicsEngineManager {
    /**
     * The {@link CollisionShapeFactory} that can be used to create a {@link CollisionShape}.
     */
    public static final CollisionShapeFactory COLLISION_SHAPE_FACTORY = new BulletCollisionShapeFactory();

    /**
     * Create a new {@link PhysicsEngine} instance.
     *
     * @param context The {@link Context} with which to create the engine
     * @return The created {@link PhysicsEngine} instance.
     */
    public static PhysicsEngine getNewPhysicsEngine(Context context) {
        return new BulletPhysics();
    }
}
