// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.settings;

import org.terasology.engine.config.Config;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.WidgetUtil;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.inputSettings.InputSettingsScreen;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.VideoSettingsScreen;

/**
 */
public class SettingsMenuScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:settingsMenuScreen");

    @In
    private Config config;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "player", button -> triggerForwardAnimation(PlayerSettingsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "video", button -> triggerForwardAnimation(VideoSettingsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "audio", button -> triggerForwardAnimation(AudioSettingsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "input", button -> triggerForwardAnimation(InputSettingsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "close", button -> {
            config.save();
            triggerBackAnimation();
        });
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
