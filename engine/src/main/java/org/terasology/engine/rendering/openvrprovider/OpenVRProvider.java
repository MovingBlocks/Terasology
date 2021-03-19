// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.openvrprovider;

import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import jopenvr.HmdMatrix34_t;
import jopenvr.HmdMatrix44_t;
import jopenvr.JOpenVRLibrary;
import jopenvr.JOpenVRLibrary.EVREventType;
import jopenvr.Texture_t;
import jopenvr.TrackedDevicePose_t;
import jopenvr.VRControllerState_t;
import jopenvr.VRTextureBounds_t;
import jopenvr.VR_IVRCompositor_FnTable;
import jopenvr.VR_IVROverlay_FnTable;
import jopenvr.VR_IVRSettings_FnTable;
import jopenvr.VR_IVRSystem_FnTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.utilities.NativeHelper;

import java.nio.IntBuffer;

/**
 * This class is designed to make all API calls to OpenVR, thereby insulating it from the user. If you're looking to get
 * some information from the headset/controllers you should probably look at OpenVRStereoRenderer, ControllerListener,
 * or OpenVRState
 */
public final class OpenVRProvider {
    public static Texture_t[] texType = new Texture_t[2];

    private static boolean initialized;
    private static final Logger logger = LoggerFactory.getLogger(OpenVRProvider.class);
    private static VR_IVRSystem_FnTable vrSystem;
    private static VR_IVRCompositor_FnTable vrCompositor;
    private static VR_IVROverlay_FnTable vrOverlay;
    private static VR_IVRSettings_FnTable vrSettings;
    private static int[] controllerDeviceIndex = new int[2];
    private static VRControllerState_t.ByReference[] inputStateRefernceArray = new VRControllerState_t.ByReference[2];
    private static VRControllerState_t[] controllerStateReference = new VRControllerState_t[2];
    private static IntBuffer hmdErrorStore;
    private static TrackedDevicePose_t.ByReference hmdTrackedDevicePoseReference;
    private static TrackedDevicePose_t[] hmdTrackedDevicePoses;
    private static boolean[] controllerTracking = new boolean[2];

    //keyboard
    private static boolean keyboardShowing;
    private static boolean headIsTracking;
    private static OpenVRProvider instance;

    private static final OpenVRState VR_STATE = new OpenVRState();

    // TextureIDs of framebuffers for each eye
    private final VRTextureBounds_t texBounds = new VRTextureBounds_t();
    private float nearClip = 0.5f;
    private float farClip = 500.0f;

    private OpenVRProvider() {
    }

    // Get a singleton instance.
    /**
    * As a general rule, we should use this class as a singleton, because multiple instantiation
    * will likely cause problems in the upstream native library. This provides a convenient method
    * of using OpenVRProvider as a singleton.
     */
    public static OpenVRProvider getInstance() {
        if (instance == null) {
            instance = new OpenVRProvider();
        }
        return instance;
    }

    /**
     * Get the state of the VR system. This contains the poses of the eyes, controllers, etc...
     * @return the VR state.
     */
    public OpenVRState getState() {
        return VR_STATE;
    }

