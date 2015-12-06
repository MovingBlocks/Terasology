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
package org.terasology.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.math.TeraMath;

/**
 * An enum for different screenshot size presets
 */
public enum ScreenshotSize {

    DOUBLE_SIZE("Double Size", 2.0F),
    NORMAL_SIZE("Normal Size", 1.0F),
    HALF_SIZE("Half Size", 0.5F),
    QUARTER_SIZE("Quarter Size", 0.25F),
    HD720("720p", 1280, 720),
    HD1080("1080p", 1920, 1080);

    private final String displayName;

    private float multiplier;

    private int width;
    private int height;

    private ScreenshotSize(String displayName, float multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    private ScreenshotSize(String displayName, int width, int height) {
        this.displayName = displayName;
        this.width = width;
        this.height = height;
    }

    /**
     * @param displayWidth the width of the window
     * @return the width of the screenshot
     */
    public int getWidth(int displayWidth) {
        if (multiplier == 0) {
            return width;
        } else {
            return TeraMath.floorToInt(displayWidth * multiplier);
        }
    }

    /**
     * @param displayHeight the height of the window
     * @return the height of the screenshot
     */
    public int getHeight(int displayHeight) {
        if (multiplier == 0) {
            return height;
        } else {
            return TeraMath.floorToInt(displayHeight * multiplier);
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
