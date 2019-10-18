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

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

/**
 * Animation Interpolation algorithms
 */
public enum GLTFInterpolation {
    /**
     * For initial value 'a' and next value 'b' and delta time 't'
     * a + (b - a ) * t
     */
    LINEAR {
        @Override
        public void interpolate(Vector3f a, Vector3f b, float t, Vector3f out) {
            out.x = a.x + t * (b.x - a.x);
            out.y = a.y + t * (b.y - a.y);
            out.z = a.z + t * (b.z - a.z);

        }

        @Override
        public void interpolate(Quat4f a, Quat4f b, float t, Quat4f out) {
            float cos = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
            float absCos = Math.abs(cos);
            float scale0;
            float scale1;
            if (1.0f - absCos > 1E-6f) {
                float sinSqr = 1.0f - absCos * absCos;
                float sinom = (float) (1.0 / Math.sqrt(sinSqr));
                float omega = (float) Math.atan2(sinSqr * sinom, absCos);
                scale0 = (float) (Math.sin((1.0 - t) * omega) * sinom);
                scale1 = (float) (Math.sin(t * omega) * sinom);
            } else {
                scale0 = 1.0f - t;
                scale1 = t;
            }
            scale1 = cos >= 0.0f ? scale1 : -scale1;
            out.x = scale0 * a.x + scale1 * b.x;
            out.y = scale0 * a.y + scale1 * b.y;
            out.z = scale0 * a.z + scale1 * b.z;
            out.w = scale0 * a.w + scale1 * b.w;
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
        public void interpolate(Quat4f a, Quat4f b, float t, Quat4f out) {
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
        public void interpolate(Quat4f a, Quat4f b, float t, Quat4f out) {
            out.set(a);
        }
    };


    public abstract void interpolate(Vector3f a, Vector3f b, float t, Vector3f out);

    public abstract void interpolate(Quat4f a, Quat4f b, float t, Quat4f out);
}
