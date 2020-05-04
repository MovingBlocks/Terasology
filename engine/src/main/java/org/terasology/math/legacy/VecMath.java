/*
 * Copyright 2020 MovingBlocks
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

package org.terasology.math.legacy;

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

/**
 * Some utilities for compatibility with VecMath.
 * @deprecated this class is schedules for removal in an upcoming version
 *             Use the JOML implementation instead: {@link org.terasology.math.VecMath}
 */
@Deprecated
public final class VecMath {

    private VecMath() {
        // no instances
    }

    public static Vector3f from(javax.vecmath.Vector3f v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    public static javax.vecmath.Vector3f to(Vector3f v) {
        return new javax.vecmath.Vector3f(v.x, v.y, v.z);
    }

    public static Quat4f from(javax.vecmath.Quat4f v) {
        return new Quat4f(v.x, v.y, v.z, v.w);
    }

    public static javax.vecmath.Quat4f to(Quat4f v) {
        return new javax.vecmath.Quat4f(v.x, v.y, v.z, v.w);
    }

}
