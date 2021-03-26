// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.viewer.color;

import java.awt.image.ColorModel;

/**
 * Maps ColorModel instances to Blender instances.
 */
public final class ColorBlenders {

    private static final ColorBlenderArgb BLENDER_ARGB = new ColorBlenderArgb();
    private static final ColorBlenderRgba BLENDER_RGBA = new ColorBlenderRgba();

    private ColorBlenders() {
        // no instances
    }

    /**
     * Tries to find a {@link ColorBlender} mapping from source to target.
     * @param source the source color model
     * @param target the target color model
     * @return the corresponding color blender
     * @throws UnsupportedOperationException if color model is not supported
     */
    public static ColorBlender forColorModel(ColorModel source, ColorModel target) {
        if (!ColorModels.RGBA.equals(source)) {
            throw new UnsupportedOperationException("source colormodel: " + source);
        }

        if (ColorModels.ARGB.equals(target)) {
            return BLENDER_ARGB;
        }

        if (ColorModels.RGBA.equals(target)) {
            return BLENDER_RGBA;
        }

        throw new UnsupportedOperationException("target colormodel: " + target);
    }
}

