// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;

/**
 * Represents a compound collision shape composed of other {@link CollisionShape}s in the physics engine.
 */
public interface CompoundShape extends CollisionShape {
    /**
     * Adds a child shape to the compound shape.
     *
     * @param collisionShape The child shape.
     */
    void addChildShape(Vector3fc origin, Quaternionfc rotation, float scale, CollisionShape collisionShape);
}
