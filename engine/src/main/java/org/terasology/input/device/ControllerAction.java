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
