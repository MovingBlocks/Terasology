// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.terasology.nui.Color;

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
