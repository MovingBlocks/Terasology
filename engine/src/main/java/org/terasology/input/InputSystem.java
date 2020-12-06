// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input;

import com.google.common.collect.Queues;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.config.facade.InputDeviceConfiguration;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.input.device.CharKeyboardAction;
import org.terasology.input.device.ControllerAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;
import org.terasology.input.device.RawKeyboardAction;
import org.terasology.input.device.nulldevices.NullControllerDevice;
import org.terasology.input.device.nulldevices.NullKeyboardDevice;
import org.terasology.input.device.nulldevices.NullMouseDevice;
import org.terasology.input.events.CharEvent;
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
import org.terasology.math.JomlUtil;
import org.terasology.registry.In;

import java.util.List;
import java.util.Queue;

/**
 * This system processes input, sending it out as events against the LocalPlayer entity.
 * <br><br>
 * In addition to raw keyboard and mouse input, the system handles Bind Buttons and Bind Axis, which can be mapped to
 * one or more inputs.
 */
@RegisterSystem
public class InputSystem extends BaseComponentSystem {

    @In
    private InputDeviceConfiguration inputDeviceConfig;

    @In
    private BindsManager bindsManager;

    @In
    private Time time;

    @In
    private DisplayDevice display;

    @In
    private LocalPlayer localPlayer;

    @In
    private CameraTargetSystem targetSystem;

    private MouseDevice mouse = new NullMouseDevice();
    private KeyboardDevice keyboard = new NullKeyboardDevice();
    private ControllerDevice controllers = new NullControllerDevice();

    private Queue<RawKeyboardAction> simulatedKeys = Queues.newArrayDeque();
    private Queue<CharKeyboardAction> simulatedTextInput = Queues.newArrayDeque();

    private EntityRef[] inputEntities;

    public void setKeyboardDevice(KeyboardDevice keyboardDevice) {
        this.keyboard = keyboardDevice;
    }

    public MouseDevice getMouseDevice() {
        return mouse;
    }

