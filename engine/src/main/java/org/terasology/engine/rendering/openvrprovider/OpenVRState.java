// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.openvrprovider;

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
    private float groundPlaneYOffset;

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

    /**
     * Add a controller listener. This listener will receive pose and button state updates for the controller.
     * @param listener - An object implementing the ControllerListener interface.
     */
    public void addControllerListener(ControllerListener listener) {
        controllerListeners.add(listener);
    }

    /**
     * Removes a controller listener.
     * @param listener - An object implementing the ControllerListener interface.
     */
    public void removeControllerListener(ControllerListener listener) {
        controllerListeners.remove(listener);
    }

    /**
     * Get the pose of an eye.
     * @param eyeIndex - An integer specifying the eye: 0 for the left eye, 1 for the right eye.
     * @return the pose, as a Matrix4f
     */
    public Matrix4f getEyePose(int eyeIndex) {
        Matrix4f matrixReturn = new Matrix4f(headPose);
        matrixReturn.mul(eyePoses[eyeIndex]);
        return matrixReturn;
    }

    /**
     * Get the projection matrix for an eye.
     * @param eyeIndex - An integer specifying the eye: 0 for the left eye, 1 for the right eye.
     * @return the projection matrix, as a Matrix4f.
     */
    public Matrix4f getEyeProjectionMatrix(int eyeIndex) {
        return new Matrix4f(projectionMatrices[eyeIndex]);
    }

    void setHeadPose(HmdMatrix34_t inputPose) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, headPose);
        headPose = new Matrix4f(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, groundPlaneYOffset, 0, 1
        ).mul(headPose);
    }

    void setEyePoseWRTHead(HmdMatrix34_t inputPose, int nIndex) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, eyePoses[nIndex]);
    }

    void setControllerPose(HmdMatrix34_t inputPose, int nIndex) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, controllerPose[nIndex]);
        controllerPose[nIndex] = new Matrix4f(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, groundPlaneYOffset, 0, 1
        ).mul(controllerPose[nIndex]);
        for (ControllerListener listener : controllerListeners) {
            listener.poseChanged(controllerPose[nIndex], nIndex);
        }
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

    /**
     * Set the offset of the default head pose from the ground (along the y axis). This is useful if there is some
     * built-in "rest" camera position (i.e. some games will default to the height of a standing player).
     * @param inputOffset - the height (in meters) by which to raise the camera.
     */
    public void setGroundPlaneYOffset(float inputOffset) {
        groundPlaneYOffset = inputOffset;
    }

    void setProjectionMatrix(
            HmdMatrix44_t inputPose,
            int eyeIndex) {
        OpenVRUtil.setSteamVRMatrix44ToMatrix4f(inputPose, projectionMatrices[eyeIndex]);
    }

}
