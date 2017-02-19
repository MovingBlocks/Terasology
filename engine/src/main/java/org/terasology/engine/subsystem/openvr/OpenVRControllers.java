/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.subsystem.openvr;

import jopenvr.VRControllerState_t;
import org.joml.Matrix4f;
import org.terasology.input.ButtonState;
import org.terasology.input.ControllerDevice;
import org.terasology.input.InputType;
import org.terasology.input.device.ControllerAction;
import org.terasology.rendering.openvrprovider.ControllerListener;
import org.terasology.rendering.openvrprovider.OpenVRUtil;
import org.terasology.input.ControllerId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class OpenVRControllers implements ControllerDevice, ControllerListener {

    private Queue<ControllerAction> queuedActions = new ArrayDeque<>();

    /**
     * Get the controller names provided by this ControllerDevice.
     * @return the list of controllers names.
     */
    @Override
    public List<String> getControllers() {
        List<String> ids = new ArrayList<>();
        ids.add("OpenVR");
        return ids;
    }

    /**
     * Get all queued actions registered since this method was last called.
     * @return a queue of actions.
     */
    @Override
    public Queue<ControllerAction> getInputQueue() {
        Queue<ControllerAction> result = new ArrayDeque<>();
        result.addAll(queuedActions);
        queuedActions.clear();
        return result;
    }

    private void handleController0(VRControllerState_t stateBefore, VRControllerState_t stateAfter) {
        if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_TRIGGER, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.ZERO),
                    "OpenVR", ButtonState.UP, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_TRIGGER, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.ZERO),
                    "OpenVR", ButtonState.DOWN, 1.0f));
        } else if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_GRIP, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.ONE),
                    "OpenVR", ButtonState.UP, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_GRIP, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.ONE),
                    "OpenVR", ButtonState.DOWN, 1.0f));
        } else if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_APP_MENU, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.TWO),
                    "OpenVR", ButtonState.UP, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_APP_MENU, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.TWO),
                    "OpenVR", ButtonState.DOWN, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_TOUCHPAD, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.X_AXIS),
                    "OpenVR", ButtonState.DOWN, -stateAfter.rAxis[0].x));
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.Y_AXIS),
                    "OpenVR", ButtonState.DOWN, stateAfter.rAxis[0].y));
        } else if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_TOUCHPAD, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.X_AXIS),
                    "OpenVR", ButtonState.UP, 0));
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.Y_AXIS),
                    "OpenVR", ButtonState.UP, 0));
        }
    }

    private void handleController1(VRControllerState_t stateBefore, VRControllerState_t stateAfter) {
        if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_TRIGGER, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.THREE),
                    "OpenVR", ButtonState.UP, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_TRIGGER, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.THREE),
                    "OpenVR", ButtonState.DOWN, 1.0f));
        } else if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_GRIP, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.FOUR),
                    "OpenVR", ButtonState.UP, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_GRIP, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.FOUR),
                    "OpenVR", ButtonState.DOWN, 1.0f));
        } else if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_APP_MENU, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.FIVE),
                    "OpenVR", ButtonState.UP, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_APP_MENU, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            queuedActions.add(new ControllerAction(InputType.CONTROLLER_BUTTON.getInput(ControllerId.FIVE),
                    "OpenVR", ButtonState.DOWN, 1.0f));
        } else if (OpenVRUtil.switchedDown(ControllerListener.BUTTON_TOUCHPAD, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            if (stateAfter.rAxis[0].x < 0 && stateAfter.rAxis[0].y < 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.SIX),
                        "OpenVR", ButtonState.DOWN, 1.0f));
            }
            if (stateAfter.rAxis[0].x > 0 && stateAfter.rAxis[0].y < 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.SEVEN),
                        "OpenVR", ButtonState.DOWN, 1.0f));
            }
            if (stateAfter.rAxis[0].x < 0 && stateAfter.rAxis[0].y < 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.EIGHT),
                        "OpenVR", ButtonState.DOWN, 1.0f));
            }
            if (stateAfter.rAxis[0].x > 0 && stateAfter.rAxis[0].y > 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.NINE),
                        "OpenVR", ButtonState.DOWN, 1.0f));
            }
        } else if (OpenVRUtil.switchedUp(ControllerListener.BUTTON_TOUCHPAD, stateBefore.ulButtonPressed, stateAfter.ulButtonPressed)) {
            if (stateAfter.rAxis[0].x < 0 && stateAfter.rAxis[0].y < 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.SIX),
                        "OpenVR", ButtonState.UP, 1.0f));
            }
            if (stateAfter.rAxis[0].x > 0 && stateAfter.rAxis[0].y < 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.SEVEN),
                        "OpenVR", ButtonState.UP, 1.0f));
            }
            if (stateAfter.rAxis[0].x < 0 && stateAfter.rAxis[0].y < 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.EIGHT),
                        "OpenVR", ButtonState.UP, 1.0f));
            }
            if (stateAfter.rAxis[0].x > 0 && stateAfter.rAxis[0].y > 0) {
                queuedActions.add(new ControllerAction(InputType.CONTROLLER_AXIS.getInput(ControllerId.NINE),
                        "OpenVR", ButtonState.UP, 1.0f));
            }
        }
    }

    /**
     * Called whenever the OpenVR controller button state changes for a given controller (left or right).
     * @param stateBefore - the state before the last change.
     * @param stateAfter - the state after the last change.
     * @param handIndex - the hand index, an integer - 0 for left, 1 for right.
     */
    @Override
    public void buttonStateChanged(VRControllerState_t stateBefore, VRControllerState_t stateAfter, int handIndex) {
        if (handIndex == 0) {
            handleController0(stateBefore, stateAfter);
        } else {
            handleController1(stateBefore, stateAfter);
        }
    }

    /**
     * Called whenever the OpenVR controller pose changes for a given controller (left or right). This particular
     * listener just ignores pose updates.
     * @param pose - the pose of the controller (a 4x4 matrix).
     * @param handIndex - the hand index, an integer - 0 for left, 1 for right.
     */
    @Override
    public void poseChanged(Matrix4f pose, int handIndex) {
        // currently no actions are sensitive to controller movement
    }

}
