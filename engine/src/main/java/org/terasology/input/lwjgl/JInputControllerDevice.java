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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.ControllerConfig;
import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.input.ButtonState;
import org.terasology.input.ControllerDevice;
import org.terasology.input.ControllerId;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.device.ControllerAction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Retrieves information on connected controllers through JInput.
 *
 * When the config.cfg is empty this will "discover" inputs via JInput. These inputs do not get added to Config
 * until either an event is received from the input, or the user goes to the settings screen; at which point an empty,
 * default ControllerInfo will be created and returned.
 */
public class JInputControllerDevice implements ControllerDevice {

    private static final Logger logger = LoggerFactory.getLogger(JInputControllerDevice.class);

    private final Set<Type> filter = ImmutableSet.of(Type.KEYBOARD, Type.MOUSE, Type.UNKNOWN);

    private final Map<Identifier, Integer> buttonMap = ImmutableMap.<Identifier, Integer>builder()
            .put(Button._0, ControllerId.ZERO)
            .put(Button._1, ControllerId.ONE)
            .put(Button._2, ControllerId.TWO)
            .put(Button._3, ControllerId.THREE)
            .put(Button._4, ControllerId.FOUR)
            .put(Button._5, ControllerId.FIVE)
            .put(Button._6, ControllerId.SIX)
            .put(Button._7, ControllerId.SEVEN)
            .put(Button._8, ControllerId.EIGHT)
            .put(Button._9, ControllerId.NINE)
            .put(Button._10, ControllerId.TEN)
            .put(Button._11, ControllerId.ELEVEN)
            .build();

    private ControllerConfig config;
    /* MUST be a CopyOnWriteArrayList! Or else you just get crashes when unplugging controllers. */
    private final List<Controller> controllers = new CopyOnWriteArrayList<>();

    public JInputControllerDevice(ControllerConfig config) {
        this.config = config;
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
        logger.info("Discovered {} input devices", env.getControllers().length);
        for (Controller c : env.getControllers()) {
            addController(c);
        }
    }

    @Override
    public List<String> getControllers() {
        List<String> ids = new ArrayList<>();

        for (Controller controller : controllers) {
            ids.add(controller.getName());
        }
        logger.info("getControllers returning {}", ids);
        return ids;
    }

    @Override
    public ControllerInfo describeController(String name) {
        Controller controller = controllers.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        ControllerInfo info = new ControllerInfo();
        if (controller != null) {
            Component[] components = controller.getComponents();
            for (int i = 0; i < components.length; i++) {

                Component c = components[i];
                Identifier identifier = c.getIdentifier();
                if (identifier instanceof Axis) {
                    ControllerConfig.Axis axis =
                            new ControllerConfig.Axis(c.getName(), identifier.getName(), false, 0.08f);
                    info.getAxes().add(axis);
                } else if (identifier instanceof Button) {
                    ControllerConfig.Button button = new ControllerConfig.Button(c.getName(), identifier.getName());
                    info.getButtons().add(button);
                }
            }
        }
        return info;
    }

    @Override
    public Queue<ControllerAction> getInputQueue() {
        Queue<ControllerAction> result = new ArrayDeque<>();
        Event event = new Event();

        Iterator<Controller> it = controllers.iterator();
        while (it.hasNext()) {
            Controller c = it.next();
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

        ControllerInfo info = config.getController(c.getName());
        if (info.isEmpty()) {
            // Controller has not been configured
            return null;
        }
        if (!info.isEnabled()) {
            // Controller is disabled
            return null;
        }

        if (id instanceof Identifier.Button) {
            state = event.getValue() != 0 ? ButtonState.DOWN : ButtonState.UP;
            Integer buttonId = buttonMap.get(id);
            if (buttonId == null) {
                return null; //button not registered
            }
            input = InputType.CONTROLLER_BUTTON.getInput(buttonId);
        } else if (id instanceof Identifier.Axis) {
            // TODO ignore event if controller not found
            if (!id.equals(Identifier.Axis.POV)) {
                ControllerConfig.Axis axis = info.findAxis(id.getName());
                if (Math.abs(axisValue) < axis.getDeadZone()) {
                    axisValue = 0;
                }
                input = InputType.CONTROLLER_AXIS.getInput(findControllerId(axis.getIdName()));
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

    private int findControllerId(String axisId) {
        if (axisId.equalsIgnoreCase("x")) {
            return ControllerId.X_AXIS;
        } else if (axisId.equalsIgnoreCase("y")) {
            return ControllerId.Y_AXIS;
        } else if (axisId.equalsIgnoreCase("z")) {
            return ControllerId.Z_AXIS;
        } else if (axisId.equalsIgnoreCase("rx")) {
            return ControllerId.RX_AXIS;
        } else if (axisId.equalsIgnoreCase("ry")) {
            return ControllerId.RY_AXIS;
        }
        return ControllerId.NONE;
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
