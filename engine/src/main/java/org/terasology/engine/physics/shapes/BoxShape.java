// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;

import org.joml.Vector3f;

/**
 * Represents a box collision shape in the physics engine.
 */
public interface BoxShape extends CollisionShape {
    /**
     * Returns the extents (size) of the box shape in every dimension.
     *
     * @return The {@link Vector3f} containing the dimensional extents of the box shape.
     */
    Vector3f getHalfExtentsWithoutMargin();

}
