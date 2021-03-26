// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;

/**
 */
public enum Preset {
    MINIMAL("${engine:menu#video-preset-minimal}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(false);
            renderConfig.setVignette(false);
            renderConfig.setEyeAdaptation(false);
            renderConfig.setFilmGrain(false);

            renderConfig.setBloom(false);
            renderConfig.setMotionBlur(false);
            renderConfig.setSsao(false);
            renderConfig.setLightShafts(false);
            renderConfig.setCloudShadows(false);
            renderConfig.setNormalMapping(false);
        }
    },
    LOW("${engine:menu#video-preset-low}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(true);
            renderConfig.setVignette(true);
            renderConfig.setEyeAdaptation(true);
            renderConfig.setFilmGrain(true);
            renderConfig.setNormalMapping(true);

            renderConfig.setBloom(false);
            renderConfig.setMotionBlur(false);
            renderConfig.setSsao(false);
            renderConfig.setLightShafts(false);
            renderConfig.setCloudShadows(false);
        }
    },
    MEDIUM("${engine:menu#video-preset-medium}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(true);
            renderConfig.setVignette(true);
            renderConfig.setEyeAdaptation(true);
            renderConfig.setFilmGrain(true);
            renderConfig.setBloom(true);
            renderConfig.setNormalMapping(true);

            renderConfig.setSsao(false);
            renderConfig.setMotionBlur(false);
            renderConfig.setLightShafts(false);
            renderConfig.setCloudShadows(false);
        }
    },
    HIGH("${engine:menu#video-preset-high}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(true);
            renderConfig.setVignette(true);
            renderConfig.setEyeAdaptation(true);
            renderConfig.setFilmGrain(true);
            renderConfig.setBloom(true);
            renderConfig.setMotionBlur(true);
            renderConfig.setLightShafts(true);
            renderConfig.setCloudShadows(true);
            renderConfig.setNormalMapping(true);

            renderConfig.setSsao(false);
        }
    },
    ULTRA("${engine:menu#video-preset-ultra}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(true);
            renderConfig.setVignette(true);
            renderConfig.setEyeAdaptation(true);
            renderConfig.setFilmGrain(true);
            renderConfig.setBloom(true);
            renderConfig.setMotionBlur(true);
            renderConfig.setSsao(true);
            renderConfig.setLightShafts(true);
            renderConfig.setCloudShadows(true);
            renderConfig.setAnimateGrass(true);
            renderConfig.setNormalMapping(true);

        }
    },
    CUSTOM("${engine:menu#video-preset-custom}") {
        @Override
        public void apply(RenderingConfig renderConfig) {
        }
    };

    private String displayName;

     Preset(String displayName) {
        this.displayName = displayName;
    }

    public abstract void apply(RenderingConfig renderConfig);

    @Override
    public String toString() {
        return displayName;
    }
}
