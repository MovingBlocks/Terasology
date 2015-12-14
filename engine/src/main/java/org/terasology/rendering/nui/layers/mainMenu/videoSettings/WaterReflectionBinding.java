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
