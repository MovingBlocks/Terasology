// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;

import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;

public class KeyEvent extends ButtonEvent {

    private Input input;
    private char keyChar;
    private final ButtonState state;

    public KeyEvent(Input input, char keyChar, ButtonState state, float delta) {
        super(delta);
        this.input = input;
        this.keyChar = keyChar;
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

    public char getKeyCharacter() {
        return keyChar;
    }

    protected void setKey(Input newInput, char newKeyChar) {
        this.input = newInput;
        this.keyChar = newKeyChar;
    }

    public void reset() {
        reset(0.0f);
    }
}
