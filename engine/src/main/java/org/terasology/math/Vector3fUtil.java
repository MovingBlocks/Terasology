/*
 * Copyright 2013 MovingBlocks
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

import org.joml.Vector3f;

/**
 */
public final class Vector3fUtil {
    private Vector3fUtil() {
    }


    /**
     * @return The reflection of direction against normal
     */
    public static Vector3f reflect(Vector3f direction, Vector3f normal, Vector3f out) {
        return out.set(normal)
                .mul(-2.0f * direction.dot(normal))
                .add(direction);
    }

    /**
     * @return the portion of direction that is parallel to normal
     */
    public static Vector3f getParallelComponent(Vector3f direction, Vector3f normal, Vector3f out) {
        return out.set(normal)
                .mul(direction.dot(normal));
    }

    /**
     * @return the portion of direction that is perpendicular to normal
     */
    public static Vector3f getPerpendicularComponent(Vector3f direction, Vector3f normal, Vector3f out) {
        return getParallelComponent(direction, normal, out)
                .mul(-1)
                .add(direction);
    }

    public static Vector3f safeNormalize(Vector3f v, Vector3f out) {
        final float EPSILON = 0.000001f;

        out.set(v);
        out.normalize();
        if (out.length() < EPSILON) {
            out.zero();
        }
        return out;
    }

    public static Vector3f min(Vector3f a, Vector3f b, Vector3f out) {
        return a.min(b, out);
    }

    public static Vector3f max(Vector3f a, Vector3f b, Vector3f out) {
        return a.max(b, out);
    }
}
