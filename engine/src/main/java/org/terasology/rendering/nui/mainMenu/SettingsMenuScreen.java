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
package org.terasology.rendering.nui.mainMenu;

import org.terasology.config.Config;
import org.terasology.entitySystem.systems.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.mainMenu.inputSettings.InputSettingsScreen;
import org.terasology.rendering.nui.widgets.ActivateEventListener;

/**
 * @author Immortius
 */
public class SettingsMenuScreen extends UIScreenLayer {

    @In
    private NUIManager nuiManager;

    @In
    private Config config;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "video", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                nuiManager.pushScreen("engine:VideoMenuScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "audio", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                nuiManager.pushScreen("engine:AudioMenuScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "input", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                UIScreenLayer inputScreen = new InputSettingsScreen();
                inputScreen.setSkin(getSkin());
                nuiManager.pushScreen(inputScreen);
            }
        });
        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                config.save();
                nuiManager.popScreen();
            }
        });
    }
}
