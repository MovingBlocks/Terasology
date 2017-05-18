/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.input;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.input.device.ControllerAction;
import org.terasology.input.device.KeyboardAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;
import org.terasology.input.device.nulldevices.NullControllerDevice;
import org.terasology.input.device.nulldevices.NullKeyboardDevice;
import org.terasology.input.device.nulldevices.NullMouseDevice;
import org.terasology.input.events.InputEvent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.KeyRepeatEvent;
import org.terasology.input.events.KeyUpEvent;
import org.terasology.input.events.LeftMouseDownButtonEvent;
import org.terasology.input.events.LeftMouseUpButtonEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseAxisEvent.MouseAxis;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseDownButtonEvent;
import org.terasology.input.events.MouseUpButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.RightMouseDownButtonEvent;
import org.terasology.input.events.RightMouseUpButtonEvent;
import org.terasology.input.internal.AbstractBindableAxis;
import org.terasology.input.internal.BindableRealAxis;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;

import java.util.Queue;

/**
 * This system processes input, sending it out as events against the LocalPlayer entity.
 * <br><br>
 * In addition to raw keyboard and mouse input, the system handles Bind Buttons and Bind Axis, which can be mapped
 * to one or more inputs.
 */
public class InputSystem extends BaseComponentSystem {

    @In
    private Config config;

    @In
    private BindsManager bindsManager;

    @In
    private DisplayDevice display;

    @In
    private LocalPlayer localPlayer;

    @In
    private CameraTargetSystem targetSystem;

    private MouseDevice mouse = new NullMouseDevice();
    private KeyboardDevice keyboard = new NullKeyboardDevice();
    private ControllerDevice controllers = new NullControllerDevice();

    private Logger logger = LoggerFactory.getLogger(InputSystem.class);

    private Queue<KeyboardAction> simulatedKeys = Queues.newArrayDeque();

    private EntityRef[] inputEntities;

    public void setMouseDevice(MouseDevice mouseDevice) {
        this.mouse = mouseDevice;
    }

    public void setKeyboardDevice(KeyboardDevice keyboardDevice) {
        this.keyboard = keyboardDevice;
    }

    public MouseDevice getMouseDevice() {
        return mouse;
    }

    public KeyboardDevice getKeyboard() {
        return keyboard;
    }

    public ControllerDevice getControllerDevice() {
        return controllers;
    }

    public void setControllerDevice(ControllerDevice controllerDevice) {
        this.controllers = controllerDevice;
    }

    @Override
    public void initialise() {
        bindsManager.registerBinds();
    }

    public void update(float delta) {
        updateInputEntities();
        processMouseInput(delta);
        processKeyboardInput(delta);
        processControllerInput(delta);
        processBindRepeats(delta);
        processBindAxis(delta);
    }

    private void updateInputEntities() {
        inputEntities = new EntityRef[] {localPlayer.getClientEntity(), localPlayer.getCharacterEntity()};
    }

