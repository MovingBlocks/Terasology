// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.engine;

import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.physics.bullet.BulletPhysics;
import org.terasology.engine.physics.bullet.shapes.BulletCollisionShapeFactory;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.physics.shapes.CollisionShapeFactory;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.gestalt.di.ServiceRegistry;

/**
 * Centralizes the various components of the physics engine. To change the physics engine used, this class
 * should ideally be the only one modified.
 */
public final class PhysicsEngineManager {

    /**
     * The {@link CollisionShapeFactory} that can be used to create a {@link CollisionShape}.
     */
    public static final CollisionShapeFactory COLLISION_SHAPE_FACTORY = new BulletCollisionShapeFactory();

    private PhysicsEngineManager() { }

    public static void registerPhysicsEngine(ServiceRegistry serviceRegistry) {
        serviceRegistry.with(BulletPhysics.class).lifetime(Lifetime.Singleton);
    }

    /**
     * Create a new {@link PhysicsEngine} instance.
     *
     * @param context The {@link Context} with which to create the engine
     * @return The created {@link PhysicsEngine} instance.
     */
    public static PhysicsEngine getNewPhysicsEngine(Context context) {
        return new BulletPhysics(context.get(BlockEntityRegistry.class));
    }
}
