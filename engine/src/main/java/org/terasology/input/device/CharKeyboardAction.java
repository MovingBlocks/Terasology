// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.device;

public final class CharKeyboardAction {
    private final char character;


    public CharKeyboardAction(char character) {
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    @Override
    public String toString() {
        return "CharKeyboardAction [" + character + "]";
    }
}
