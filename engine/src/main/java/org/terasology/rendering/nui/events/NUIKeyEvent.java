// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;

/**
 * See {@link NUIInputEvent}
 */
public class NUIKeyEvent extends NUIInputEvent {
    private Input key;
    private ButtonState state;

    public NUIKeyEvent(MouseDevice mouse, KeyboardDevice keyboard, Input key, ButtonState state) {
        super(mouse, keyboard);
        this.key = key;
        this.state = state;
    }

    public Input getKey() {
        return key;
    }

    public ButtonState getState() {
        return state;
    }

    public boolean isDown() {
        return state != ButtonState.UP;
    }
}
