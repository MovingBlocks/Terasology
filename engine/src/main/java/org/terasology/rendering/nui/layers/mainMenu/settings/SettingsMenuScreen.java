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
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.inputSettings.InputSettingsScreen;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.VideoSettingsScreen;

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
