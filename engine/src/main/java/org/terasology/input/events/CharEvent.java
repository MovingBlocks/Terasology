// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.events;

public final class CharEvent extends InputEvent {

    private static CharEvent event = new CharEvent('\0', 0);

    private char character;

    private CharEvent(char character, float delta) {
        super(delta);
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    public static CharEvent create(char character, float delta) {
        event.reset(delta);
        event.character = character;
        return event;
    }

    public static CharEvent createCopy(char character, float delta) {
        return new CharEvent(character, delta);
    }

    public void reset() {
        reset(0.0f);
    }
}
