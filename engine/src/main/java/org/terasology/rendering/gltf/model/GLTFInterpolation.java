/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.gltf.model;


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
