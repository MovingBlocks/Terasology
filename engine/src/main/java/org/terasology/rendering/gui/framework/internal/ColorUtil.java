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

package org.terasology.rendering.gui.framework.internal;

import org.newdawn.slick.Color;

/**
 * This is a temporary workaround to allow modules to use GUI color methods without a dependency on org.newdawn.slick.Color.
 * TODO: Remove this class after NUI is available.
 * 
 * @author mkienenb
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    private static float rgbToColor(int v) {
        return (float) v / 255.0f;
    }

    public static Color getColorForColorHexString(String color) {
        String normalisedColor = color.trim().toLowerCase();

        int r = 0;
        int g = 0;
        int b = 0;
        int a = 255;

        if (normalisedColor.matches("^#[a-f0-9]{1,8}$")) {
            normalisedColor = normalisedColor.replace("#", "");

            // Cannot parseInt something that will not fit into a signed int,
            // so pull the alpha off the string
            String normalisedColorA = null;
            if (normalisedColor.length() > 6) {
                normalisedColorA = normalisedColor.substring(0, normalisedColor.length() - 6);
                normalisedColor = normalisedColor.substring(normalisedColor.length() - 6);
            }
            int sum = Integer.parseInt(normalisedColor, 16);

            if (null != normalisedColorA) {
                int sumA = Integer.parseInt(normalisedColorA, 16);
                a = sumA & 0x000000FF;
            }

            r = (sum & 0x00FF0000) >> 16;
            g = (sum & 0x0000FF00) >> 8;
            b = sum & 0x000000FF;
        }

        return new Color(rgbToColor(r), rgbToColor(g), rgbToColor(b), rgbToColor(a));
    }

}
