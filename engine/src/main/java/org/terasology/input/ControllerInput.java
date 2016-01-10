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
 * Input information from controllers.
 */
public enum ControllerInput implements Input {

    NONE(InputType.CONTROLLER_BUTTON, ControllerId.NONE, "CONTROLLER_NONE", "None"),

    X_AXIS(InputType.CONTROLLER_AXIS, ControllerId.X_AXIS, "X_AXIS", "X-Axis"),
    Y_AXIS(InputType.CONTROLLER_AXIS, ControllerId.Y_AXIS, "Y_AXIS", "Y-Axis"),
    Z_AXIS(InputType.CONTROLLER_AXIS, ControllerId.Z_AXIS, "Z_AXIS", "Z-Axis"),

    RX_AXIS(InputType.CONTROLLER_AXIS, ControllerId.RX_AXIS, "RX_AXIS", "Rotational X-Axis"),
    RY_AXIS(InputType.CONTROLLER_AXIS, ControllerId.RY_AXIS, "RY_AXIS", "Rotational Y-Axis"),

    POVX_AXIS(InputType.CONTROLLER_AXIS, ControllerId.POVX_AXIS, "POVX_AXIS", "Point-of-View X-Axis"),
    POVY_AXIS(InputType.CONTROLLER_AXIS, ControllerId.POVY_AXIS, "POVY_AXIS", "Point-of-View Y-Axis"),

    BUTTON_0(InputType.CONTROLLER_BUTTON, ControllerId.ZERO, "BUTTON_0", "Controller Button 0"),
    BUTTON_1(InputType.CONTROLLER_BUTTON, ControllerId.ONE, "BUTTON_1", "Controller Button 1"),
    BUTTON_2(InputType.CONTROLLER_BUTTON, ControllerId.TWO, "BUTTON_2", "Controller Button 2"),
    BUTTON_3(InputType.CONTROLLER_BUTTON, ControllerId.THREE, "BUTTON_3", "Controller Button 3"),
    BUTTON_4(InputType.CONTROLLER_BUTTON, ControllerId.FOUR, "BUTTON_4", "Controller Button 4"),
    BUTTON_5(InputType.CONTROLLER_BUTTON, ControllerId.FIVE, "BUTTON_5", "Controller Button 5"),
    BUTTON_6(InputType.CONTROLLER_BUTTON, ControllerId.SIX, "BUTTON_6", "Controller Button 6"),
    BUTTON_7(InputType.CONTROLLER_BUTTON, ControllerId.SEVEN, "BUTTON_7", "Controller Button 7"),
    BUTTON_8(InputType.CONTROLLER_BUTTON, ControllerId.EIGHT, "BUTTON_8", "Controller Button 8"),
    BUTTON_9(InputType.CONTROLLER_BUTTON, ControllerId.NINE, "BUTTON_9", "Controller Button 9"),
    BUTTON_10(InputType.CONTROLLER_BUTTON, ControllerId.TEN, "BUTTON_10", "Controller Button 10"),
    BUTTON_11(InputType.CONTROLLER_BUTTON, ControllerId.ELEVEN, "BUTTON_11", "Controller Button 11");

    private int id;
    private String displayName;
    private String name;
    private InputType type;

    private ControllerInput(InputType type, int id, String name, String displayName) {
        this.type = type;
        this.id = id;
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.displayName = displayName;
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
        for (ControllerInput input : values()) {
            if (input.type == type && input.id == id) {
                return input;
            }
        }
        return NONE;
    }

}
