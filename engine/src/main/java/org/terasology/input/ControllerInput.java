/*
 * Copyright 2015 MovingBlocks
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

/**
 * TODO Type description
 */
public enum ControllerInput implements Input {

    NONE(ControllerId.NONE, "CONTROLLER_NONE", ""),
    CONTROLLER_0(ControllerId.ZERO, "CONTROLLER_0", "Controller Button 0"),
    CONTROLLER_1(ControllerId.ONE, "CONTROLLER_1", "Controller Button 1"),
    CONTROLLER_2(ControllerId.TWO, "CONTROLLER_2", "Controller Button 2"),
    CONTROLLER_3(ControllerId.THREE, "CONTROLLER_3", "Controller Button 3"),
    CONTROLLER_4(ControllerId.FOUR, "CONTROLLER_4", "Controller Button 4"),
    CONTROLLER_5(ControllerId.FIVE, "CONTROLLER_5", "Controller Button 5"),
    CONTROLLER_6(ControllerId.SIX, "CONTROLLER_6", "Controller Button 6"),
    CONTROLLER_7(ControllerId.SEVEN, "CONTROLLER_7", "Controller Button 7"),
    CONTROLLER_8(ControllerId.EIGHT, "CONTROLLER_8", "Controller Button 8"),
    CONTROLLER_9(ControllerId.NINE, "CONTROLLER_9", "Controller Button 9"),
    CONTROLLER_10(ControllerId.TEN, "CONTROLLER_10", "Controller Button 10"),
    CONTROLLER_11(ControllerId.ELEVEN, "CONTROLLER_11", "Controller Button 11");

    private int id;
    private String displayName;
    private String name;

    private ControllerInput(int id, String name, String displayName) {
        this.id = id;
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.displayName = displayName;
    }

    @Override
    public InputType getType() {
        return InputType.CONTROLLER_BUTTON;
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

    public static Input find(String name) {
        String upperCase = name.toUpperCase(Locale.ENGLISH);
        for (ControllerInput input : values()) {
            if (input.name.equals(upperCase)) {
                return input;
            }
        }
        return NONE;
    }

    public static Input find(InputType type, int id) {
        if (type == InputType.CONTROLLER_BUTTON) {
            for (ControllerInput input : values()) {
                if (input.id == id) {
                    return input;
                }
            }
        }
        return NONE;
    }

}
