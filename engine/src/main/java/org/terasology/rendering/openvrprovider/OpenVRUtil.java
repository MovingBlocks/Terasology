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

/**
 * Utility functions that don't interact with the headset (conversions and the like).
 */
final class OpenVRUtil {

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

    static Matrix4f createIdentityMatrix4f() {
        return new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }
}
