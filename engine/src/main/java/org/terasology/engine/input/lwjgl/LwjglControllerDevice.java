// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.lwjgl;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.ControllerConfig;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.input.ButtonState;
import org.terasology.input.ControllerDevice;
import org.terasology.input.ControllerId;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.device.ControllerAction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Queue;

/**
 * Retrieves information on connected controllers through GLFW.
 */
public class LwjglControllerDevice implements ControllerDevice {

    public static final int TROVE_NO_ENTRY_VALUE = -1;
    private static final Logger logger = LoggerFactory.getLogger(LwjglControllerDevice.class);
    private final ControllerConfig config;
    private final TIntHashSet joystickIds = new TIntHashSet();
    private final TIntHashSet gamepadIds = new TIntHashSet();

    private final TIntIntHashMap buttonMap = new TIntIntHashMap(12, 0.5f, TROVE_NO_ENTRY_VALUE, TROVE_NO_ENTRY_VALUE);


    private final TIntIntHashMap axisMapping = new TIntIntHashMap(5, 0.5f, TROVE_NO_ENTRY_VALUE, TROVE_NO_ENTRY_VALUE);

    public LwjglControllerDevice(ControllerConfig config) {
        this.config = config;

        for (int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_16; jid++) {
            if (GLFW.glfwJoystickPresent(jid)) {
                handleJoystickConnect(jid);
            }
        }
        GLFW.glfwSetJoystickCallback(this::onJoystickConnectDisconnect);
        updateGamepadMappings();
        setupTeraAxisMappings();
        setupTeraButtonsMapping();
    }

    private void setupTeraAxisMappings() {
        axisMapping.put(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X, ControllerId.X_AXIS);
        axisMapping.put(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y, ControllerId.Y_AXIS);
        axisMapping.put(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X, ControllerId.RX_AXIS);
        axisMapping.put(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y, ControllerId.RY_AXIS);
        axisMapping.put(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER, ControllerId.Z_AXIS);
    }


