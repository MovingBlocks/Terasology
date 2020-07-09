// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;

public class KeyEvent extends ButtonEvent {

    private Input input;
    private ButtonState state;

    public KeyEvent(Input input, ButtonState state, float delta) {
        super(delta);
        this.input = input;
        this.state = state;
    }

    public Input getKey() {
        return input;
    }

    @Override
    public ButtonState getState() {
        return state;
    }

    public String getKeyName() {
        return input.getName();
    }

    protected void setKey(Input newInput) {
        this.input = newInput;
    }

    public void reset() {
        reset(0.0f);
    }
}
