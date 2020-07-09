// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.device;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;

public final class RawKeyboardAction {
    private final Input input;
    private final ButtonState state;

    public RawKeyboardAction(Input input, ButtonState state) {
        this.input = input;
        this.state = state;
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

    @Override
    public String toString() {
        return "KeyboardAction [" + this.input + " (" + state + ")]";
    }
}
