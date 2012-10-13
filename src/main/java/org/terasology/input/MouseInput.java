/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

/**
 * @author Immortius
 */
public enum MouseInput {
    MOUSE_NONE(InputType.MOUSE_BUTTON, -1, ""),
    MOUSE_LEFT(InputType.MOUSE_BUTTON, 0, "M_Left"),
    MOUSE_RIGHT(InputType.MOUSE_BUTTON, 1, "M_Right"),
    MOUSE_1(InputType.MOUSE_BUTTON, 0, "M_1"),
    MOUSE_2(InputType.MOUSE_BUTTON, 1, "M_2"),
    MOUSE_3(InputType.MOUSE_BUTTON, 2, "M_3"),
    MOUSE_4(InputType.MOUSE_BUTTON, 3, "M_4"),
    MOUSE_5(InputType.MOUSE_BUTTON, 4, "M_5"),
    MOUSE_WHEEL_UP(InputType.MOUSE_WHEEL, 1, "MWheel_Up"),
    MOUSE_WHEEL_DOWN(InputType.MOUSE_WHEEL, -1, "MWheel_Down");

    private InputType type;
    private int id;
    private String shortString;

    private MouseInput(InputType type, int id, String shortString) {
        this.type = type;
        this.id = id;
        this.shortString =shortString;
    }

    public InputType getType() {
        return type;
    }

    public int getId() {
        return id;
    }
    public Input getInput() {
        return new Input(type, id);
    }

    public String toShortString() {
        return shortString;
    }

    public static MouseInput parse(String id) {
        try {
            MouseInput input = valueOf(id);
            return input;
        } catch (IllegalArgumentException e) {
            return MOUSE_NONE;
        }
    }

    public static MouseInput getInputFor(InputType type, int id) {
        for (MouseInput input : values()) {
            if (input.type == type && input.id == id) {
                return input;
            }
        }
        return MOUSE_NONE;
    }
}
