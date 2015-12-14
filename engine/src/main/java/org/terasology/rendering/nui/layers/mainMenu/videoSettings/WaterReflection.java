/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.config.RenderingConfig;

/**
 */
public enum WaterReflection {
    SKY("Sky Only") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setReflectiveWater(false);
            renderConfig.setLocalReflections(false);
        }
    },
    GLOBAL("Global") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setReflectiveWater(true);
            renderConfig.setLocalReflections(false);
        }
    },
    LOCAL("SSR (EXPERIMENTAL)") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setReflectiveWater(false);
            renderConfig.setLocalReflections(true);
        }
    };

    private String displayName;

    private WaterReflection(String displayName) {
        this.displayName = displayName;
    }

    public abstract void apply(RenderingConfig renderConfig);

    @Override
    public String toString() {
        return displayName;
    }
}
