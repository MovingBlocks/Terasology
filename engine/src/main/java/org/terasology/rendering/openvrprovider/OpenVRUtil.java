/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.openvrprovider;

import jopenvr.HmdMatrix34_t;
import jopenvr.HmdMatrix44_t;
import jopenvr.VRControllerState_t;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Utility functions that don't interact with the headset (conversions and the like).
 */
public final class OpenVRUtil {

    private OpenVRUtil() {
        // Not called
    }

    static void setSteamVRMatrix3ToMatrix4f(HmdMatrix34_t hmdMatrix, Matrix4f matrixToSet) {
        matrixToSet.set(
            hmdMatrix.m[0], hmdMatrix.m[4], hmdMatrix.m[8], 0,
            hmdMatrix.m[1], hmdMatrix.m[5], hmdMatrix.m[9], 0,
            hmdMatrix.m[2], hmdMatrix.m[6], hmdMatrix.m[10], 0,
            hmdMatrix.m[3], hmdMatrix.m[7], hmdMatrix.m[11], 1f
        );
    }

    static void setSteamVRMatrix44ToMatrix4f(HmdMatrix44_t hmdMatrix, Matrix4f matrixToSet) {
        matrixToSet.set(
            hmdMatrix.m[0], hmdMatrix.m[4], hmdMatrix.m[8], hmdMatrix.m[12],
            hmdMatrix.m[1], hmdMatrix.m[5], hmdMatrix.m[9], hmdMatrix.m[13],
            hmdMatrix.m[2], hmdMatrix.m[6], hmdMatrix.m[10], hmdMatrix.m[14],
            hmdMatrix.m[3], hmdMatrix.m[7], hmdMatrix.m[11], hmdMatrix.m[15]
        );
    }

    public static VRControllerState_t createZeroControllerState() {
        VRControllerState_t state = new VRControllerState_t();
        // controller not connected, clear state
        state.ulButtonPressed = 0;

        for (int i = 0; i < 5; i++) {
            if (state.rAxis[i] != null) {
                state.rAxis[i].x = 0.0f;
                state.rAxis[i].y = 0.0f;
            }
        }
        return state;
    }

    public static boolean isPressed(long nButton, long uiButtonPressed) {
        return ((uiButtonPressed & nButton) > 0);
    }

    public static boolean switchedDown(long nButton, long stateBefore, long stateAfter) {
        return (!isPressed(nButton, stateBefore) && isPressed(nButton, stateAfter));
    }

    public static boolean switchedUp(long nButton, long stateBefore, long stateAfter) {
        return (isPressed(nButton, stateBefore) && !isPressed(nButton, stateAfter));
    }

    /*
     * Takes 3 unit vectors, representing an orthonormal basis, and generates a unit quaternion.
     */
    public static Vector4f getQuaternion(boolean normalizeAxes,
                                         float xxInput, float xyInput, float xzInput,
                                         float yxInput, float yyInput, float yzInput,
                                         float zxInput, float zyInput, float zzInput) {
        float xx = xxInput;
        float xy = xyInput;
        float xz = xzInput;
        float yx = yxInput;
        float yy = yyInput;
        float yz = yzInput;
        float zx = zxInput;
        float zy = zyInput;
        float zz = zzInput;
        float x;
        float y;
        float z;
        float w;
        if (normalizeAxes) {
            final float lx = 1f / new Vector3f(xx, xy, xz).length();
            final float ly = 1f / new Vector3f(yx, yy, yz).length();
            final float lz = 1f / new Vector3f(zx, zy, zz).length();
            xx *= lx;
            xy *= lx;
            xz *= lx;
            yx *= ly;
            yy *= ly;
            yz *= ly;
            zx *= lz;
            zy *= lz;
            zz *= lz;
        }
        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        final float t = xx + yy + zz;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = (float) Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s; // so this division isn't bad
            x = (zy - yz) * s;
            y = (xz - zx) * s;
            z = (yx - xy) * s;
        } else if ((xx > yy) && (xx > zz)) {
            float s = (float) Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (yx + xy) * s;
            z = (xz + zx) * s;
            w = (zy - yz) * s;
        } else if (yy > zz) {
            float s = (float) Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (yx + xy) * s;
            z = (zy + yz) * s;
            w = (xz - zx) * s;
        } else {
            float s = (float) Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (xz + zx) * s;
            y = (zy + yz) * s;
            w = (yx - xy) * s;
        }
        return new Vector4f(x, y, z, w);
    }

    /*
     * Converts the rotation portion of a 4x4 matrix into a unit quaternion.
     */
    public static Vector4f convertToQuaternion(Matrix4f m1) {
        return getQuaternion(true,
            m1.m00(), m1.m10(), m1.m20(),
            m1.m01(), m1.m11(), m1.m21(),
            m1.m02(), m1.m12(), m1.m22()
        );
    }

    static Matrix4f createIdentityMatrix4f() {
        return new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }
}
