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

import java.awt.image.DirectColorModel;

/**
 * A collection of constants for different color models.
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
