// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.math;

import org.joml.Vector3fc;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

/**
 * Some utilities for compatibility with VecMath.
 */
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

    public static javax.vecmath.Vector3f to(Vector3fc v) {
        return new javax.vecmath.Vector3f(v.x(), v.y(), v.z());
    }

    public static Quat4f from(javax.vecmath.Quat4f v) {
        return new Quat4f(v.x, v.y, v.z, v.w);
    }

    public static javax.vecmath.Quat4f to(Quat4f v) {
        return new javax.vecmath.Quat4f(v.x, v.y, v.z, v.w);
    }

}
