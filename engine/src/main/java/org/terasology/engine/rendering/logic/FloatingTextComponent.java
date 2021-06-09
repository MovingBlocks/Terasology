// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.terasology.nui.Color;

/**
 * Makes the game render the specified text at the current location of the enitity.
 */
public class FloatingTextComponent implements VisualComponent<FloatingTextComponent> {
    public String text;
    public Color textColor = Color.WHITE;
    public Color textShadowColor = Color.BLACK;
    public float scale = 1f;
    public boolean isOverlay;

    @Override
    public void copy(FloatingTextComponent other) {
        this.text = other.text;
        this.textColor = other.textColor;
        this.textShadowColor = other.textShadowColor;
        this.scale = other.scale;
        this.isOverlay = other.isOverlay;
    }
}
