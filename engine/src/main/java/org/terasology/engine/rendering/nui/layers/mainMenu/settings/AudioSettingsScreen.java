// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.settings;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.AudioConfig;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UISlider;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

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
