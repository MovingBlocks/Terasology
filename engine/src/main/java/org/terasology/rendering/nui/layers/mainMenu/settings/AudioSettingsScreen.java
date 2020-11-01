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
package org.terasology.rendering.nui.layers.mainMenu.settings;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.AudioConfig;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UISlider;

/**
 */
public class AudioSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:AudioMenuScreen");

    @In
    private AudioConfig config;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        // TODO: Remove this screen when AutoConfig UI is in place

        UISlider sound = find("sound", UISlider.class);
        if (sound != null) {
            sound.setIncrement(0.05f);
            sound.setPrecision(2);
            sound.setMinimum(0);
            sound.setRange(1.0f);
            sound.bindValue(new Binding<Float>() {
                @Override
                public Float get() {
                    return config.soundVolume.get();
                }

                @Override
                public void set(Float value) {
                    config.soundVolume.set(value);
                }
            });
        }

        UISlider music = find("music", UISlider.class);
        if (music != null) {
            music.setIncrement(0.05f);
            music.setPrecision(2);
            music.setMinimum(0);
            music.setRange(1.0f);
            music.bindValue(new Binding<Float>() {
                @Override
                public Float get() {
                    return config.musicVolume.get();
                }

                @Override
                public void set(Float value) {
                    config.musicVolume.set(value);
                }
            });
        }

        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

}
