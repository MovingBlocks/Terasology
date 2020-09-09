// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.device;

import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;

/**
 *
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
