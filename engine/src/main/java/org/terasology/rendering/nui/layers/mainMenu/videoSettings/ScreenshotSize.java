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

    SUPER("Super Size", 0) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(0);
        }
    },
    NORMAL("Normal Size", 1) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(1);
        }
    },
    SMALL("Small Size", 2) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(2);
        }
    },
    THUMBNAIL("Thumbnail", 3) {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotSize(3);
        }
    };


    private String displayName;
    private int index;

    private ScreenshotSize(String displayName, int index) {
        this.displayName = displayName;
        this.index = index;
    }

    public abstract void apply(RenderingConfig config);

    public String getDisplayName() {
        return displayName;
    }

    public int getIndex() {
        return index;
    }
}
