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

public enum ScreenshotFormat {

    PNG("PNG") {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotFormat(PNG);
        }
    },
    JPEG("JPEG") {
        @Override
        public void apply(RenderingConfig config) {
            config.setScreenshotFormat(JPEG);
        }
    };

    private String displayName;

    private ScreenshotFormat(String displayName) {
        this.displayName = displayName;
    }

    public abstract void apply(RenderingConfig config);

    @Override
    public String toString() {
        return displayName;
    }
}