    /**
     * Initialize the VR system. Note that calling this method will cause OpenVR to launch. If there is no headset
     * connected, or if the OpenVR library fails to initialize for some reason, this will return false, and a log
     * entry about why initialization failed will be written.
     * @return true if successful.
     */
    public boolean init() {
        for (int handIndex = 0; handIndex < 2; handIndex++) {
            controllerDeviceIndex[handIndex] = -1;
            controllerStateReference[handIndex] = new VRControllerState_t();
            inputStateRefernceArray[handIndex] = new VRControllerState_t.ByReference();
            inputStateRefernceArray[handIndex].setAutoRead(false);
            inputStateRefernceArray[handIndex].setAutoWrite(false);
            inputStateRefernceArray[handIndex].setAutoSynch(false);
            texType[handIndex] = new Texture_t();
        }
        if (!initializeOpenVRLibrary()) {
            logger.warn("JOpenVR library loading failed.");
            return false;
        }
        if (!initializeJOpenVR()) {
            logger.warn("JOpenVR initialization failed.");
            return false;
        }
        int initAttempts = 0;
        boolean initSuccess = false;

        // OpenVR has a race condition here - it is necessary
        // to initialize the overlay, but certain operations
        // that appear to take place outside of the main thread
        // seem to prevent that from happening if it's done too
        // soon after OpenVR is initialized. This loop waits a
        // reasonable amount of time and makes several attempts.
        // In my testing, it works all of the time.
        while (!initSuccess && initAttempts < 10) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            initSuccess = initOpenVRCompositor(true);
            initAttempts++;
        }
        if (!initOpenVROverlay()) {
            logger.warn("VROverlay initialization failed.");
            return false;
        }
        if (!initOpenVROSettings()) {
            logger.warn("OpenVR settings initialization failed.");
            return false;
        }
        initialized = true;
        return true;
    }

    /**
     *
     * @return true if initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * In some instances, OpenVR will lose tracking on the head set. For example, if the line of sight to both light
     * houses is obstructed, it is impossible to track the head set. In this case, the head set cannot be reliably
     * tracked. In such cases, this method will return false, signaling that the head set tracking information returned
     * by getEyePose() is unreliable.
     *
     * @return true if the pose of the headset is currently considered reliable.
     */
    public boolean isHeadTracking() {
        return headIsTracking;
    }

    /**
     *
     * @param controllerIndex - 0 for left, 1 for right, an integer.
     * @return true if the pose of the controller is currently considered reliable.
     */
    public boolean isControllerTrackint(int controllerIndex) {
        return controllerTracking[controllerIndex];
    }

    /**
     * Shut down the VR system.
     */
    public void shutdown() {
        JOpenVRLibrary.VR_ShutdownInternal();
        vrSystem = null;
        vrCompositor = null;
        vrOverlay = null;
        vrSettings = null;
        initialized = false;
    }

    /**
     * Query the VR library and update the VR state, which can then be retrieved via getState().
     * This method should be called once per frame.
     */
    public void updateState() {
        updatePose();
        pollControllers();
        pollInputEvents();
    }

    /**
     * Make the specified controller vibrate
     * @param controller - the hand index, 0 for left and 1 for right, an integer.
     * @param strength - the strength of the pulse - a short value from 0 - 3999.
     */
    public static void triggerHapticPulse(int controller, int strength) {
        if (controllerDeviceIndex[controller] == -1) {
            return;
        }
        vrSystem.TriggerHapticPulse.apply(controllerDeviceIndex[controller], 0, (short) strength);
    }

    /**
     * Submit the frame stored in the frame buffers for the left and right eyes to the compositor. When this method is
     * called, the contents of those frame buffers will show up in the head set. This method should be called exactly
     * once per frame.
     */
    public void submitFrame() {
        for (int nEye = 0; nEye < 2; nEye++) {
            vrCompositor.Submit.apply(
                    nEye,
                    texType[nEye], null,
                    JOpenVRLibrary.EVRSubmitFlags.EVRSubmitFlags_Submit_Default);
        }
        if (vrCompositor.PostPresentHandoff != null) {
            vrCompositor.PostPresentHandoff.apply();
        }
    }

    /**
     * Set the distance of the camera from the near clipping plane, in OpenGL units, as a float.
     * vrProvider.getState().getProjectionMatrix(...) method.
     * @param nearClipIn - the near clip to set.
     */
    public void setNearClip(float nearClipIn) {
        this.nearClip = nearClipIn;
    }

    /**
     * Set the distance of the camera from the far clipping plane, in OpenGL units, as a float.
     * vrProvider.getState().getProjectionMatrix(...) method.
     * @param farClipIn - the near clip to set.
     */
    public void setFarClip(float farClipIn) {
        this.farClip = farClipIn;
    }

    /**
     * Turn on the keyboard overlay. This is a keyboard that hovers in front of the user, that can be typed upon by
     * pointing the ray extending from the top of the controller at the key the user wants to press.
     * @param showingState - true or false
     * @return - true if successful. If this call fails, an error is logged.
     */
    public static boolean setKeyboardOverlayShowing(boolean showingState) {
        int ret;
        if (showingState) {
            Pointer pointer = new Memory(3);
            pointer.setString(0, "mc");
            Pointer empty = new Memory(1);
            empty.setString(0, "");
            ret = vrOverlay.ShowKeyboard.apply(0, 0, pointer, 256, empty, (byte) 1, 0);
            keyboardShowing = 0 == ret; //0 = no error, > 0 see EVROverlayError
            if (ret != 0) {
                logger.error("VR Overlay Error: " + vrOverlay.GetOverlayErrorNameFromEnum.apply(ret).getString(0));
            }
        } else {
            try {
                vrOverlay.HideKeyboard.apply();
            } catch (Error e) {
                logger.error("Error bringing up keyboard overlay: " + e.toString());
            }
            keyboardShowing = false;
        }

        return keyboardShowing;
    }

    private void pollControllers() {
        for (int handIndex = 0; handIndex < 2; handIndex++) {
            if (controllerDeviceIndex[handIndex] != -1) {
                vrSystem.GetControllerState.apply(controllerDeviceIndex[handIndex], inputStateRefernceArray[handIndex]);
                inputStateRefernceArray[handIndex].read();
                controllerStateReference[handIndex] = inputStateRefernceArray[handIndex];
                VR_STATE.updateControllerButtonState(controllerStateReference);
            }
        }
    }

    private boolean initializeOpenVRLibrary() {
        if (initialized) {
            return true;
        }
        logger.info("Adding OpenVR search path: " + NativeHelper.getOpenVRLibPath());
        NativeLibrary.addSearchPath("openvr_api", NativeHelper.getOpenVRLibPath());

        if (jopenvr.JOpenVRLibrary.VR_IsHmdPresent() != 1) {
            logger.info("VR Headset not detected.");
            return false;
        }
        logger.info("VR Headset detected.");
        return true;
    }

    private static boolean initializeJOpenVR() {
        hmdErrorStore = IntBuffer.allocate(1);
        vrSystem = null;
        JOpenVRLibrary.VR_InitInternal(hmdErrorStore, JOpenVRLibrary.EVRApplicationType.EVRApplicationType_VRApplication_Scene);
        if (hmdErrorStore.get(0) == 0) {
            // ok, try and get the vrSystem pointer..
            vrSystem = new VR_IVRSystem_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSystem_Version, hmdErrorStore));
        }
        if (vrSystem == null || hmdErrorStore.get(0) != 0) {
            String errorString = jopenvr.JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(hmdErrorStore.get(0)).getString(0);
            logger.info("vrSystem initialization failed:" + errorString);
            return false;
        } else {
            vrSystem.setAutoSynch(false);
            vrSystem.read();

            logger.info("OpenVR initialized & VR connected.");

            hmdTrackedDevicePoseReference = new TrackedDevicePose_t.ByReference();
            hmdTrackedDevicePoses = (TrackedDevicePose_t[]) hmdTrackedDevicePoseReference.toArray(JOpenVRLibrary.k_unMaxTrackedDeviceCount);

            // disable all this stuff which kills performance
            hmdTrackedDevicePoseReference.setAutoRead(false);
            hmdTrackedDevicePoseReference.setAutoWrite(false);
            hmdTrackedDevicePoseReference.setAutoSynch(false);
            for (int i = 0; i < JOpenVRLibrary.k_unMaxTrackedDeviceCount; i++) {
                hmdTrackedDevicePoses[i].setAutoRead(false);
                hmdTrackedDevicePoses[i].setAutoWrite(false);
                hmdTrackedDevicePoses[i].setAutoSynch(false);
            }
        }
        return true;
    }

    // needed for in-game keyboard
    private static boolean initOpenVROverlay() {
        vrOverlay = new VR_IVROverlay_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVROverlay_Version, hmdErrorStore));
        if (hmdErrorStore.get(0) == 0) {
            vrOverlay.setAutoSynch(false);
            vrOverlay.read();
            logger.info("OpenVR Overlay initialized OK.");
        } else {
            String errorString = jopenvr.JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(hmdErrorStore.get(0)).getString(0);
            logger.info("vrOverlay initialization failed:" + errorString);
            return false;
        }
        return true;
    }

    private static boolean initOpenVROSettings() {
        vrSettings = new VR_IVRSettings_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSettings_Version, hmdErrorStore));
        if (hmdErrorStore.get(0) == 0) {
            vrSettings.setAutoSynch(false);
            vrSettings.read();
            logger.info("OpenVR Settings initialized OK.");
        } else {
            String errorString = jopenvr.JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(hmdErrorStore.get(0)).getString(0);
            logger.info("OpenVROSettings initialization failed:" + errorString);
            return false;
        }
        return true;
    }

    private boolean initOpenVRCompositor(boolean set) {
        if (set && vrSystem != null) {
            vrCompositor = new VR_IVRCompositor_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRCompositor_Version, hmdErrorStore));
            if (hmdErrorStore.get(0) == 0) {
                logger.info("OpenVR Compositor initialized OK.");
                vrCompositor.setAutoSynch(false);
                vrCompositor.read();
                vrCompositor.SetTrackingSpace.apply(JOpenVRLibrary.ETrackingUniverseOrigin.ETrackingUniverseOrigin_TrackingUniverseStanding);
            } else {
                String errorString = jopenvr.JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(hmdErrorStore.get(0)).getString(0);
                logger.info("vrCompositor initialization failed:" + errorString);
                return false;
            }
        }
        if (vrCompositor == null) {
            logger.info("Skipping VR Compositor...");
        }

        // left eye
        texBounds.uMax = 1f;
        texBounds.uMin = 0f;
        texBounds.vMax = 1f;
        texBounds.vMin = 0f;
        texBounds.setAutoSynch(false);
        texBounds.setAutoRead(false);
        texBounds.setAutoWrite(false);
        texBounds.write();
        // texture type
        for (int nEye = 0; nEye < 2; nEye++) {
            texType[0].eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
            texType[0].eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
            texType[0].setAutoSynch(false);
            texType[0].setAutoRead(false);
            texType[0].setAutoWrite(false);
            texType[0].handle = -1;
            texType[0].write();
        }
        logger.info("OpenVR Compositor initialized OK.");
        return true;
    }

    private static void findControllerDevices() {
        controllerDeviceIndex[ControllerListener.RIGHT_CONTROLLER] = -1;
        controllerDeviceIndex[ControllerListener.LEFT_CONTROLLER] = -1;

        controllerDeviceIndex[ControllerListener.RIGHT_CONTROLLER] =
                vrSystem.GetTrackedDeviceIndexForControllerRole.apply(
                        JOpenVRLibrary.ETrackedControllerRole.ETrackedControllerRole_TrackedControllerRole_LeftHand);
        controllerDeviceIndex[ControllerListener.LEFT_CONTROLLER] =
                vrSystem.GetTrackedDeviceIndexForControllerRole.apply(
                        JOpenVRLibrary.ETrackedControllerRole.ETrackedControllerRole_TrackedControllerRole_RightHand);
    }

    private static void pollInputEvents() {
        jopenvr.VREvent_t event = new jopenvr.VREvent_t();

        while (vrSystem.PollNextEvent.apply(event, event.size()) > 0) {

            switch (event.eventType) {
                case EVREventType.EVREventType_VREvent_KeyboardClosed:
                    //'huzzah'
                    keyboardShowing = false;
                    break;
                case EVREventType.EVREventType_VREvent_KeyboardCharInput:
                    break;
                default:
                    break;
            }
        }
    }

    private void updatePose() {
        vrCompositor.WaitGetPoses.apply(hmdTrackedDevicePoseReference, JOpenVRLibrary.k_unMaxTrackedDeviceCount, null, 0);
        for (int nDevice = 0; nDevice < JOpenVRLibrary.k_unMaxTrackedDeviceCount; ++nDevice) {
            hmdTrackedDevicePoses[nDevice].read();
        }

        if (hmdTrackedDevicePoses[JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd].bPoseIsValid != 0) {
            for (int nEye = 0; nEye < 2; nEye++) {
                HmdMatrix34_t matPose = vrSystem.GetEyeToHeadTransform.apply(nEye);
                VR_STATE.setEyePoseWRTHead(matPose, nEye);
                HmdMatrix44_t matProjection =
                        vrSystem.GetProjectionMatrix.apply(nEye,
                                nearClip,
                                farClip,
                                JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL);
                VR_STATE.setProjectionMatrix(matProjection, nEye);
            }
            VR_STATE.setHeadPose(hmdTrackedDevicePoses[JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd].mDeviceToAbsoluteTracking);
            headIsTracking = true;
        } else {
            headIsTracking = false;
        }

        findControllerDevices();

        for (int handIndex = 0; handIndex < 2; handIndex++) {
            if (controllerDeviceIndex[handIndex] != -1) {
                controllerTracking[handIndex] = true;
                VR_STATE.setControllerPose(hmdTrackedDevicePoses[controllerDeviceIndex[handIndex]].mDeviceToAbsoluteTracking, handIndex);
            } else {
                controllerTracking[handIndex] = false;
            }
        }
    }
}
