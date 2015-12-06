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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 */
public enum MouseInput implements Input {
    NONE(InputType.MOUSE_BUTTON, -1, "MOUSE_NONE", ""),
    MOUSE_LEFT(InputType.MOUSE_BUTTON, 0, "MOUSE_LEFT", "Left Click", "M_LEFT", "M_1"),
    MOUSE_RIGHT(InputType.MOUSE_BUTTON, 1, "MOUSE_RIGHT", "Right Click", "M_2"),
    MOUSE_3(InputType.MOUSE_BUTTON, 2, "MOUSE_3", "Mouse 3", "M_3"),
    MOUSE_4(InputType.MOUSE_BUTTON, 3, "MOUSE_4", "Mouse 4", "M_4"),
    MOUSE_5(InputType.MOUSE_BUTTON, 4, "MOUSE_5", "Mouse 5", "M_5"),
    WHEEL_UP(InputType.MOUSE_WHEEL, 1, "MOUSE_WHEEL_UP", "Mouse Wheel Up", "MWHEEL_UP"),
    WHEEL_DOWN(InputType.MOUSE_WHEEL, -1, "MOUSE_WHEEL_DOWN", "Mouse Wheel Down", "MWHEEL_DOWN");

    private static Map<String, MouseInput> lookup = Maps.newHashMap();

    private InputType type;
    private int id;
    private String displayName;
    private String name;
    private Set<String> identifiers;

    static {
        for (MouseInput value : values()) {
            for (String identifier : value.identifiers) {
                lookup.put(identifier, value);
            }
        }
    }

    private MouseInput(InputType type, int id, String name, String displayName, String... alternateStrings) {
        this.type = type;
        this.id = id;
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.displayName = displayName;
        this.identifiers = Sets.newHashSetWithExpectedSize(alternateStrings.length + 2);
        this.identifiers.add(name);
        this.identifiers.add(toString().toUpperCase(Locale.ENGLISH));
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
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public static MouseInput find(InputType type, int id) {
        for (MouseInput input : values()) {
            if (input.type == type && input.id == id) {
                return input;
            }
        }
        return NONE;
    }

    public static Input find(String name) {
        return lookup.get(name.toUpperCase(Locale.ENGLISH));
    }
}
