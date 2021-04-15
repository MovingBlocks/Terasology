// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.color;

import java.awt.image.DirectColorModel;

/**
 * A collection of constants for different color treeView.
 */
public final class ColorModels {

    /**
     * The default color model for OpenGL
     */
    public static final DirectColorModel RGBA = new DirectColorModel(32,
            0xFF000000,       // Red
            0xFF0000,         // Green
            0xFF00,           // Blue
            0xFF);            // Alpha

    /**
     * The default Swing color model for BufferedImage.TYPE_INT_ARGB
     */
    public static final DirectColorModel ARGB = new DirectColorModel(32,
            0x00ff0000,       // Red
            0x0000ff00,       // Green
            0x000000ff,       // Blue
            0xff000000);      // Alpha



    private ColorModels() {
        // no instances
    }
}
