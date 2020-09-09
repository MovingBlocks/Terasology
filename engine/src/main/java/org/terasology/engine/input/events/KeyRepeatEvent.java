// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;

import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;
import org.terasology.nui.input.Keyboard;

public final class KeyRepeatEvent extends KeyEvent {

    private static final KeyRepeatEvent event = new KeyRepeatEvent(Keyboard.Key.NONE, '\0', 0);

    private KeyRepeatEvent(Input key, char keyChar, float delta) {
        super(key, keyChar, ButtonState.REPEAT, delta);
    }

    public static KeyRepeatEvent create(Input key, char keyChar, float delta) {
        event.reset(delta);
        event.setKey(key, keyChar);
        return event;
    }

    public static KeyRepeatEvent createCopy(KeyRepeatEvent toBeCopied) {
        return new KeyRepeatEvent(toBeCopied.getKey(), toBeCopied.getKeyCharacter(), toBeCopied.getDelta());
    }


}
