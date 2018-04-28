/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.logic;

import org.terasology.rendering.nui.Color;

/**
 * Makes the game render the specified text at the current location of the enitity.
 */
public class FloatingTextComponent implements VisualComponent {
    public String text;
    public Color textColor = Color.WHITE;
    public Color textShadowColor = Color.BLACK;
    public float scale = 1f;
    public boolean isOverlay;
}
