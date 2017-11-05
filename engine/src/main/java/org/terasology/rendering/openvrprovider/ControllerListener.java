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

import jopenvr.JOpenVRLibrary;
import jopenvr.VRControllerState_t;
import org.joml.Matrix4f;

/**
 * Interface intended to be front-facing to user for controller interaction.
 */
public interface ControllerListener {
    int LEFT_CONTROLLER = 0;
    int RIGHT_CONTROLLER = 1;
    int EAXIS_TRIGGER = 1;
    int EAXIS_TOUCHPAD = 0;
    long TOUCHPAD_BUTTON = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_SteamVR_Touchpad);
    long TRIGGER_BUTTON = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_SteamVR_Trigger);
    long APP_MENU_BUTTON = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_ApplicationMenu);
    long GRIP_BUTTON = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_Grip);
    float TRIGGER_THRESHOLD = .25f;

    /**
     * Override this method with a handler for whenever the state of the OpenVR controller changes.
     * @param stateBefore - the controller state before the change.
     * @param stateAfter - the controller state after the change.
     * @param handIndex - the hand index of the affected controller, an integer. 0 for the left hand, 1 for the right.
     */
    void buttonStateChanged(VRControllerState_t stateBefore, VRControllerState_t stateAfter, int handIndex);

    /**
     * Override this method with a handler for whenever the pose of the OpenVR controller changes.
     * @param pose - the pose of the controller at the point of update, a 4x4 homogenous transformation matrix.
     * @param handIndex - the hand index of the affected controller, an integer. 0 for the left hand, 1 for the right.
     */
    void poseChanged(Matrix4f pose, int handIndex);
}
