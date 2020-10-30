// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.Keyboard;

public final class KeyDownEvent extends KeyEvent {

    private static KeyDownEvent event = new KeyDownEvent(Keyboard.Key.NONE, 0);

    private KeyDownEvent(Input key, float delta) {
        super(key, ButtonState.DOWN, delta);
    }

    public static KeyDownEvent create(Input key, float delta) {
        event.reset(delta);
        event.setKey(key);
        return event;
    }

    public static KeyDownEvent createCopy(KeyDownEvent toBeCopied) {
        return new KeyDownEvent(toBeCopied.getKey(), toBeCopied.getDelta());
    }
}
