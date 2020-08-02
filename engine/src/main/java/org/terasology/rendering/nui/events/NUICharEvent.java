// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.events;

import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;

public class NUICharEvent extends NUIInputEvent {

    private char character;

    public NUICharEvent(MouseDevice mouse, KeyboardDevice keyboard, char character) {
        super(mouse, keyboard);
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }
}
