package openvrprovider;

import jopenvr.JOpenVRLibrary;
import jopenvr.VRControllerState_t;

/* Interface intended to be front-facing to user for controller interaction. */
public interface ControllerListener {
    public final int LEFT_CONTROLLER = 0;
    public final int RIGHT_CONTROLLER = 1;
    public static int k_EAxis_Trigger = 1;
    public static int k_EAxis_TouchPad = 0;
    public static long k_buttonTouchpad = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_SteamVR_Touchpad);
    public static long k_buttonTrigger = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_SteamVR_Trigger);
    public static long k_buttonAppMenu = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_ApplicationMenu);
    public static long k_buttonGrip = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_Grip);
    public static float triggerThreshold = .25f;

    public void buttonStateChanged(VRControllerState_t stateBefore, VRControllerState_t stateAfter, int nController);
    // TODO: touch, axes
}
