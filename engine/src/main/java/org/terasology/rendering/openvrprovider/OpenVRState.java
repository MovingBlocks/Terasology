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
import jopenvr.VRControllerAxis_t;
import jopenvr.VRControllerState_t;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Contains all of the information that the user will need from OpenVR without using any OpenVR data structures. The
OpenVRProvider automatically updates this.
 */
public class OpenVRState {

    // Controllers
    private static Matrix4f[] controllerPose = new Matrix4f[2];
    private static VRControllerState_t[] lastControllerState = new VRControllerState_t[2];

    private List<ControllerListener> controllerListeners = new ArrayList<>();

    // In the head frame
    private Matrix4f[] eyePoses = new Matrix4f[2];
    private Matrix4f[] projectionMatrices = new Matrix4f[2];

    // In the tracking system intertial frame
    private Matrix4f headPose = OpenVRUtil.createIdentityMatrix4f();

    OpenVRState() {
        for (int handIndex = 0; handIndex < 2; handIndex++) {
            lastControllerState[handIndex] = new VRControllerState_t();
            controllerPose[handIndex] = OpenVRUtil.createIdentityMatrix4f();
            eyePoses[handIndex] = OpenVRUtil.createIdentityMatrix4f();
            projectionMatrices[handIndex] = OpenVRUtil.createIdentityMatrix4f();

            for (int i = 0; i < 5; i++) {
                lastControllerState[handIndex].rAxis[i] = new VRControllerAxis_t();
            }
        }
    }

    public void addControllerListener(ControllerListener toAdd) {
        controllerListeners.add(toAdd);
    }

    void setHeadPose(HmdMatrix34_t inputPose) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, headPose);
    }

    void setEyePoseWRTHead(HmdMatrix34_t inputPose, int nIndex) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, eyePoses[nIndex]);
    }

    void setControllerPose(HmdMatrix34_t inputPose, int nIndex) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, controllerPose[nIndex]);
    }

    public Matrix4f getEyePose(int nEye) {
        Matrix4f matrixReturn = new Matrix4f(headPose);
        matrixReturn.mul(eyePoses[nEye]);
        return matrixReturn;
    }

    public Matrix4f getEyeProjectionMatrix(int nEye) {
        return new Matrix4f(projectionMatrices[nEye]);
    }

    void updateControllerButtonState(
            VRControllerState_t[] controllerStateReference) {
        // each controller{
        for (int handIndex = 0; handIndex < 2; handIndex++) {
            // store previous state
            if (lastControllerState[handIndex].ulButtonPressed != controllerStateReference[handIndex].ulButtonPressed) {
                for (ControllerListener listener : controllerListeners) {
                    listener.buttonStateChanged(lastControllerState[handIndex], controllerStateReference[handIndex], handIndex);
                }
            }
            lastControllerState[handIndex].unPacketNum = controllerStateReference[handIndex].unPacketNum;
            lastControllerState[handIndex].ulButtonPressed = controllerStateReference[handIndex].ulButtonPressed;
            lastControllerState[handIndex].ulButtonTouched = controllerStateReference[handIndex].ulButtonTouched;

            // 5 axes but only [0] and [1] is anything, trigger and touchpad
            for (int i = 0; i < 5; i++) {
                if (controllerStateReference[handIndex].rAxis[i] != null) {
                    lastControllerState[handIndex].rAxis[i].x = controllerStateReference[handIndex].rAxis[i].x;
                    lastControllerState[handIndex].rAxis[i].y = controllerStateReference[handIndex].rAxis[i].y;
                }
            }
        }
    }

    void setProjectionMatrix(
            HmdMatrix44_t inputPose,
            int nEye) {
        OpenVRUtil.setSteamVRMatrix44ToMatrix4f(inputPose, projectionMatrices[nEye]);
    }

}
