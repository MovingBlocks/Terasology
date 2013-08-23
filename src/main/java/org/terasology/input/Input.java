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

package org.terasology.input;

import org.lwjgl.input.Keyboard;

import java.util.Locale;

/**
 * The description of an input, whether key, mouse button or mouse wheel.
 * Immutable.
 *
 * @author Immortius
 */
public final class Input {
    private InputType type;
    private int id;

    public Input() {
        this.type = InputType.NONE;
    }

    public Input(InputType type, int id) {
        this.type = type;
        this.id = id;
    }

    public static Input parse(String inputString) {
        String normalisedString = inputString.toUpperCase(Locale.ENGLISH);
        if (normalisedString.startsWith("KEY_")) {
            int id = Keyboard.getKeyIndex(normalisedString.substring(4));
            if (id != Keyboard.KEY_NONE) {
                return new Input(InputType.KEY, id);
            }
        } else {
            MouseInput mouseInput = MouseInput.parse(normalisedString);
            if (mouseInput != MouseInput.MOUSE_NONE) {
                return mouseInput.getInput();
            }
        }
        return new Input();
    }

    public InputType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String toShortString() {
        switch (type) {
            case KEY:
                return Keyboard.getKeyName(id);
            case MOUSE_BUTTON:
            case MOUSE_WHEEL:
                return MouseInput.getInputFor(type, id).toShortString();
        }
        return "";
    }

    public String toString() {
        switch (type) {
            case KEY:
                return "KEY_" + Keyboard.getKeyName(id);
            case MOUSE_BUTTON:
            case MOUSE_WHEEL:
                return MouseInput.getInputFor(type, id).toString();
        }
        return "";
    }
}
