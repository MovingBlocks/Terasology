// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;

import org.terasology.engine.math.AABB;
import org.terasology.engine.math.Transform;
import org.terasology.math.geom.Quat4f;

/**
 * The base type representing a collision shape in the physics engine.
 */
public interface CollisionShape {
    /**
     * Returns the axis-aligned bounding box ({@link AABB}) of the transformed shape.
     *
     * @param transform The {@link Transform} pertaining to the space in which the AABB is to be calculated.
     * @return The {@link AABB} bounding the shape.
     */
    AABB getAABB(Transform transform);

    /**
     * Returns an identical shape rotated through the rotation angles represented by {@code rot}.
     *
     * @param rot The quaternion representation of the rotation.
     * @return The rotated shape.
     */
    CollisionShape rotate(Quat4f rot);
}
