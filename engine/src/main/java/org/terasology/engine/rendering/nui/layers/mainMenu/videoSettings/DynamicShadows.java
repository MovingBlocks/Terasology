// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;

/**
 */
public enum DynamicShadows {
    OFF("${engine:menu#shadows-off}", false, false),
    ON("${engine:menu#shadows-on}", true, false),
    ON_PCF("${engine:menu#shadows-pcr}", true, true);

    private final String displayName;
    private final boolean shadow;
    private final boolean pcf;

    DynamicShadows(String displayName, boolean shadow, boolean pcf) {
        this.displayName = displayName;
        this.shadow = shadow;
        this.pcf = pcf;
    }

    public void apply(RenderingConfig renderConfig) {
        renderConfig.setDynamicShadows(shadow);
        renderConfig.setDynamicShadowsPcfFiltering(pcf);
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static DynamicShadows find(boolean dynamicShadows, boolean pcf) {
        if (dynamicShadows) {
            return (pcf) ? ON_PCF : ON;
        }
        return OFF;
    }
}
