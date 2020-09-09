// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;

import org.terasology.engine.math.Transform;

/**
 * Represents a compound collision shape composed of other {@link CollisionShape}s in the physics engine.
 */
public interface CompoundShape extends CollisionShape {
    /**
     * Adds a child shape to the compound shape.
     *
     * @param transform The space transformation of the child shape relative to the compound shape.
     * @param collisionShape The child shape.
     */
    void addChildShape(Transform transform, CollisionShape collisionShape);
}
