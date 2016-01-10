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
package org.terasology.input.device;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;

/**
 */
public final class KeyboardAction {
    private final Input input;
    private final ButtonState state;
    private final char inputChar;

    public KeyboardAction(Input input, ButtonState state, char inputChar) {
        this.input = input;
        this.state = state;
        this.inputChar = inputChar;
    }

    public KeyboardAction(Input input, int delta, char inputChar) {
        this.input = input;
        this.state = ButtonState.DOWN;
        this.inputChar = inputChar;
    }

    /**
     * @return The type of input involved in this action (mouse button/mouse wheel)
     */
    public Input getInput() {
        return input;
    }

    /**
     * @return The state of that input button
     */
    public ButtonState getState() {
        return state;
    }

    public char getInputChar() {
        return inputChar;
    }

    @Override
    public String toString() {
        return "KeyboardAction [" + this.input + " '" + inputChar + "' (" + state + ")]";
    }
}
