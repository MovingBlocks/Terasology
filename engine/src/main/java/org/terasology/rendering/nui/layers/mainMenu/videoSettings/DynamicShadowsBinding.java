// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;
import org.terasology.nui.databinding.Binding;

/**
 */
public class DynamicShadowsBinding implements Binding<DynamicShadows> {

    private RenderingConfig config;

    public DynamicShadowsBinding(RenderingConfig config) {
        this.config = config;
    }

    @Override
    public DynamicShadows get() {
        return DynamicShadows.find(config.isDynamicShadows(), config.isDynamicShadowsPcfFiltering());
    }

    @Override
    public void set(DynamicShadows value) {
        value.apply(config);
    }
}
