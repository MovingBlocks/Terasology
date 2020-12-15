// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.Keyboard;

public final class KeyUpEvent extends KeyEvent {

    private static KeyUpEvent event = new KeyUpEvent(Keyboard.Key.NONE, 0);

    private KeyUpEvent(Input key, float delta) {
        super(key, ButtonState.UP, delta);
    }

    public static KeyUpEvent create(Input key, float delta) {
        event.reset(delta);
        event.setKey(key);
        return event;
    }

    public static KeyUpEvent createCopy(KeyUpEvent toBeCopied) {
        return new KeyUpEvent(toBeCopied.getKey(), toBeCopied.getDelta());
    }
}
