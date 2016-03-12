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
import org.terasology.i18n.TranslationSystem;

/**
 */
public enum Preset {
    MINIMAL("video-preset-minimal") {
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
        }
    },
    NICE("video-preset-nice") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(true);
            renderConfig.setVignette(true);
            renderConfig.setEyeAdaptation(true);
            renderConfig.setFilmGrain(true);

            renderConfig.setBloom(false);
            renderConfig.setMotionBlur(false);
            renderConfig.setSsao(false);
            renderConfig.setLightShafts(false);
            renderConfig.setCloudShadows(false);
        }
    },
    EPIC("video-preset-epic") {
        @Override
        public void apply(RenderingConfig renderConfig) {
            renderConfig.setFlickeringLight(true);
            renderConfig.setVignette(true);
            renderConfig.setEyeAdaptation(true);
            renderConfig.setFilmGrain(true);
            renderConfig.setBloom(true);

            renderConfig.setSsao(false);
            renderConfig.setMotionBlur(false);
            renderConfig.setLightShafts(false);
            renderConfig.setCloudShadows(false);
        }
    },
    INSANE("video-preset-insane") {
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

            renderConfig.setSsao(false);
        }
    },
    UBER("video-preset-uber") {
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

        }
    },
    CUSTOM("video-preset-custom") {
        @Override
        public void apply(RenderingConfig renderConfig) {
        }
    };

    private String displayName;

    private Preset(String displayName) {
        this.displayName = displayName;
    }

    public abstract void apply(RenderingConfig renderConfig);
    private TranslationSystem translationSystem;

    @Override
    public String toString() {

        // "${engine:menu#video-preset-custom}" basic format
        //return translationSystem.translate("$engine:menu#"+displayName+"}"); //should be constructed properly
        return translationSystem.translate("${engine:menu#video-preset-custom}"); //random test
    }
}