    public void setMouseDevice(MouseDevice mouseDevice) {
        this.mouse = mouseDevice;
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

    /**
     * Updates/processes user input across all of the players input devices.
     *
     * @param delta The length of the current frame.
     */
    public void update(float delta) {
        updateInputEntities();
        processMouseInput(delta);
        processKeyboardInput(delta);
        processControllerInput(delta);
        processBindRepeats(delta);
        processBindAxis(delta);
    }

    /**
     * Returns true if the game window currently has display focus, therefore mouse input is being captured.
     *
     * @return true if display currently has focus, else false.
     */
    public boolean isCapturingMouse() {
        return display.hasFocus();
    }

    /**
     * Updates the client and input entities of the local player, to be used in input events against the local player.
     */
    private void updateInputEntities() {
        if (inputEntities == null
                || inputEntities.length != 2
                || inputEntities[0] == null
                || inputEntities[1] == null
                || inputEntities[0] != localPlayer.getClientEntity()
                || inputEntities[1] != localPlayer.getCharacterEntity()) {
            inputEntities = new EntityRef[]{localPlayer.getClientEntity(), localPlayer.getCharacterEntity()};
        }
    }

    /**
     * Processes the current input state of the mouse, sends input events and updates bind buttons.
     * <p>
     * Mouse position actions are handled here, while mouse button and mouse wheel actions are handled at {@link
     * #processMouseButtonInput(float, MouseAction)} and {@link #processMouseWheelInput(float, MouseAction)}
     * accordingly.
     *
     * @param delta The length of the current frame.
     */
    private void processMouseInput(float delta) {
        if (!isCapturingMouse()) {
            return;
        }
        this.mouse.update();

        Vector2d deltaMouse = mouse.getDelta();
        //process mouse movement x axis
        if (deltaMouse.x != 0) {
            double xValue = deltaMouse.x * inputDeviceConfig.getMouseSensitivity();
            MouseAxisEvent event = MouseAxisEvent.create(MouseAxis.X, xValue, delta);
            send(event);
        }

        //process mouse movement y axis
        if (deltaMouse.y != 0) {
            double yMovement = inputDeviceConfig.isMouseYAxisInverted() ? deltaMouse.y * -1 : deltaMouse.y;
            double yValue = yMovement * inputDeviceConfig.getMouseSensitivity();
            MouseAxisEvent event = MouseAxisEvent.create(MouseAxis.Y, yValue, delta);
            send(event);
        }

        //process mouse clicks
        for (MouseAction action : mouse.getInputQueue()) {
            switch (action.getInput().getType()) {
                case MOUSE_BUTTON:
                    processMouseButtonInput(delta, action);
                    break;
                case MOUSE_WHEEL:
                    processMouseWheelInput(delta, action);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Processes input actions by the mouse buttons, sends input events and updates bind buttons accordingly.
     *
     * @param delta The length of the current frame.
     * @param action The input action to be processed.
     */
    private void processMouseButtonInput(float delta, MouseAction action) {
        int id = action.getInput().getId();
        if (id != MouseInput.NONE.getId()) {
            MouseInput button = MouseInput.find(action.getInput().getType(), action.getInput().getId());
            boolean consumed = sendMouseEvent(button, action.getState().isDown(), action.getMousePosition(), delta);
            BindableButton bind = bindsManager.getMouseButtonBinds().get(button);
            if (bind != null) {
                updateBindState(bind, action.getInput(), action.getState().isDown(), delta, consumed);
            }
        }
    }

    /**
     * Processes input actions by the mouse wheel, sends input events and updates bind buttons accordingly.
     *
     * @param delta The length of the current frame.
     * @param action The input action to be processed.
     */
    private void processMouseWheelInput(float delta, MouseAction action) {
        int dir = action.getInput().getId();
        if (dir != 0 && action.getTurns() != 0) {
            boolean consumed = sendMouseWheelEvent(action.getMousePosition(), dir * action.getTurns(), delta);
            BindableButton bind = (dir == 1) ? bindsManager.getMouseWheelUpBind() :
                    bindsManager.getMouseWheelDownBind();
            if (bind != null) {
                for (int i = 0; i < action.getTurns(); ++i) {
                    updateBindState(bind, action.getInput(), true, delta, consumed);
                    updateBindState(bind, action.getInput(), false, delta, consumed);
                }
            }
        }
    }

    /**
     * Processes the current input state of any connected controllers, and updates bind buttons.
     * <p>
     * Controller button and axis events are both handled in {@link #processControllerButtonInput(float,
     * ControllerAction, boolean, Input)} and {@link #processControllerAxisInput(ControllerAction, Input)} accordingly.
     *
     * @param delta The length of the current frame.
     */
    private void processControllerInput(float delta) {
        if (!isCapturingMouse()) {
            return;
        }
        for (ControllerAction action : controllers.getInputQueue()) {
            // TODO: send event to entity system
            boolean consumed = false;

            Input input = action.getInput();
            if (input.getType() == InputType.CONTROLLER_BUTTON) {
                processControllerButtonInput(delta, action, consumed, input);
            } else if (input.getType() == InputType.CONTROLLER_AXIS) {
                processControllerAxisInput(action, input);
            }
        }
    }

    /**
     * Processes input actions by controller buttons, and updates bind buttons accordingly.
     *
     * @param delta The length of the current frame.
     * @param action The input action to be processed.
     * @param consumed True if sent input event has been processed/consumed by an event receiver.
     * @param input The specific input of the controller button.
     */
    private void processControllerButtonInput(float delta, ControllerAction action, boolean consumed, Input input) {
        BindableButton bind = bindsManager.getControllerBinds().get(input);
        if (bind != null) {
            boolean pressed = action.getState() == ButtonState.DOWN;
            updateBindState(bind, input, pressed, delta, consumed);
        }
    }

    /**
     * Processes input actions by controller axis, and updates bind axis accordingly.
     *
     * @param action The input action to be processed.
     * @param input The specific input of the controller axis.
     */
    private void processControllerAxisInput(ControllerAction action, Input input) {
        BindableRealAxis axis = bindsManager.getControllerAxisBinds().get(input);
        if (axis != null) {
            ControllerInfo info = inputDeviceConfig.getController(action.getController());
            boolean isX = action.getInput().getId() == ControllerId.X_AXIS;
            boolean isY = action.getInput().getId() == ControllerId.Y_AXIS;
            boolean isZ = action.getInput().getId() == ControllerId.Z_AXIS;
            float f = (isX && info.isInvertX() || isY && info.isInvertY() || isZ && info.isInvertZ()) ? -1 : 1;
            axis.setTargetValue(action.getAxisValue() * f);
        }
    }

    /**
     * Updates the bind state of a button bind based on provided input information.
     *
     * @param bind The button bind to be updated.
     * @param input The specific input to be binded.
     * @param pressed True if the button in the input is pressed, false if not.
     * @param delta The length of the current frame.
     * @param consumed True if the input event has been processed/consumed by an event receiver.
     */
    private void updateBindState(BindableButton bind, Input input, boolean pressed, float delta, boolean consumed) {
        bind.updateBindState(
                input,
                pressed,
                delta, inputEntities,
                targetSystem.getTarget(),
                targetSystem.getTargetBlockPosition(),
                targetSystem.getHitPosition(),
                targetSystem.getHitNormal(),
                consumed,
                time.getGameTimeInMs());
    }

    public void simulateTextInput(String text) {
        text.chars()
                .mapToObj(intChar -> (char) intChar)
                .map(CharKeyboardAction::new)
                .forEach(simulatedTextInput::add);
    }

    /**
     * Simulates a single key stroke from the keyboard.
     * <p>
     * Simulated key strokes: To simulate input from a keyboard, we simply have to extract the Input associated to the
     * action and this function adds it to the keyboard's input queue.
     *
     * @param key The key to be simulated.
     */
    public void simulateSingleKeyStroke(Input key) {
        /* TODO: Perhaps there is a better way to extract the character.
            All the simulate functions extract keyChar by getting the first character from it's display string.
            While it works for normal character buttons, might not work for special buttons if required later.
        */
        RawKeyboardAction action = new RawKeyboardAction(key, ButtonState.DOWN);
        simulatedKeys.add(action);
    }

    /**
     * Simulates a repeated key stroke from the keyboard.
     * <p>
     * Simulated key strokes: To simulate input from a keyboard, we simply have to extract the Input associated to the
     * action and this function adds it to the keyboard's input queue.
     *
     * @param key The key to be simulated.
     */
    public void simulateRepeatedKeyStroke(Input key) {
        RawKeyboardAction action = new RawKeyboardAction(key, ButtonState.REPEAT);
        simulatedKeys.add(action);
    }

    /**
     * Cancels the simulation of key strokes.
     *
     * @param key The key to cancel the simulation of.
     */
    public void cancelSimulatedKeyStroke(Input key) {
        RawKeyboardAction action = new RawKeyboardAction(key, ButtonState.UP);
        simulatedKeys.add(action);
    }

    /**
     * Processes input actions by keyboard buttons, sends key events and updates bind buttons accordingly.
     *
     * @param delta The length of the current frame.
     */
    private void processKeyboardInput(float delta) {
        Queue<RawKeyboardAction> keyQueue = keyboard.getInputQueue();
        keyQueue.addAll(simulatedKeys);
        simulatedKeys.clear();
        for (RawKeyboardAction action : keyQueue) {
            boolean consumed = sendKeyEvent(action.getInput(), action.getState(), delta);

            // Update bind
            BindableButton bind = bindsManager.getKeyBinds().get(action.getInput().getId());
            if (bind != null && action.getState() != ButtonState.REPEAT) {
                boolean pressed = action.getState() == ButtonState.DOWN;
                updateBindState(bind, action.getInput(), pressed, delta, consumed);
            }
        }
        Queue<CharKeyboardAction> charQueue = keyboard.getCharInputQueue();
        charQueue.addAll(simulatedTextInput);
        simulatedTextInput.clear();
        charQueue.forEach((action) -> sendCharEvent(action.getCharacter(), delta));
    }


    /**
     * Processes/Updates all bind axis.
     *
     * @param delta The length of the current frame.
     */
    private void processBindAxis(float delta) {
        for (AbstractBindableAxis axis : bindsManager.getAxisBinds()) {
            axis.update(inputEntities, delta, targetSystem.getTarget(),
                    targetSystem.getTargetBlockPosition(),
                    targetSystem.getHitPosition(),
                    targetSystem.getHitNormal());
        }
    }

    /**
     * Processes/Updates all bind buttons.
     *
     * @param delta The length of the current frame.
     */
    private void processBindRepeats(float delta) {
        for (BindableButton button : bindsManager.getButtonBinds()) {
            button.update(inputEntities,
                    delta,
                    targetSystem.getTarget(),
                    targetSystem.getTargetBlockPosition(),
                    targetSystem.getHitPosition(),
                    targetSystem.getHitNormal(),
                    time.getGameTimeInMs());
        }
    }

    /**
     * Creates and sends an input event based on a provided raw keyboard input.
     *
     * @param key The specific input to be sent.
     * @param state The state of the input key.
     * @param delta The length of the current frame.
     * @return true if the event has been consumed by an event listener, false otherwise.
     */
    private boolean sendKeyEvent(Input key, ButtonState state, float delta) {
        KeyEvent event;
        switch (state) {
            case UP:
                event = KeyUpEvent.create(key, delta);
                break;
            case DOWN:
                event = KeyDownEvent.create(key, delta);
                break;
            case REPEAT:
                event = KeyRepeatEvent.create(key, delta);
                break;
            default:
                return false;
        }

        boolean consumed = send(event);
        event.reset();
        return consumed;
    }

    /**
     * Creates and sends an input event based on a provided text keyboard input.
     *
     * @param character character for send
     * @param delta The length of the current frame.
     */
    private void sendCharEvent(char character, float delta) {
        CharEvent event = CharEvent.create(character, delta);
        send(event);
        event.reset();
    }

    /**
     * Creates and sends an input event based on a provided mouse action.
     *
     * @param button The specific input to be sent.
     * @param buttonDown True if the button is pressed, false if not.
     * @param delta The length of the current frame.
     * @return True if the event has been consumed by an event listener, false otherwise.
     */
    private boolean sendMouseEvent(MouseInput button, boolean buttonDown, Vector2i position, float delta) {
        MouseButtonEvent event;
        switch (button) {
            case NONE:
                return false;
            case MOUSE_LEFT:
                event = (buttonDown) ? LeftMouseDownButtonEvent.create(position, delta) :
                        LeftMouseUpButtonEvent.create(position, delta);
                break;
            case MOUSE_RIGHT:
                event = (buttonDown) ? RightMouseDownButtonEvent.create(position, delta) :
                        RightMouseUpButtonEvent.create(position, delta);
                break;
            default:
                event = (buttonDown) ? MouseDownButtonEvent.create(button, position, delta) :
                        MouseUpButtonEvent.create(button, position, delta);
                break;
        }
        boolean consumed = send(event);
        event.reset();
        return consumed;
    }

    /**
     * Creates and sends an input event based on a provided mouse wheel action.
     *
     * @param pos The position of the mouse.
     * @param wheelTurns The number of times the scroll wheel has turned.
     * @param delta The length of the current frame.
     * @return True if the event has been consumed by an event listener, false otherwise.
     */
    private boolean sendMouseWheelEvent(Vector2i pos, int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(pos,
                wheelTurns, delta);
        return send(mouseWheelEvent);
    }

    /**
     * Sends a provided input event to the local player's input entities.
     *
     * @param event The input event to be sent.
     * @return True if the event has been consumed by an event listener, false otherwise.
     */
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

    /**
     * Sets up the target info for a specified input event.
     *
     * @param event The specified input event.
     */
    private void setupTarget(InputEvent event) {
        if (targetSystem.isTargetAvailable()) {
            event.setTargetInfo(targetSystem.getTarget(),
                    targetSystem.getTargetBlockPosition(),
                    targetSystem.getHitPosition(),
                    targetSystem.getHitNormal());
        }
    }

    /**
     * Drop all pending/unprocessed input events.
     */
    public void drainQueues() {
        mouse.getInputQueue();
        keyboard.getInputQueue();
        keyboard.getCharInputQueue();
        controllers.getInputQueue();
    }

    /**
     * API-exposed caller to {@link BindsManager#getBindsConfig()} and
     * {@link org.terasology.config.BindsConfig#getBinds(SimpleUri)}.
     * <p>
     * TODO: Restored for API reasons, may be duplicating code elsewhere. Should be reviewed.
     *
     * @param bindId the ID.
     * @return a list of keyboard/mouse inputs that trigger the binding.
     */
    public List<Input> getInputsForBindButton(SimpleUri bindId) {
        return bindsManager.getBindsConfig().getBinds(bindId);
    }
}
