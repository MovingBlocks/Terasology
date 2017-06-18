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
package org.terasology.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.Time;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;
import org.terasology.input.device.nulldevices.NullMouseDevice;
import org.terasology.input.events.InputEvent;
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
import org.terasology.input.internal.BindableButtonImpl;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;

/**
 * This system processes mouse inputs for the menu.
 * A separate system to relax dependency for the camera target system in menus.
 */
public class MenuInputSystem extends BaseComponentSystem {

    @In
    private Config config;

    @In
    private DisplayDevice display;

    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    @In
    private ModuleManager moduleManager;

    private MouseDevice mouse = new NullMouseDevice();
    private Logger logger = LoggerFactory.getLogger(InputSystem.class);
    private boolean capturingMouse;

    public void setMouseDevice(MouseDevice mouseDevice) {
        this.mouse = mouseDevice;
    }


    public MouseDevice getMouseDevice() {
        return mouse;
    }

    @Override
    public void initialise() {
        capturingMouse = true;
    }

    public void update(float delta) {
        processMouseInput(delta);
    }

    public boolean isCapturingMouse() {
        return capturingMouse && display.hasFocus();
    }

    public void setCapturingMouse(boolean capturingMouse) {
        this.capturingMouse = capturingMouse;
    }

    private void processMouseInput(float delta) {
        if (!isCapturingMouse()) {
            return;
        }

        Vector2i deltaMouse = mouse.getDelta();
        //process mouse movement x axis
        if (deltaMouse.x != 0) {
            MouseAxisEvent event = new MouseXAxisEvent(deltaMouse.x * config.getInput().getMouseSensitivity(), delta);
            setupTarget(event);
            for (EntityRef entity : getInputEntities()) {
                entity.send(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }

        //process mouse movement y axis
        if (deltaMouse.y != 0) {
            int yMovement = config.getInput().isMouseYAxisInverted() ? deltaMouse.y * -1 : deltaMouse.y;
            MouseAxisEvent event = new MouseYAxisEvent(yMovement * config.getInput().getMouseSensitivity(), delta);
            setupTarget(event);
            for (EntityRef entity : getInputEntities()) {
                entity.send(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }

        //process mouse clicks
        for (MouseAction action : mouse.getInputQueue()) {
            switch (action.getInput().getType()) {
                case MOUSE_BUTTON:
                    int id = action.getInput().getId();
                    if (id != -1) {
                        MouseInput button = MouseInput.find(action.getInput().getType(), action.getInput().getId());
                        sendMouseEvent(button, action.getState().isDown(), action.getMousePosition(), delta);
                    }
                    break;
                case MOUSE_WHEEL:
                    int dir = action.getInput().getId();
                    if (dir != 0 && action.getTurns() != 0) {
                        sendMouseWheelEvent(action.getMousePosition(), dir * action.getTurns(), delta);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // No target or entity required for menus in a 2d context.
    private void setupTarget(InputEvent event) {
        EntityRef menuEntity = EntityRef.NULL;
        Vector3f nullVectorF = new Vector3f();
        Vector3i nullVectorI = new Vector3i();
        event.setTargetInfo(menuEntity, nullVectorI, nullVectorF, nullVectorF);
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
        setupTarget(event);
        for (EntityRef entity : getInputEntities()) {
            entity.send(event);
            if (event.isConsumed()) {
                break;
            }
        }
        boolean consumed = event.isConsumed();
        event.reset();
        return consumed;
    }

    private boolean sendMouseWheelEvent(Vector2i pos, int wheelTurns, float delta) {
        MouseWheelEvent mouseWheelEvent = new MouseWheelEvent(pos, wheelTurns, delta);
        setupTarget(mouseWheelEvent);
        for (EntityRef entity : getInputEntities()) {
            entity.send(mouseWheelEvent);
            if (mouseWheelEvent.isConsumed()) {
                break;
            }
        }
        return mouseWheelEvent.isConsumed();
    }

    private EntityRef[] getInputEntities() {
        return new EntityRef[]{localPlayer.getClientEntity(), localPlayer.getCharacterEntity()};
    }
}

