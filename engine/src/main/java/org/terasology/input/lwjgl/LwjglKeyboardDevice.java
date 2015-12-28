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

import org.lwjgl.input.Keyboard;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.device.KeyboardAction;
import org.terasology.input.device.KeyboardDevice;

import java.util.Queue;

/**
 */
public class LwjglKeyboardDevice implements KeyboardDevice {
    @Override
    public boolean isKeyDown(int key) {
        return Keyboard.isKeyDown(key);
    }

    @Override
    public Queue<KeyboardAction> getInputQueue() {
        Queue<KeyboardAction> result = Queues.newArrayDeque();

        while (Keyboard.next()) {
            ButtonState state;
            if (Keyboard.isRepeatEvent()) {
                state = ButtonState.REPEAT;
            } else {
                state = (Keyboard.getEventKeyState()) ? ButtonState.DOWN : ButtonState.UP;
            }
            Input input = InputType.KEY.getInput(Keyboard.getEventKey());
            result.add(new KeyboardAction(input, state, Keyboard.getEventCharacter()));
        }

        return result;
    }
}
