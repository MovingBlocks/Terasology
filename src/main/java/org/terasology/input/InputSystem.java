/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.config.Config;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.In;
import org.terasology.game.CoreRegistry;
import org.terasology.game.TerasologyEngine;
import org.terasology.input.events.InputEvent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.KeyRepeatEvent;
import org.terasology.input.events.KeyUpEvent;
import org.terasology.input.events.LeftMouseDownButtonEvent;
import org.terasology.input.events.LeftMouseUpButtonEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseDownButtonEvent;
import org.terasology.input.events.MouseUpButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.input.events.RightMouseDownButtonEvent;
import org.terasology.input.events.RightMouseUpButtonEvent;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;

import java.util.List;
import java.util.Map;

/**
 * This system processes input, sending it out as events against the LocalPlayer entity.
 * <p/>
 * In addition to raw keyboard and mouse input, the system handles Bind Buttons and Bind Axis, which can be mapped
 * to one or more inputs.
 */
public class InputSystem implements EventHandlerSystem {

    @In
    private Config config;

    private Map<String, BindableAxisImpl> axisLookup = Maps.newHashMap();
    private Map<String, BindableButtonImpl> buttonLookup = Maps.newHashMap();

    private List<BindableAxisImpl> axisBinds = Lists.newArrayList();
    private List<BindableButtonImpl> buttonBinds = Lists.newArrayList();

    // Links between primitive inputs and bind buttons
    private Map<Integer, BindableButtonImpl> keyBinds = Maps.newHashMap();
    private Map<Integer, BindableButtonImpl> mouseButtonBinds = Maps.newHashMap();
    private BindableButtonImpl mouseWheelUpBind;
    private BindableButtonImpl mouseWheelDownBind;

    private LocalPlayer localPlayer;
    private CameraTargetSystem cameraTargetSystem;
    private GUIManager guiManager;

    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        guiManager = CoreRegistry.get(GUIManager.class);

