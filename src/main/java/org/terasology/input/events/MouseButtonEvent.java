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


import org.lwjgl.input.Mouse;
import org.terasology.input.ButtonState;

public class MouseButtonEvent extends ButtonEvent {

    private int button = 0;
    private ButtonState state;

    public MouseButtonEvent(int button, ButtonState state, float delta) {
        super(delta);
        this.state = state;
        this.button = button;
    }

    public ButtonState getState() {
        return state;
    }

    public int getButton() {
        return button;
    }

    public String getMouseButtonName() {
        return Mouse.getButtonName(button);
    }

    public String getButtonName() {
        return "mouse:" + getMouseButtonName();
    }

    protected void setButton(int button) {
        this.button = button;
    }

    public void reset() {
        reset(0f);
    }
}
