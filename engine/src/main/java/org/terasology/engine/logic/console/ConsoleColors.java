// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.console;

import org.terasology.nui.Color;

/**
 * Defines a set of often-used color constants for the console
 */
public final class ConsoleColors {
    
    /**
     * Error color
     */
    public static final Color ERROR = new Color(224, 128, 128);

    /**
     * Command color
     */
    public static final Color COMMAND = new Color(196, 196, 224);
    
    /**
     * Chat text color
     */
    public static final Color CHAT = new Color(208, 208, 224);
    
    /**
     * Notification color
     */
    public static final Color NOTIFICATION = new Color(208, 208, 224);

    /**
     * Default console color
     */
    public static final Color DEFAULT = Color.WHITE;


    private ConsoleColors() {
        // avoid instantiation
    }
}
