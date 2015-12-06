/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.logic.console;

import org.terasology.rendering.nui.Color;

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
