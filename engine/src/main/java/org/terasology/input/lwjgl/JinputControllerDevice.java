/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.input.lwjgl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.ButtonState;
import org.terasology.input.ControllerDevice;
import org.terasology.input.ControllerId;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.device.ControllerAction;

import com.google.common.collect.ImmutableSet;

/**
 * TODO Type description
 */
public class JinputControllerDevice implements ControllerDevice {

    private static final Logger logger = LoggerFactory.getLogger(JinputControllerDevice.class);

    /**
     * CopyOnWriteAL supports removing elements while iterating over the list.
     */
    private List<Controller> controllers = new CopyOnWriteArrayList<>();

    private Set<Type> filter = ImmutableSet.of(Type.KEYBOARD, Type.MOUSE, Type.UNKNOWN);

    public JinputControllerDevice() {
        ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();

        // Unfortunately, no existing implementation
        env.addControllerListener(new ControllerListener() {

            @Override
            public void controllerRemoved(ControllerEvent ev) {
                Controller controller = ev.getController();
                logger.info("Controller disconnected: " + controller.getName());
                removeController(controller);
            }

            @Override
            public void controllerAdded(ControllerEvent ev) {
                Controller controller = ev.getController();
                logger.info("Controller connected: " + controller.getName());
                addController(controller);
            }
        });

        for (Controller c : env.getControllers()) {
            addController(c);
        }
    }

    @Override
    public List<String> getControllers() {
        List<String> ids = new ArrayList<>();

        for (Controller c : controllers) {
            ids.add(c.getName());
        }

        return ids;
    }

    @Override
    public void setDeadZone(int index, float deadZone) {
//        Controller controller = controllers.get(index);
//        for (Component c = controller.getComponent(Identifier.Axis.X);
//            if (c.getIdentifier() instanceof Identifier.Axis) {
//            }
//        }
    }

    @Override
    public Queue<ControllerAction> getInputQueue() {
        Queue<ControllerAction> result = new ArrayDeque<>();
        Event event = new Event();

        for (Controller c : controllers) {
            if (c.poll()) {
                EventQueue queue = c.getEventQueue();

                while (queue.getNextEvent(event)) {
                    ControllerAction action = convertEvent(c, event);
                    if (action != null) {
                        result.add(action);
                    }
                }
            } else {
                removeController(c);
            }
        }

        return result;
    }

    private ControllerAction convertEvent(Controller c, Event event) {
        Component comp = event.getComponent();
        Identifier id = comp.getIdentifier();
        float axisValue = comp.getPollData();
        Input input;
        ButtonState state = ButtonState.UP;

        if (id instanceof Identifier.Button) {
            state = event.getValue() != 0 ? ButtonState.DOWN : ButtonState.UP;
            input = InputType.CONTROLLER_BUTTON.getInput(comp.getIdentifier().getName());
        } else if (id instanceof Identifier.Axis) {
            if (id.equals(Identifier.Axis.X)) {
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.X_AXIS);
            } else if (id.equals(Identifier.Axis.Y)) {
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.Y_AXIS);
            } else if (id.equals(Identifier.Axis.Z)) {
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.Z_AXIS);
            } else if (id.equals(Identifier.Axis.RX)) {
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.RX_AXIS);
            } else if (id.equals(Identifier.Axis.RY)) {
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.RY_AXIS);
            } else if (id.equals(Identifier.Axis.POV)) {
                // the poll data float value is actually an ID in this case
                boolean isX = (axisValue == Component.POV.LEFT) || (axisValue == Component.POV.RIGHT);
                boolean isY = (axisValue == Component.POV.UP) || (axisValue == Component.POV.DOWN);
                if (isX || isY) {
                    input = InputType.CONTROLLER_AXIS.getInput(isX ? ControllerId.POVX_AXIS : ControllerId.POVY_AXIS);
                    if ((axisValue == Component.POV.UP) || (axisValue == Component.POV.LEFT)) {
                         axisValue = -1;
                    }
                    if ((axisValue == Component.POV.DOWN) || (axisValue == Component.POV.RIGHT)) {
                        axisValue = 1;
                    }
                } else {
                    return null;  // TODO: handle 8-button POVs
                }
            } else {
                return null; // unrecognized axis
            }
        } else {
            return null; // unrecognized id (e.g. Identifier.Key)
        }

        return new ControllerAction(input, c.getName(), state, axisValue);
    }

    /**
     * Removes a controller. Also works while iterating over the list.
     * @param controller the controller to remove
     */
    private void removeController(Controller controller) {
        controllers.remove(controller);
        logger.info("Removed controller: " + controller.getName());
    }

    private void addController(Controller c) {
        if (filter.contains(c.getType())) {
            logger.debug("Ignoring controller: " + c.getName());
            return;
        }

        if (c.getControllers().length == 0) {
            controllers.add(c);
            logger.info("Registered controller: " + c.getName());
        } else {
            for (Controller sub : c.getControllers()) {
                addController(sub);
            }
        }
    }

}
