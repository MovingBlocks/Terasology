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
import org.terasology.math.geom.ImmutableVector2f;

/**
 */
public final class ControllerAction {
    private final Input input;
    private final ButtonState state;
    private final int controller;
    private final ImmutableVector2f axis;

    public ControllerAction(Input input, ButtonState state, int controller, float axisX, float axisY) {
        this.input = input;
        this.state = state;
        this.controller = controller;
        this.axis = new ImmutableVector2f(axisX, axisY);
    }

    /**
     * @return Whether this is an axis action
     */
    public boolean isAxisAction() {
        return false;
    }

    /**
     * @return the index of the controller that sent the event
     */
    public int getController() {
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

    public ImmutableVector2f getAxisPosition() {
        return axis;
    }

    @Override
    public String toString() {
        return "ControllerAction [" + this.input + ", axis: " + axis + " (" + state + ")]";
    }
}
