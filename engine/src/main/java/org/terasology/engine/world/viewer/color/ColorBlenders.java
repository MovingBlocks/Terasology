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

package org.terasology.world.viewer.color;

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

