/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;

/**
 * See {@link NUIInputEvent}
 */
public class NUIKeyEvent extends NUIInputEvent {
    private Input key;
    private char keyCharacter;
    private ButtonState state;

    public NUIKeyEvent(MouseDevice mouse, KeyboardDevice keyboard, Input key, char keyChar, ButtonState state) {
        super(mouse, keyboard);
        this.key = key;
        this.keyCharacter = keyChar;
        this.state = state;
    }

    public Input getKey() {
        return key;
    }

    public char getKeyCharacter() {
        return keyCharacter;
    }

    public ButtonState getState() {
        return state;
    }

    public boolean isDown() {
        return state != ButtonState.UP;
    }
}
