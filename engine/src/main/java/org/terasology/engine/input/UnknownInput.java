// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input;

import org.terasology.nui.input.Input;
import org.terasology.nui.input.InputType;

import java.util.Locale;
import java.util.Objects;

/**
 *
 */
public final class UnknownInput implements Input {
    public static final String UNKNOWN_PART = "_UNKNOWN_";
    private final InputType type;
    private final int id;

    public UnknownInput(InputType type, int id) {
        this.type = type;
        this.id = id;
    }

    public static Input tryParse(String string) {
        for (InputType type : InputType.values()) {
            if (string.startsWith(type.toString())) {
                String remainder = string.substring(type.toString().length());
                if (remainder.startsWith(UNKNOWN_PART)) {
                    String hexadecimal = remainder.substring(UNKNOWN_PART.length());
                    try {
                        int id = (int) Long.parseLong(hexadecimal, 16);
                        return type.getInput(id);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public InputType getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return type.toString() + UNKNOWN_PART + Integer.toHexString(id).toUpperCase(Locale.ENGLISH);
    }

    @Override
    public String getDisplayName() {
        return "Unknown " + Integer.toHexString(id).toUpperCase(Locale.ENGLISH);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof org.terasology.nui.input.UnknownInput) {
            Input other = (Input) obj;
            return Objects.equals(other.getType(), this.type) && other.getId() == this.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.id);
    }
}
