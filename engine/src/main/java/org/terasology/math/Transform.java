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
package org.terasology.math;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Represents a space transformation. Used for transforming points from one space to another space and
 * vice versa.
 */
public class Transform {
    /**
     * The origin vector (in global coordinates) of this transform.
     */
    public final Vector3f origin;
    /**
     * The rotation of this transform.
     */
    public final Quaternionf rotation;
    /**
     * The scale of this transform.
     */
    public float scale;

    /**
     * Creates a new {@link Transform} with the given origin, rotation, and scale.
     *
     * @param origin The origin of the transform.
     * @param rotation The rotation of the transform.
     * @param scale The scale of the transform.
     */
    public Transform(Vector3f origin, Quaternionf rotation, float scale) {
        this.origin = origin;
        this.rotation = rotation;
        this.scale = scale;
    }

    /**
     * Returns the rotation and scaling information contained in the transform as a 3x3 basis matrix.
     *
     * @return The basis matrix.
     */
    public Matrix3f getBasis() {
        return new Matrix3f()
            .set(rotation)
            .scale(scale);
    }

    /**
     * Transforms the point {@code v} into the space represented by this transform. The result is placed
     * back into {@code v}.
     * @param v The point to transform.
     */
    public void transform(Vector3f v) {
        getBasis().transform(v);
        v.add(origin);
    }
}
