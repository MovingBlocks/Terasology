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

import org.lwjgl.input.Controllers;
import org.terasology.input.ButtonState;
import org.terasology.input.ControllerDevice;
import org.terasology.input.ControllerId;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.device.ControllerAction;

/**
 * TODO Type description
 */
public class LwjglControllerDevice implements ControllerDevice {

    @Override
    public List<String> getControllers() {
        List<String> ids = new ArrayList<>();

        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            ids.add(Controllers.getController(i).getName());
        }

        return ids;
    }

    @Override
    public Queue<ControllerAction> getInputQueue() {
        Queue<ControllerAction> result = new ArrayDeque<>();

        while (Controllers.next()) {
            int controller = Controllers.getEventSource().getIndex();
            float axisValue = 0f;
            Input input;
            ButtonState state = ButtonState.UP;

            if (Controllers.isEventButton()) {
                state = Controllers.getEventButtonState() ? ButtonState.DOWN : ButtonState.UP;
                input = InputType.CONTROLLER_BUTTON.getInput(Controllers.getEventControlIndex());
            } else if (Controllers.isEventXAxis()) {
                axisValue = Controllers.getEventXAxisValue();
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.X_AXIS);
            } else if (Controllers.isEventYAxis()) {
                axisValue = Controllers.getEventYAxisValue();
                input = InputType.CONTROLLER_AXIS.getInput(ControllerId.Y_AXIS);
            } else { //if (Controllers.isEventPovX() || Controllers.isEventPovY()) {
                continue;
            }
            result.add(new ControllerAction(input, controller, state, axisValue));
        }

        return result;
    }

}
