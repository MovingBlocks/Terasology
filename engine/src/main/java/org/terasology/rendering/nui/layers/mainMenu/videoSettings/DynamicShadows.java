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
public enum DynamicShadows {
    OFF("Off", false, false),
    ON("On", true, false),
    ON_PCF("On (PCF)", true, true);

    private String displayName;
    private boolean shadow;
    private boolean pcf;

    private DynamicShadows(String displayName, boolean shadow, boolean pcf) {
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
