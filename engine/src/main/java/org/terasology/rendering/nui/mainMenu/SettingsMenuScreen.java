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
package org.terasology.rendering.nui.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.entitySystem.systems.In;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreenUtil;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.baseWidgets.UISpace;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;
import org.terasology.rendering.nui.mainMenu.inputSettings.InputSettingsScreen;
import org.terasology.rendering.nui.mainMenu.videoSettings.VideoSettingsScreen;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class SettingsMenuScreen extends UIScreen {
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenuScreen.class);

    @In
    private NUIManager nuiManager;

    @In
    private Config config;

    @Override
    public void initialise() {
        UIScreenUtil.trySubscribe(this, "video", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.pushScreen("engine:VideoMenuScreen");
            }
        });
        UIScreenUtil.trySubscribe(this, "audio", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.pushScreen("engine:AudioMenuScreen");
            }
        });
        UIScreenUtil.trySubscribe(this, "input", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                UIScreen inputScreen = new InputSettingsScreen();
                inputScreen.setSkin(getSkin());
                nuiManager.pushScreen(inputScreen);
            }
        });
        UIScreenUtil.trySubscribe(this, "close", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                config.save();
                nuiManager.popScreen();
            }
        });
    }
}
