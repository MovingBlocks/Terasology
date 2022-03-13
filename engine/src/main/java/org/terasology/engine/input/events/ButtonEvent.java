// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.events;

import org.terasology.input.ButtonState;

public abstract class ButtonEvent extends InputEvent {

    public ButtonEvent(float delta) {
        super(delta);
    }

    public abstract ButtonState getState();

    public boolean isDown() {
        return getState() != ButtonState.UP;
    }
}