        CoreRegistry.get(EventSystem.class).registerEventHandler(guiManager);
    }

    @Override
    public void shutdown() {
    }

    public BindableButton registerBindButton(String bindId, String displayName) {
        return registerBindButton(bindId, displayName, new BindButtonEvent());
    }

    public BindableButton registerBindButton(String bindId, String displayName, BindButtonEvent event) {
        BindableButtonImpl bind = new BindableButtonImpl(bindId, displayName, event);
        buttonLookup.put(bindId, bind);
        buttonBinds.add(bind);
        return bind;
    }

    public BindableButton getBindButton(String bindId) {
        return buttonLookup.get(bindId);
    }

    public void linkBindButtonToInput(Input input, String bindId) {
        switch (input.getType()) {
            case KEY:
                linkBindButtonToKey(input.getId(), bindId);
                break;
            case MOUSE_BUTTON:
                linkBindButtonToMouse(input.getId(), bindId);
                break;
            case MOUSE_WHEEL:
                linkBindButtonToMouseWheel(input.getId(), bindId);
                break;
        }
    }

    public void linkBindButtonToInput(InputEvent input, String bindId) {
        if (input instanceof KeyEvent) {
            linkBindButtonToKey(((KeyEvent) input).getKey(), bindId);
        } else if (input instanceof MouseButtonEvent) {
            linkBindButtonToMouse(((MouseButtonEvent) input).getButton(), bindId);
        } else if (input instanceof MouseWheelEvent) {
            linkBindButtonToMouseWheel(((MouseWheelEvent) input).getWheelTurns(), bindId);
        }
    }

    public void linkBindButtonToKey(int key, String bindId) {
        BindableButtonImpl bindInfo = buttonLookup.get(bindId);
        keyBinds.put(key, bindInfo);
    }

    public void linkBindButtonToMouse(int mouseButton, String bindId) {
        BindableButtonImpl bindInfo = buttonLookup.get(bindId);
        mouseButtonBinds.put(mouseButton, bindInfo);
    }

    public void linkBindButtonToMouseWheel(int direction, String bindId) {
        if (direction > 0) {
            mouseWheelDownBind = buttonLookup.get(bindId);
        } else if (direction < 0) {
            mouseWheelUpBind = buttonLookup.get(bindId);
        }
    }

    public BindableAxis registerBindAxis(String id, BindableButton positiveButton, BindableButton negativeButton) {
        return registerBindAxis(id, new BindAxisEvent(), positiveButton, negativeButton);
    }

    public BindableAxis registerBindAxis(String id, BindAxisEvent event, String positiveButtonId, String negativeButtonId) {
        return registerBindAxis(id, event, getBindButton(positiveButtonId), getBindButton(negativeButtonId));
    }

    public BindableAxis registerBindAxis(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        BindableAxisImpl axis = new BindableAxisImpl(id, event, positiveButton, negativeButton);
        axisBinds.add(axis);
        axisLookup.put(id, axis);
        return axis;
    }

    public void update(float delta) {
        processMouseInput(delta);
        processKeyboardInput(delta);
        processBindRepeats(delta);
        processBindAxis(delta);
    }

    private void processMouseInput(float delta) {
        if (TerasologyEngine.isEditorInFocus())
            return;

        //process mouse clicks
        while (Mouse.next()) {
            //left/right/middle mouse click
            if (Mouse.getEventButton() != -1) {
                int button = Mouse.getEventButton();
                boolean buttonDown = Mouse.getEventButtonState();
                boolean consumed = sendMouseEvent(button, buttonDown, delta);

                BindableButtonImpl bind = mouseButtonBinds.get(button);
                if (bind != null) {
                    bind.updateBindState(buttonDown, delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, guiManager.isConsumingInput());
                }
            }
            //mouse wheel
            else if (Mouse.getEventDWheel() != 0) {
                int wheelMoved = Mouse.getEventDWheel();
                boolean consumed = sendMouseWheelEvent(wheelMoved / 120, delta);

                BindableButtonImpl bind = (wheelMoved > 0) ? mouseWheelUpBind : mouseWheelDownBind;
                if (bind != null) {
                    bind.updateBindState(true, delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, guiManager.isConsumingInput());
                    bind.updateBindState(false, delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, guiManager.isConsumingInput());
                }
            }
        }

        //process mouse movement x axis
        int deltaX = Mouse.getDX();
        if (deltaX != 0) {
            MouseAxisEvent event = new MouseXAxisEvent(deltaX * config.getInput().getMouseSensitivity(), delta);
            setupTarget(event);
            localPlayer.getEntity().send(event);
        }

        //process mouse movement y axis
        int deltaY = Mouse.getDY();
        if (deltaY != 0) {
            MouseAxisEvent event = new MouseYAxisEvent(deltaY * config.getInput().getMouseSensitivity(), delta);
            setupTarget(event);
            localPlayer.getEntity().send(event);
        }
    }

    private void setupTarget(InputEvent event) {
        if (cameraTargetSystem.isTargetAvailable() && !guiManager.isConsumingInput()) {
            event.setTarget(cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
        }
    }

    private void processKeyboardInput(float delta) {
        boolean guiConsumingInput = guiManager.isConsumingInput();
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();

            ButtonState state = getButtonState(Keyboard.getEventKeyState(), Keyboard.isRepeatEvent());
            boolean consumed = sendKeyEvent(key, state, delta);

            // Update bind
            BindableButtonImpl bind = keyBinds.get(key);
            if (bind != null && !Keyboard.isRepeatEvent()) {
                bind.updateBindState(Keyboard.getEventKeyState(), delta, localPlayer.getEntity(), cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), consumed, guiConsumingInput);
            }
        }
    }

    private void processBindAxis(float delta) {
        for (BindableAxisImpl axis : axisBinds) {
            axis.update(localPlayer.getEntity(), delta, cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
        }
    }

    private void processBindRepeats(float delta) {
        for (BindableButtonImpl button : buttonBinds) {
            button.update(localPlayer.getEntity(), delta, cameraTargetSystem.getTarget(), cameraTargetSystem.getTargetBlockPosition(), cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal());
        }
    }

    private ButtonState getButtonState(boolean keyDown, boolean repeatEvent) {
        if (repeatEvent) {
            return ButtonState.REPEAT;
        }
        return (keyDown) ? ButtonState.DOWN : ButtonState.UP;
    }

    private boolean sendKeyEvent(int key, ButtonState state, float delta) {
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
        setupTarget(event);
        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseEvent(int button, boolean buttonDown, float delta) {
        MouseButtonEvent event;
        switch (button) {
            case -1:
                return false;
            case 0:
                event = (buttonDown) ? LeftMouseDownButtonEvent.create(delta) : LeftMouseUpButtonEvent.create(delta);
                break;
            case 1:
                event = (buttonDown) ? RightMouseDownButtonEvent.create(delta) : RightMouseUpButtonEvent.create(delta);
                break;
            default:
                event = (buttonDown) ? MouseDownButtonEvent.create(button, delta) : MouseUpButtonEvent.create(button, delta);
                break;
        }
        setupTarget(event);
        localPlayer.getEntity().send(event);
        return event.isConsumed();
    }

    private boolean sendMouseWheelEvent(int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(wheelTurns, delta);
        setupTarget(mouseWheelEvent);
        localPlayer.getEntity().send(mouseWheelEvent);
        return mouseWheelEvent.isConsumed();
    }

}

