// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.math.TeraMath;

/**
 * An enum for different screenshot size presets
 */
public enum ScreenshotSize {

    QUARTER_SIZE("${engine:menu#screenshot-size-quarter}", 0.25F),
    HALF_SIZE("${engine:menu#screenshot-size-half}", 0.5F),
    NORMAL_SIZE("${engine:menu#screenshot-size-normal}", 1.0F),
    DOUBLE_SIZE("${engine:menu#screenshot-size-double}", 2.0F),
    HD720("720p", 1280, 720),
    HD1080("1080p", 1920, 1080),
    UHD_1("4K UHD", 3840, 2160); // see: https://en.wikipedia.org/wiki/4K_resolution

    private final String displayName;

    private float multiplier;

    private int width;
    private int height;

    ScreenshotSize(String displayName, float multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    ScreenshotSize(String displayName, int width, int height) {
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
