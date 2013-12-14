/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.mainMenu.videoSettings;

import org.terasology.config.RenderingConfig;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * @author Immortius
 */
public class EnvironmentEffectsBinding implements Binding<EnvironmentalEffects> {

    private RenderingConfig config;

    public EnvironmentEffectsBinding(RenderingConfig config) {
        this.config = config;
    }

    @Override
    public EnvironmentalEffects get() {
        if (config.isVolumetricLighting()) {
            if (config.isAnimateWater() && config.isAnimateGrass() && config.isVolumetricFog()) {
                return EnvironmentalEffects.HIGH;
            }
        } else if (config.isAnimateWater()) {
            if (config.isAnimateGrass() && config.isVolumetricFog()) {
                return EnvironmentalEffects.MEDIUM;
            }
        } else if (config.isAnimateGrass()) {
            if (config.isVolumetricFog()) {
                return EnvironmentalEffects.LOW;
            }
        } else if (!config.isVolumetricFog()) {
            return EnvironmentalEffects.OFF;
        }
        return EnvironmentalEffects.CUSTOM;
    }

    @Override
    public void set(EnvironmentalEffects value) {
        value.apply(config);
    }
}
