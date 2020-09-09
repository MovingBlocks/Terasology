// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;

import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;
import org.terasology.nui.input.Keyboard;

public final class KeyDownEvent extends KeyEvent {

    private static final KeyDownEvent event = new KeyDownEvent(Keyboard.Key.NONE, '\0', 0);

    private KeyDownEvent(Input key, char keyChar, float delta) {
        super(key, keyChar, ButtonState.DOWN, delta);
    }

    public static KeyDownEvent create(Input key, char keyChar, float delta) {
        event.reset(delta);
        event.setKey(key, keyChar);
        return event;
    }

    public static KeyDownEvent createCopy(KeyDownEvent toBeCopied) {
        return new KeyDownEvent(toBeCopied.getKey(), toBeCopied.getKeyCharacter(), toBeCopied.getDelta());
    }
}
