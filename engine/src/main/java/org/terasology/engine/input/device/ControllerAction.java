// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.device;

import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;

/**
 *
 */
public final class ControllerAction {
    private final Input input;
    private final ButtonState state;
    private final String controller;
    private final float axisValue;

    public ControllerAction(Input input, String controller, ButtonState state, float axisValue) {
        this.input = input;
        this.state = state;
        this.controller = controller;
        this.axisValue = axisValue;
    }

    /**
     * @return the name of the controller that sent the event
     */
    public String getController() {
        return controller;
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

    public float getAxisValue() {
        return axisValue;
    }

    @Override
    public String toString() {
        return "ControllerAction [" + this.input + ", axis: " + axisValue + " (" + state + ")]";
    }
}
