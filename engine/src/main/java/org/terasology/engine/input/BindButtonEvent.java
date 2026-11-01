// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.input.events.ButtonEvent;
import org.terasology.input.ButtonState;

/**
 * An event triggered by auto-registered (physical) buttons when they are pressed.
 * <p>
 * To be used in combination with {@link RegisterBindButton} and {@link DefaultBinding}.
 * <p>
 * Classes extending this usually follow the naming pattern {@code &#60;Name&#62;Button}.
 * <p>
 * <b>NOTE:</b> DO NOT USE DIRECTLY!
 */
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
