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
 * @author Martin Steiger
 */
public final class ConsoleColors {
    
    /**
     * For the word "Terasology"
     */
    public static final Color TERASOLOGY = new Color(0, 128, 0);
    
    /**
     * Command color
     */
    public static final Color COMMAND = new Color(32, 32, 224);
    
    /**
     * Player name color
     */
    public static final Color PLAYER = new Color(32, 32, 224);
    
    /**
     * Area name color
     */
    public static final Color AREA = new Color(32, 32, 224);


    private ConsoleColors() {
        // avoid instantiation
    }
}
