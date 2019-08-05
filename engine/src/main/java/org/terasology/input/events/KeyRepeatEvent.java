/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.input.events;

import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.Keyboard;

public final class KeyRepeatEvent extends KeyEvent {

    private static KeyRepeatEvent event = new KeyRepeatEvent(Keyboard.Key.NONE, '\0', 0);

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
