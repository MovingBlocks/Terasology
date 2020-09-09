// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;


import org.terasology.nui.Color;

/**
 *
 */
public enum CoreMessageType implements MessageType {
    CONSOLE(ConsoleColors.DEFAULT),
    CHAT(ConsoleColors.CHAT),
    ERROR(ConsoleColors.ERROR),
    NOTIFICATION(ConsoleColors.NOTIFICATION);

    private final Color color;

    CoreMessageType(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
