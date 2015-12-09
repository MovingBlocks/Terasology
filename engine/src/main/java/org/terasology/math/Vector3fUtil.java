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

import com.bulletphysics.BulletGlobals;
import org.terasology.math.geom.Vector3f;

/**
 */
public final class Vector3fUtil {
    private Vector3fUtil() {
    }


    /**
     * @return The reflection of direction against normal
     */
    public static Vector3f reflect(Vector3f direction, Vector3f normal, Vector3f out) {
        out.set(normal);
        out.scale(-2.0f * direction.dot(normal));
        out.add(direction);
        return out;
    }

    /**
     * @return the portion of direction that is parallel to normal
     */
    public static Vector3f getParallelComponent(Vector3f direction, Vector3f normal, Vector3f out) {
        out.set(normal);
        out.scale(direction.dot(normal));
        return out;
    }

    /**
     * @return the portion of direction that is perpendicular to normal
     */
    public static Vector3f getPerpendicularComponent(Vector3f direction, Vector3f normal, Vector3f out) {
        Vector3f perpendicular = getParallelComponent(direction, normal, out);
        perpendicular.scale(-1);
        perpendicular.add(direction);
        return perpendicular;
    }

    public static Vector3f safeNormalize(Vector3f v, Vector3f out) {
        out.set(v);
        out.normalize();
        if (out.length() < BulletGlobals.SIMD_EPSILON) {
            out.set(0, 0, 0);
        }
        return out;
    }

    public static Vector3f min(Vector3f a, Vector3f b, Vector3f out) {
        out.x = Math.min(a.x, b.x);
        out.y = Math.min(a.y, b.y);
        out.z = Math.min(a.z, b.z);
        return out;
    }

    public static Vector3f max(Vector3f a, Vector3f b, Vector3f out) {
        out.x = Math.max(a.x, b.x);
        out.y = Math.max(a.y, b.y);
        out.z = Math.max(a.z, b.z);
        return out;
    }
}