    private void processMouseInput(float delta) {
        if (!display.hasFocus()) {
            return;
        }

        Vector2i deltaMouse = mouse.getDelta();
        //process mouse movement x axis
        if (deltaMouse.x != 0) {
            MouseAxisEvent event = MouseAxisEvent.create(MouseAxis.X, deltaMouse.x * config.getInput().getMouseSensitivity(), delta);
            send(event);
        }

        //process mouse movement y axis
        if (deltaMouse.y != 0) {
            int yMovement = config.getInput().isMouseYAxisInverted() ? deltaMouse.y * -1 : deltaMouse.y;
            MouseAxisEvent event = MouseAxisEvent.create(MouseAxis.Y, yMovement * config.getInput().getMouseSensitivity(), delta);
            send(event);
        }

        //process mouse clicks
        for (MouseAction action : mouse.getInputQueue()) {
            switch (action.getInput().getType()) {
                case MOUSE_BUTTON:
                    int id = action.getInput().getId();
                    if (id != MouseInput.NONE.getId()) {
                        MouseInput button = MouseInput.find(action.getInput().getType(), action.getInput().getId());
                        boolean consumed = sendMouseEvent(button, action.getState().isDown(), action.getMousePosition(), delta);

                        BindableButton bind = bindsManager.getMouseButtonBinds().get(button);
                        if (bind != null) {
                            updateBindState(bind, action.getInput(), action.getState().isDown(), delta, consumed);
                        }
                    }
                    break;
                case MOUSE_WHEEL:
                    int dir = action.getInput().getId();
                    if (dir != 0 && action.getTurns() != 0) {
                        boolean consumed = sendMouseWheelEvent(action.getMousePosition(), dir * action.getTurns(), delta);

                        BindableButton bind = (dir == 1) ? bindsManager.getMouseWheelUpBind() : bindsManager.getMouseWheelDownBind();
                        if (bind != null) {
                            for (int i = 0; i < action.getTurns(); ++i) {
                                updateBindState(bind, action.getInput(), true, delta, consumed);
                                updateBindState(bind, action.getInput(), false, delta, consumed);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void processControllerInput(float delta) {
        if (!display.hasFocus()) {
            return;
        }
        for (ControllerAction action : controllers.getInputQueue()) {
            // TODO: send event to entity system
            boolean consumed = false;

            // Update button bind
            Input input = action.getInput();
            if (input.getType() == InputType.CONTROLLER_BUTTON) {
                BindableButton bind = bindsManager.getControllerBinds().get(input);
                if (bind != null) {
                    boolean pressed = action.getState() == ButtonState.DOWN;
                    updateBindState(bind, input, pressed, delta, consumed);
                }
            } else if (input.getType() == InputType.CONTROLLER_AXIS) {
                BindableRealAxis axis = bindsManager.getControllerAxisBinds().get(input);
                if (axis != null) {
                    ControllerInfo info = config.getInput().getControllers().getController(action.getController());
                    boolean isX = action.getInput().getId() == ControllerId.X_AXIS;
                    boolean isY = action.getInput().getId() == ControllerId.Y_AXIS;
                    boolean isZ = action.getInput().getId() == ControllerId.Z_AXIS;
                    float f = (isX && info.isInvertX() || isY && info.isInvertY() || isZ && info.isInvertZ()) ? -1 : 1;
                    axis.setTargetValue(action.getAxisValue() * f);
                }
            }
        }
    }

    private void updateBindState(BindableButton bind, Input input, boolean pressed, float delta, boolean consumed) {
        bind.updateBindState(
                input,
                pressed,
                delta, inputEntities,
                targetSystem.getTarget(),
                targetSystem.getTargetBlockPosition(),
                targetSystem.getHitPosition(),
                targetSystem.getHitNormal(),
                consumed);
    }

    /**
     * Simulated key strokes: To simulate input from a keyboard, we simply have to extract the Input associated to the action
     * and this function adds it to the keyboard's input queue.
     * @param key The key to be simulated.
     */
    public void simulateSingleKeyStroke(Input key) {
        /* TODO: Perhaps there is a better way to extract the character.
            All the simulate functions extract keyChar by getting the first character from it's display string.
            While it works for normal character buttons, might not work for special buttons if required later.
        */
        char keyChar = key.getDisplayName().charAt(0);
        KeyboardAction action = new KeyboardAction(key, ButtonState.DOWN, keyChar);
        simulatedKeys.add(action);
    }

    public void simulateRepeatedKeyStroke(Input key) {
        char keyChar = key.getDisplayName().charAt(0);
        KeyboardAction action = new KeyboardAction(key, ButtonState.REPEAT, keyChar);
        simulatedKeys.add(action);
    }

    public void cancelSimulatedKeyStroke(Input key) {
        char keyChar = key.getDisplayName().charAt(0);
        KeyboardAction action = new KeyboardAction(key, ButtonState.UP, keyChar);
        simulatedKeys.add(action);
    }

    private void processKeyboardInput(float delta) {
        Queue<KeyboardAction> keyQueue = keyboard.getInputQueue();
        keyQueue.addAll(simulatedKeys);
        simulatedKeys.clear();
        for (KeyboardAction action : keyQueue) {
            boolean consumed = sendKeyEvent(action.getInput(), action.getInputChar(), action.getState(), delta);

            // Update bind
            BindableButton bind = bindsManager.getKeyBinds().get(action.getInput().getId());
            if (bind != null && action.getState() != ButtonState.REPEAT) {
                boolean pressed = action.getState() == ButtonState.DOWN;
                updateBindState(bind, action.getInput(), pressed, delta, consumed);
            }
        }
    }

    private void processBindAxis(float delta) {
        for (AbstractBindableAxis axis : bindsManager.getAxisBinds()) {
            axis.update(inputEntities, delta, targetSystem.getTarget(), targetSystem.getTargetBlockPosition(),
                    targetSystem.getHitPosition(), targetSystem.getHitNormal());
        }
    }

    private void processBindRepeats(float delta) {
        for (BindableButton button : bindsManager.getButtonBinds()) {
            button.update(inputEntities, delta, targetSystem.getTarget(), targetSystem.getTargetBlockPosition(),
                    targetSystem.getHitPosition(), targetSystem.getHitNormal());
        }
    }

    private boolean sendKeyEvent(Input key, char keyChar, ButtonState state, float delta) {
        KeyEvent event;
        switch (state) {
            case UP:
                event = KeyUpEvent.create(key, keyChar, delta);
                break;
            case DOWN:
                event = KeyDownEvent.create(key, keyChar, delta);
                break;
            case REPEAT:
                event = KeyRepeatEvent.create(key, keyChar, delta);
                break;
            default:
                return false;
        }

        boolean consumed = send(event);
        event.reset();
        return consumed;
    }

    private boolean sendMouseEvent(MouseInput button, boolean buttonDown, Vector2i position, float delta) {
        MouseButtonEvent event;
        switch (button) {
            case NONE:
                return false;
            case MOUSE_LEFT:
                event = (buttonDown) ? LeftMouseDownButtonEvent.create(position, delta) : LeftMouseUpButtonEvent.create(position, delta);
                break;
            case MOUSE_RIGHT:
                event = (buttonDown) ? RightMouseDownButtonEvent.create(position, delta) : RightMouseUpButtonEvent.create(position, delta);
                break;
            default:
                event = (buttonDown) ? MouseDownButtonEvent.create(button, position, delta) : MouseUpButtonEvent.create(button, position, delta);
                break;
        }
        boolean consumed = send(event);
        event.reset();
        return consumed;
    }

    private boolean sendMouseWheelEvent(Vector2i pos, int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(pos, wheelTurns, delta);
        return send(mouseWheelEvent);
    }

    private boolean send(InputEvent event) {
        setupTarget(event);
        for (EntityRef entity : inputEntities) {
            entity.send(event);
            if (event.isConsumed()) {
                break;
            }
        }
        return event.isConsumed();
    }

    private void setupTarget(InputEvent event) {
        if (targetSystem.isTargetAvailable()) {
            event.setTargetInfo(targetSystem.getTarget(), targetSystem.getTargetBlockPosition(), targetSystem.getHitPosition(), targetSystem.getHitNormal());
        }
    }

    /**
     * Drop all pending/unprocessed input events.
     */
    public void drainQueues() {
        mouse.getInputQueue();
        keyboard.getInputQueue();
        controllers.getInputQueue();
    }
}
