/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.math;

import com.bulletphysics.linearmath.QuaternionUtil;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public final class Quat4fUtil {

    public static final Quat4f IDENTITY = new Quat4f(0, 0, 0, 1);

    private Quat4fUtil() {
    }

    public static Quat4f fromAngles(float rotX, float rotY, float rotZ) {
        Quat4f quat = new Quat4f();
        Quat4f qZ = new Quat4f();
        QuaternionUtil.setRotation(qZ, new Vector3f(0, 0, 1), rotZ);
        Quat4f qY = new Quat4f();
        QuaternionUtil.setRotation(qY, new Vector3f(0, 1, 0), rotY);
        Quat4f qX = new Quat4f();
        QuaternionUtil.setRotation(qX, new Vector3f(1, 0, 0), rotX);
        quat.set(1, 0, 0, 0);
        quat.mul(qZ);
        quat.mul(qY);
        quat.mul(qX);
        return quat;
    }
}
