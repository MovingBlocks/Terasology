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


/**
 */
public class MouseAxisEvent extends AxisEvent {

    public enum MouseAxis {
        X,
        Y;
    }

    private float value;
    private MouseAxis mouseAxis;


    public MouseAxisEvent(MouseAxis axis, float value, float delta) {
        super(delta);
        this.mouseAxis = axis;
        this.value = value;
    }

    public MouseAxis getMouseAxis() {
        return mouseAxis;
    }

    @Override
    public float getValue() {
        return value;
    }
}
