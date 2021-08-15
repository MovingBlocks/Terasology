// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.input.events.ButtonEvent;
import org.terasology.input.ButtonState;

public class BindButtonEvent extends ButtonEvent {

    private SimpleUri id;
    private ButtonState state;

    public BindButtonEvent() {
        super(0);
    }

    public void prepare(SimpleUri buttonId, ButtonState newState, float delta) {
        reset(delta);
        this.id = buttonId;
        this.state = newState;
    }

    public SimpleUri getId() {
        return id;
    }

    @Override
    public ButtonState getState() {
        return state;
    }

}
