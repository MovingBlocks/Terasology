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
import java.util.Queue;

import org.lwjgl.input.Controllers;
import org.terasology.input.ButtonState;
import org.terasology.input.ControllerDevice;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.device.ControllerAction;

/**
 * TODO Type description
 */
public class LwjglControllerDevice implements ControllerDevice {

    public LwjglControllerDevice() {
    }

    @Override
    public Queue<ControllerAction> getInputQueue() {
        Queue<ControllerAction> result = new ArrayDeque<>();

        while (Controllers.next()) {
            if (Controllers.isEventButton()) {
                ButtonState state = Controllers.getEventButtonState() ? ButtonState.DOWN : ButtonState.UP;
                Input input = InputType.CONTROLLER_BUTTON.getInput(Controllers.getEventControlIndex());
                int controller = Controllers.getEventSource().getIndex();
                float axisX = Controllers.getEventXAxisValue();
                float axisY = Controllers.getEventYAxisValue();
                result.add(new ControllerAction(input, state, controller, axisX, axisY));
            }
        }

        return result;
    }

}
