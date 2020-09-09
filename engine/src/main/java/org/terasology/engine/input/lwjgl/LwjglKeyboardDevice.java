// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.lwjgl;

import com.google.common.collect.Queues;
import org.lwjgl.input.Keyboard;
import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.device.KeyboardAction;
import org.terasology.nui.input.device.KeyboardDevice;

import java.util.Queue;

/**
 *
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
