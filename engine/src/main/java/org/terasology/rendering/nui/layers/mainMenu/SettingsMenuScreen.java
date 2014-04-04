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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.layers.mainMenu.inputSettings.InputSettingsScreen;
import org.terasology.rendering.nui.widgets.ActivateEventListener;

/**
 * @author Immortius
 */
public class SettingsMenuScreen extends CoreScreenLayer {

    private static final AssetUri INPUT_SCREEN_URI = new AssetUri(AssetType.UI_ELEMENT, "engine:inputScreen");

    @In
    private Config config;

    @Override
    public void initialise() {
        CoreScreenLayer inputScreen = new InputSettingsScreen();
        inputScreen.setSkin(getSkin());
        UIData inputScreenData = new UIData(inputScreen);
        Assets.generateAsset(INPUT_SCREEN_URI, inputScreenData, UIElement.class);
        WidgetUtil.trySubscribe(this, "player", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:PlayerMenuScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "video", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:VideoMenuScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "audio", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:AudioMenuScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "input", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen(INPUT_SCREEN_URI);
            }
        });
        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                config.save();
                getManager().popScreen();
            }
        });
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
