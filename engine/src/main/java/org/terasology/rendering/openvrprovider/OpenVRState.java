package org.terasology.rendering.openvrprovider;

import jopenvr.*;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/* Contains all of the information that the user will need from OpenVR without using any OpenVR data structures. The
OpenVRProvider automatically updates this.
 */
public class OpenVRState {
    public static int leftEye = JOpenVRLibrary.EVREye.EVREye_Eye_Left;
    public static int rightEye = JOpenVRLibrary.EVREye.EVREye_Eye_Right;

    // Controllers
    private static Matrix4f[] controllerPose = new Matrix4f[2];
    private static VRControllerState_t[] lastControllerState = new VRControllerState_t[2];

    private List<ControllerListener> controllerListeners = new ArrayList<>();

    // In the head frame
    private Matrix4f[] eyePoses = new Matrix4f[2];
    private Matrix4f[] projectionMatrices = new Matrix4f[2];

    // In the tracking system intertial frame
    private Matrix4f headPose = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);

    public void addControllerListener(ControllerListener toAdd) {
        controllerListeners.add(toAdd);
    }

    public OpenVRState() {
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

    public void setHeadPose(HmdMatrix34_t inputPose) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, headPose);
    }

    public void setEyePoseWRTHead(HmdMatrix34_t inputPose, int nIndex) {
        OpenVRUtil.setSteamVRMatrix3ToMatrix4f(inputPose, eyePoses[nIndex]);
    }

    public void setControllerPose(HmdMatrix34_t inputPose, int nIndex) {
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

    public void updateControllerButtonState(
            VRControllerState_t[] controllerStateReference) {
        // each controller{
        for (int c = 0; c < 2; c++) {
            // store previous state
            if (lastControllerState[c].ulButtonPressed != controllerStateReference[c].ulButtonPressed) {
                for (ControllerListener listener : controllerListeners) {
                    listener.buttonStateChanged(lastControllerState[c], controllerStateReference[c], c);
                }
            }
            lastControllerState[c].unPacketNum = controllerStateReference[c].unPacketNum;
            lastControllerState[c].ulButtonPressed = controllerStateReference[c].ulButtonPressed;
            lastControllerState[c].ulButtonTouched = controllerStateReference[c].ulButtonTouched;

            // 5 axes but only [0] and [1] is anything, trigger and touchpad
            for (int i = 0; i < 5; i++) {
                if (controllerStateReference[c].rAxis[i] != null) {
                    lastControllerState[c].rAxis[i].x = controllerStateReference[c].rAxis[i].x;
                    lastControllerState[c].rAxis[i].y = controllerStateReference[c].rAxis[i].y;
                }
            }
        }
    }

    public void setProjectionMatrix(
            HmdMatrix44_t inputPose,
            int nEye) {
        OpenVRUtil.setSteamVRMatrix44ToMatrix4f(inputPose, projectionMatrices[nEye]);
    }

}
