// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.Keyboard;

public final class KeyRepeatEvent extends KeyEvent {

    private static KeyRepeatEvent event = new KeyRepeatEvent(Keyboard.Key.NONE, 0);

    private KeyRepeatEvent(Input key, float delta) {
        super(key, ButtonState.REPEAT, delta);
    }

    public static KeyRepeatEvent create(Input key, float delta) {
        event.reset(delta);
        event.setKey(key);
        return event;
    }

    public static KeyRepeatEvent createCopy(KeyRepeatEvent toBeCopied) {
        return new KeyRepeatEvent(toBeCopied.getKey(), toBeCopied.getDelta());
    }


}
