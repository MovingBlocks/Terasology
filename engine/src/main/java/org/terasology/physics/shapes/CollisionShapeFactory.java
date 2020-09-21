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
package org.terasology.physics.shapes;


import org.joml.Vector3f;

import java.util.List;

/**
 * Interface for factories creating new collision shapes.
 */
public interface CollisionShapeFactory {
    /**
     * Creates a new box shape with the given extents.
     *
     * @param extents The full extents of the box shape.
     * @return The created box shape.
     */
    BoxShape getNewBox(Vector3f extents);

    /**
     * Creates a new convex hull shape enveloping the given vertices.
     *
     * @param vertices The vertices to be enveloped by the convex hull shape.
     * @return The created convex hull shape.
     */
    ConvexHullShape getNewConvexHull(List<Vector3f> vertices);

    /**
     * Creates a new box shape with unit extents, that is a cube with sides of length 1.
     *
     * @return The created unit cube box shape.
     */
    default BoxShape getNewUnitCube() {
        return getNewBox(new Vector3f(1,1,1));
    }

    /**
     * Creates a new empty compound shape.
     *
     * @return The created empty compound shape.
     */
    CompoundShape getNewCompoundShape();

    /**
     * Creates a new sphere shape with the given radius.
     *
     * @param radius The radius of the shape.
     * @return The created sphere shape.
     */
    SphereShape getNewSphere(float radius);
}
