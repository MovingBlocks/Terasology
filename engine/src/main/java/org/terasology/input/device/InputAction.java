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
import org.terasology.math.Vector2i;

/**
 * @author Immortius
 */
public final class InputAction {
    private final Input input;
    private final ButtonState state;
    private final int delta;
    private final char inputChar;
    private final Vector2i mousePosition;

    public InputAction(Input input, ButtonState state, Vector2i mousePosition) {
        this.mousePosition = mousePosition;
        this.input = input;
        this.state = state;
        this.delta = 0;
        this.inputChar = '\0';
    }

    public InputAction(Input input, int delta, Vector2i mousePosition) {
        this.mousePosition = mousePosition;
        this.input = input;
        this.state = ButtonState.DOWN;
        this.delta = delta;
        this.inputChar = '\0';
    }

    public InputAction(Input input, ButtonState state, char inputChar) {
        this.mousePosition = null;
        this.input = input;
        this.state = state;
        this.delta = 0;
        this.inputChar = inputChar;
    }

    public InputAction(Input input, int delta, char inputChar) {
        this.mousePosition = null;
        this.input = input;
        this.state = ButtonState.DOWN;
        this.delta = delta;
        this.inputChar = inputChar;
    }

    /**
     * @return Whether this is an axis action (e.g. a mouse wheel or volume knob)
     */
    public boolean isAxisAction() {
        return delta != 0;
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

    /**
     * @return For axis actions, the change in value
     */
    public int getTurns() {
        return delta;
    }

    public char getInputChar() {
        return inputChar;
    }

    public Vector2i getMousePosition() {
        return mousePosition;
    }
    
    @Override
    public String toString() {
        return "InputAction [" + this.input + " \'" + inputChar + "' (" + state + "), mouse: " + mousePosition + "]";
    }
}
