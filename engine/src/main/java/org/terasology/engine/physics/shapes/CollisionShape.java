// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;

import org.terasology.joml.geom.AABBf;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

/**
 * The base type representing a collision shape in the physics engine.
 */
public interface CollisionShape {
    /**
     * Returns the axis-aligned bounding box ({@link org.terasology.joml.geom.AABBfc}) of the transformed shape.
     *
     * @return The {@link AABBf} bounding the shape.
     */
    AABBf getAABB(Vector3fc origin, Quaternionfc rotation, float scale);

    /**
     * Returns an identical shape rotated through the rotation angles represented by {@code rot}.
     *
     * @param rot The quaternion representation of the rotation.
     * @return The rotated shape.
     */
    CollisionShape rotate(Quaternionf rot);
}
