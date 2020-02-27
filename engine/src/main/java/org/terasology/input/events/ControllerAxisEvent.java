/*
 * Copyright 2020 MovingBlocks
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

import org.terasology.input.ControllerInput;
import org.terasology.input.Input;

public class ControllerAxisEvent extends AxisEvent {

    private static ControllerAxisEvent event = new ControllerAxisEvent(ControllerInput.X_AXIS, 0, 0);

    private float value;
    private Input axis;

    protected ControllerAxisEvent(Input axis, float value, float delta) {
        super(delta);
        this.axis = axis;
        this.value = value;
    }

    public Input getAxis() {
        return axis;
    }

    @Override
    public float getValue() {
        return value;
    }

    public static ControllerAxisEvent create(Input axis, float value, float delta) {
        event.reset(delta);
        event.axis = axis;
        event.value = value;
        return event;
    }

    public static ControllerAxisEvent createCopy(ControllerAxisEvent toBeCopied) {
        return new ControllerAxisEvent(toBeCopied.getAxis(), toBeCopied.getValue(), toBeCopied.getDelta());
    }

    public void reset() {
        reset(0f);
    }
}
