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

import org.terasology.config.RenderingConfig;

public enum ScreenshotSize {

    DOUBLE_SIZE("Super Size", 2.0F) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(DOUBLE_SIZE);
        }
    },
    NORMAL_SIZE("Normal Size", 1.0F) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(NORMAL_SIZE);
        }
    },
    HALF_SIZE("Small Size", 0.5F) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(HALF_SIZE);
        }
    },
    QUARTER_SIZE("Thumbnail", 0.25F) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(QUARTER_SIZE);
        }
    },
    HD720("720p", 1280, 720) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(HD720);
        }
    },
    HD1080("1080p", 1920, 1080) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(HD1080);
        }
    };


    private String displayName;
    private float multiplier;

    private int width;
    private int height;

    private boolean isWithMultiplier;

    private ScreenshotSize(String displayName, float multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
        this.isWithMultiplier = true;
    }

    private ScreenshotSize(String displayName, int width, int height) {
        this.displayName = displayName;
        this.width = width;
        this.height = height;
        this.isWithMultiplier = false;
    }

    public abstract void apply(RenderingConfig config);

    public float getMultiplier() {
        return multiplier;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isWithMultiplier() {
        return isWithMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
