/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.input.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;

public class KeyEvent extends ButtonEvent {

    private Input input;
    private char keyChar;
    private ButtonState state;

    public KeyEvent(Input input, char keyChar, ButtonState state, float delta) {
        super(delta);
        this.input = input;
        this.keyChar = keyChar;
        this.state = state;
    }

    public Input getKey() {
        return input;
    }

    @Override
    public ButtonState getState() {
        return state;
    }

    public String getKeyName() {
        return input.getName();
    }

    public char getKeyCharacter() {
        return keyChar;
    }

    protected void setKey(Input newInput, char newKeyChar) {
        this.input = newInput;
        this.keyChar = newKeyChar;
    }

    public void reset() {
        reset(0.0f);
    }
}
