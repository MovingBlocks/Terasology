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

import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.layers.mainMenu.inputSettings.InputSettingsScreen;

/**
 */
public class SettingsMenuScreen extends CoreScreenLayer {

    @In
    private Config config;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "player", button -> getManager().pushScreen("engine:PlayerMenuScreen"));
        WidgetUtil.trySubscribe(this, "video", button -> getManager().pushScreen("engine:VideoMenuScreen"));
        WidgetUtil.trySubscribe(this, "audio", button -> getManager().pushScreen("engine:AudioMenuScreen"));
        WidgetUtil.trySubscribe(this, "input", button -> getManager().pushScreen(InputSettingsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "close", button -> {
            config.save();
            getManager().popScreen();
        });
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
