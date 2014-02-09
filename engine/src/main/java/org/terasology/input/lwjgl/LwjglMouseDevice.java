/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.collect.Queues;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.input.ButtonState;
import org.terasology.input.InputType;
import org.terasology.input.device.InputAction;
import org.terasology.input.device.MouseDevice;
import org.terasology.math.Vector2i;

import java.util.Queue;

/**
 * @author Immortius
 */
public class LwjglMouseDevice implements MouseDevice {

    @Override
    public Vector2i getPosition() {
        return new Vector2i(Mouse.getX(), Display.getHeight() - Mouse.getY());
    }

    @Override
    public Vector2i getDelta() {
        return new Vector2i(Mouse.getDX(), -Mouse.getDY());
    }

    @Override
    public boolean isButtonDown(int button) {
        return Mouse.isButtonDown(button);
    }

    @Override
    public boolean isVisible() {
        return !Mouse.isGrabbed();
    }

    @Override
    public Queue<InputAction> getInputQueue() {
        Queue<InputAction> result = Queues.newArrayDeque();

        while (Mouse.next()) {
            if (Mouse.getEventButton() != -1) {
                ButtonState state = (Mouse.getEventButtonState()) ? ButtonState.DOWN : ButtonState.UP;
                result.add(new InputAction(InputType.MOUSE_BUTTON.getInput(Mouse.getEventButton()), state));
            }
            if (Mouse.getEventDWheel() != 0) {
                int id = (Mouse.getEventDWheel() > 0) ? 1 : -1;
                result.add(new InputAction(InputType.MOUSE_WHEEL.getInput(id), id * Mouse.getEventDWheel() / 120));
            }
        }

        return result;
    }
}