    private void setupTeraButtonsMapping() {
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_A, ControllerId.ZERO);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_B, ControllerId.ONE);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_X, ControllerId.TWO);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_Y, ControllerId.THREE);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, ControllerId.FOUR);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, ControllerId.FIVE);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_BACK, ControllerId.SIX);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_START, ControllerId.SEVEN);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_THUMB, ControllerId.EIGHT);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, ControllerId.NINE);
        buttonMap.put(GLFW.GLFW_GAMEPAD_BUTTON_GUIDE, ControllerId.TEN);
        // buttonMap.put(Component.Identifier.Button._11, ControllerId.ELEVEN) // Unknown which button use there for 
        // xbox.
    }

    private void updateGamepadMappings() {
        try (InputStream inputStream = LwjglControllerDevice.class.getResourceAsStream("/gamecontrollerdb.txt")) {
            byte[] bytes = ByteStreams.toByteArray(inputStream);
            String gamecontrollerDBContent = new String(bytes, TerasologyConstants.CHARSET);
            ByteBuffer gamecontrolleDB = MemoryUtil.memASCIISafe(gamecontrollerDBContent);
            if (!GLFW.glfwUpdateGamepadMappings(gamecontrolleDB)) {
                logger.error("Cannot update GLFW's gamepad mapping, gamepads may not work correctly");
            }
        } catch (IOException e) {
            logger.error("Cannot read resource '/gamecontrollerdb.txt', gamepads may not work correctly", e);
        }
    }

    private void onJoystickConnectDisconnect(int jid, int event) {
        if (event == GLFW.GLFW_CONNECTED) {
            handleJoystickConnect(jid);
        } else {
            handleJoystickDisconnect(jid);
        }
    }

    private void handleJoystickConnect(int jid) {
        if (GLFW.glfwJoystickIsGamepad(jid)) {
            gamepadIds.add(jid);
            logger.atInfo().log("JoyStick connected: {}", GLFW.glfwGetJoystickName(jid));
        } else {
            joystickIds.add(jid);
            logger.atInfo().log("Gamepad connected: {}", GLFW.glfwGetGamepadName(jid));
        }
    }

    private void handleJoystickDisconnect(int jid) {
        if (GLFW.glfwJoystickIsGamepad(jid)) {
            gamepadIds.remove(jid);
            logger.atInfo().log("JoyStick disconnected: {}", GLFW.glfwGetJoystickName(jid));
        } else {
            joystickIds.remove(jid);
            logger.atInfo().log("Gamepad disconnected: {}", GLFW.glfwGetGamepadName(jid));
        }
    }


    @Override
    public Queue<ControllerAction> getInputQueue() {

        Queue<ControllerAction> controllerActions = Lists.newLinkedList();
        for (int jid : gamepadIds.toArray()) {
            if (GLFW.glfwJoystickPresent(jid)) {
                // callback remove it later, if you are here
                continue;
            }
            String controllerName = GLFW.glfwGetGamepadName(jid);
            GLFWGamepadState gamepadState = GLFWGamepadState.create();

            if (GLFW.glfwGetGamepadState(jid, gamepadState)) {
                FloatBuffer axes = gamepadState.axes();
                int axesIndex = 0;
                while (axes.hasRemaining()) {

                    int teraId = axisMapping.get(axesIndex);
                    Input input = InputType.CONTROLLER_AXIS.getInput(teraId);

                    float axisValue = axes.get();
                    if (Math.abs(axisValue) < getDeadzoneForInput(controllerName, input)) {
                        axisValue = 0;
                    }

                    controllerActions.add(new ControllerAction(input, controllerName, ButtonState.UP, axisValue));
                    axesIndex++;
                }

                ByteBuffer buttonsStates = gamepadState.buttons();
                int buttonIndex = 0;
                while (buttonsStates.hasRemaining()) {

                    int teraButtonId = buttonMap.get(buttonIndex);
                    if (teraButtonId == -1) {
                        if (GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP <= buttonIndex && buttonIndex <= GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) {
                            boolean isX =
                                    (buttonIndex == GLFW.GLFW_GAMEPAD_BUTTON_DPAD_LEFT) || (buttonIndex == GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT);
                            Input input = InputType.CONTROLLER_AXIS.getInput(isX ? ControllerId.POVX_AXIS
                                                                                 : ControllerId.POVY_AXIS);
                            float axisValue;
                            if (buttonIndex == GLFW.GLFW_GAMEPAD_BUTTON_DPAD_RIGHT || buttonIndex == GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) {
                                axisValue = -1;
                            } else {
                                axisValue = 1;
                            }
                            controllerActions.add(new ControllerAction(input, controllerName, ButtonState.UP,
                                    axisValue));
                        }
                        logger.error("Received unknown/unhandled buttonId for GLFW: {}", buttonIndex);
                    } else {
                        Input input = InputType.CONTROLLER_BUTTON.getInput(teraButtonId);

                        short btnState = buttonsStates.get();
                        ButtonState buttonState = btnState == GLFW.GLFW_RELEASE ? ButtonState.UP : ButtonState.DOWN;

                        controllerActions.add(new ControllerAction(input, controllerName, buttonState, 0.0F));
                    }
                    buttonIndex++;
                }
            } else {
                logger.atError().log("Cannot get states for {}", GLFW.glfwGetGamepadName(jid));
            }

        }
        //TODO: handle this!
//        for (int jid : joystickIds.toArray()) {
//            GLFW.glfwGetJoystickAxes(jid);
//            GLFW.glfwGetJoystickButtons(jid);
//            GLFW.glfwGetJoystickHats(jid);
//        }

        return controllerActions;
    }

    private double getDeadzoneForInput(String controllerName, Input input) {
        double deadzone;
        if (input.getId() == ControllerId.RY_AXIS || input.getId() == ControllerId.RX_AXIS) {
            deadzone = config.getController(controllerName).getRotationDeadZone();
        } else {
            deadzone = config.getController(controllerName).getMovementDeadZone();
        }
        return deadzone;
    }

    @Override
    public List<String> getControllers() {
        List<String> names = Lists.newArrayList();
        for (int jid : joystickIds.toArray()) {
            names.add(GLFW.glfwGetJoystickName(jid));
        }

        for (int jid : gamepadIds.toArray()) {
            names.add(GLFW.glfwGetGamepadName(jid));
        }
        return names;
    }
}
