// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;

/**
 */
public enum WaterReflection {
    SKY("${engine:menu#water-reflections-sky}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setReflectiveWater(false);
            renderConfig.setLocalReflections(false);
        }
    },
    GLOBAL("${engine:menu#water-reflections-global}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setReflectiveWater(true);
            renderConfig.setLocalReflections(false);
        }
    },
    LOCAL("${engine:menu#water-reflections-ssr}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setReflectiveWater(false);
            renderConfig.setLocalReflections(true);
        }
    };

    private String displayName;

    WaterReflection(String displayName) {
        this.displayName = displayName;
    }

    public abstract void apply(RenderingConfig renderConfig);

    @Override
    public String toString() {
        return displayName;
    }
}
