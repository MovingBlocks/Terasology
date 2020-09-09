// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;

import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;
import org.terasology.nui.input.Keyboard;

public final class KeyUpEvent extends KeyEvent {

    private static final KeyUpEvent event = new KeyUpEvent(Keyboard.Key.NONE, '\0', 0);

    private KeyUpEvent(Input key, char keyChar, float delta) {
        super(key, keyChar, ButtonState.UP, delta);
    }

    public static KeyUpEvent create(Input key, char keyChar, float delta) {
        event.reset(delta);
        event.setKey(key, keyChar);
        return event;
    }

    public static KeyUpEvent createCopy(KeyUpEvent toBeCopied) {
        return new KeyUpEvent(toBeCopied.getKey(), toBeCopied.getKeyCharacter(), toBeCopied.getDelta());
    }
}
