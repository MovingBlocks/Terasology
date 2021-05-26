// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;

/**
 * Represents a sphere collision shape in the physics engine.
 */
public interface SphereShape extends CollisionShape {
    /**
     * Returns the radius of the sphere shape.
     */
    float getRadius();
}
