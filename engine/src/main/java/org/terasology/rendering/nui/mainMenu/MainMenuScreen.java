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

import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.systems.In;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.version.TerasologyVersion;

/**
 * @author Immortius
 */
public class MainMenuScreen extends UIScreenLayer {

    @In
    private GameEngine engine;

    @Override
    public void initialise() {
        find("version", UILabel.class).setText(TerasologyVersion.getInstance().getHumanVersion());
        WidgetUtil.trySubscribe(this, "singleplayer", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:selectGameScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "multiplayer", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                UIScreenLayer screen = getManager().pushScreen("engine:selectGameScreen");
                if (screen instanceof SelectGameScreen) {
                    ((SelectGameScreen) screen).setLoadingAsServer(true);
                }
            }
        });
        WidgetUtil.trySubscribe(this, "join", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                UIScreenLayer screen = getManager().pushScreen("engine:joinGameScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "settings", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:settingsMenuScreen");
            }
        });
        WidgetUtil.trySubscribe(this, "exit", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                engine.shutdown();
            }
        });

        WidgetUtil.trySubscribe(this, "migtest", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().pushScreen("engine:migTestScreen");
            }
        });
    }

}


