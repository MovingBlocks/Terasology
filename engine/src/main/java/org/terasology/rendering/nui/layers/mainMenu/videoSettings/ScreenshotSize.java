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

import org.lwjgl.opengl.Display;
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
    };


    private String displayName;
    private float multiplier;

    private ScreenshotSize(String displayName, float multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    public abstract void apply(RenderingConfig config);

    public float getMultiplier() {
        return multiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
