// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;


import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Animation Interpolation algorithms
 */
public enum GLTFInterpolation {
    /**
     * For initial value 'a' and next value 'b' and delta time 't' a + (b - a ) * t
     */
    LINEAR {
        @Override
        public void interpolate(Vector3f a, Vector3f b, float t, Vector3f out) {
            a.lerp(b, t, out);
        }

        @Override
        public void interpolate(Quaternionf a, Quaternionf b, float t, Quaternionf out) {
            a.slerp(b, t, out);
        }
    },
    /**
     * Don't interpolate, just use the previous value until past the time for the next value
     */
    STEP {
        @Override
        public void interpolate(Vector3f a, Vector3f b, float t, Vector3f out) {
            out.set(a);
        }

        @Override
        public void interpolate(Quaternionf a, Quaternionf b, float t, Quaternionf out) {
            out.set(a);
        }
    },
    /**
     * Cubic spline interpolation: NOTE: not supported, treating as step instead
     */
    CUBICSPLINE {
        @Override
        public void interpolate(Vector3f a, Vector3f b, float t, Vector3f out) {
            out.set(a);
        }

        @Override
        public void interpolate(Quaternionf a, Quaternionf b, float t, Quaternionf out) {
            out.set(a);
        }
    };


    public abstract void interpolate(Vector3f a, Vector3f b, float t, Vector3f out);

    public abstract void interpolate(Quaternionf a, Quaternionf b, float t, Quaternionf out);
}
