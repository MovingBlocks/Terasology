/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.input;

import java.util.Locale;
import java.util.Objects;

/**
 */
public final class UnknownInput implements Input {
    public static final String UNKNOWN_PART = "_UNKNOWN_";
    private InputType type;
    private int id;

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
        if (obj instanceof UnknownInput) {
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
