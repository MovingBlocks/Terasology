// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;
import org.terasology.nui.databinding.Binding;

public class PresetBinding implements Binding<Preset> {
    private RenderingConfig config;

    public PresetBinding(RenderingConfig config) {
        this.config = config;
    }

    @Override
    public Preset get() {
        if (config.isFlickeringLight() && config.isVignette() && config.isEyeAdaptation() && config.isFilmGrain() && config.isNormalMapping()) {
            if (config.isSsao()) {
                if (config.isBloom() && config.isMotionBlur() && config.isLightShafts() && config.isCloudShadows()) {
                    return Preset.ULTRA;
                }
            } else if (config.isCloudShadows()) {
                if (config.isBloom() && config.isMotionBlur() && config.isLightShafts()) {
                    return Preset.HIGH;
                }
            } else if (config.isBloom()) {
                if (!config.isMotionBlur() && !config.isLightShafts()) {
                    return Preset.MEDIUM;
                }
            } else if (!config.isMotionBlur() && !config.isLightShafts()) {
                return Preset.LOW;
            }
        } else if (!config.isBloom() && !config.isMotionBlur() && !config.isFlickeringLight() && !config.isVignette() && !config.isEyeAdaptation() && !config.isFilmGrain()) {
            return Preset.MINIMAL;
        }
        return Preset.CUSTOM;
    }

    @Override
    public void set(Preset value) {
        value.apply(config);
    }
}

