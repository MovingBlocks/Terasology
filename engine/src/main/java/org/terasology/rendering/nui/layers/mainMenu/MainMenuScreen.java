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

import org.terasology.engine.GameEngine;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.layers.mainMenu.settings.SettingsMenuScreen;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.version.TerasologyVersion;

/**
 */
public class MainMenuScreen extends CoreScreenLayer {

    @In
    private GameEngine engine;

    @Override
    public void initialise() {

        UILabel versionLabel = find("version", UILabel.class);
        versionLabel.setText(TerasologyVersion.getInstance().getHumanVersion());

        WidgetUtil.trySubscribe(this, "singleplayer", button -> {
            SelectGameScreen screen = getManager().createScreen(SelectGameScreen.ASSET_URI, SelectGameScreen.class);
            screen.setLoadingAsServer(false);
            // in theory, the screen can be garbage collected now and the information will be lost
            // TODO: pass the screen directly to NUIManger.push()
            triggerForwardAnimation(SelectGameScreen.ASSET_URI);
        });
        WidgetUtil.trySubscribe(this, "multiplayer", button -> {
            SelectGameScreen screen = getManager().createScreen(SelectGameScreen.ASSET_URI, SelectGameScreen.class);
            screen.setLoadingAsServer(true);
            // in theory, the screen can be garbage collected now and the information will be lost
            // TODO: pass the screen directly to NUIManger.push()
            triggerForwardAnimation(SelectGameScreen.ASSET_URI);
        });
        WidgetUtil.trySubscribe(this, "join", button -> triggerForwardAnimation(JoinGameScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "settings", button -> triggerForwardAnimation(SettingsMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "exit", button -> engine.shutdown());
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}


