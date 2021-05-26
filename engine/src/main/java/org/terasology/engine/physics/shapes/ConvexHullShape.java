// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.shapes;


import org.joml.Vector3f;

/**
 * Represents a convex hull collision shape in the physics engine.
 */
public interface ConvexHullShape extends CollisionShape {
    /**
     * Returns the scaled vertices of the {@link ConvexHullShape}.
     *
     * @return An array containing the scaled vertices.
     */
    Vector3f[] getVertices();
}
