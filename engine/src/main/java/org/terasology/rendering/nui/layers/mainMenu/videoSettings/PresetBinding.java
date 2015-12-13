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
import org.terasology.rendering.nui.databinding.Binding;

/**
 */
public class PresetBinding implements Binding<Preset> {
    private RenderingConfig config;

    public PresetBinding(RenderingConfig config) {
        this.config = config;
    }

    @Override
    public Preset get() {
        if (config.isFlickeringLight() && config.isVignette() && config.isEyeAdaptation() && config.isFilmGrain()) {
            if (config.isSsao()) {
                if (config.isBloom() && config.isMotionBlur() && config.isLightShafts() && config.isCloudShadows()) {
                    return Preset.UBER;
                }
            } else if (config.isCloudShadows()) {
                if (config.isBloom() && config.isMotionBlur() && config.isLightShafts()) {
                    return Preset.INSANE;
                }
            } else if (config.isBloom()) {
                if (!config.isMotionBlur() && !config.isLightShafts()) {
                    return Preset.EPIC;
                }
            } else if (!config.isMotionBlur() && !config.isLightShafts()) {
                return Preset.NICE;
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

