// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.device;

import org.terasology.math.geom.Vector2i;
import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;

/**
 *
 */
public final class MouseAction {
    private final Input input;
    private final ButtonState state;
    private final int delta;
    private final Vector2i mousePosition;

    public MouseAction(Input input, ButtonState state, Vector2i mousePosition) {
        this.mousePosition = mousePosition;
        this.input = input;
        this.state = state;
        this.delta = 0;
    }

    public MouseAction(Input input, int delta, Vector2i mousePosition) {
        this.mousePosition = mousePosition;
        this.input = input;
        this.state = ButtonState.DOWN;
        this.delta = delta;
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

    public Vector2i getMousePosition() {
        return mousePosition;
    }

    @Override
    public String toString() {
        return "MouseAction [" + this.input + ", mouse: " + mousePosition + " (" + state + ")]";
    }
}
