// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;
import org.terasology.nui.databinding.Binding;

/**
 */
public class WaterReflectionBinding implements Binding<WaterReflection> {

    private RenderingConfig config;

    public WaterReflectionBinding(RenderingConfig config) {
        this.config = config;
    }

    @Override
    public WaterReflection get() {
        if (config.isReflectiveWater()) {
            return WaterReflection.GLOBAL;
        } else if (config.isLocalReflections()) {
            return WaterReflection.LOCAL;
        } else {
            return WaterReflection.SKY;
        }
    }

    @Override
    public void set(WaterReflection value) {
        value.apply(config);
    }
}
