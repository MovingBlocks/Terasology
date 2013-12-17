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

import org.terasology.asset.Assets;
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.systems.In;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.baseWidgets.UISpace;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class MainMenuScreen extends UIScreen {

    @In
    private GameEngine engine;

    @In
    private NUIManager nuiManager;

    public MainMenuScreen() {
        ColumnLayout grid = new ColumnLayout();
        grid.addWidget(new UIButton("singleplayer", "Single Player"));
        grid.addWidget(new UIButton("multiplayer", "Host Game"));
        grid.addWidget(new UIButton("join", "Join Game"));
        grid.addWidget(new UIButton("settings", "Settings"));
        grid.addWidget(new UISpace());
        grid.addWidget(new UIButton("exit", "Exit"));
        grid.setPadding(new Border(0, 0, 4, 4));

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.2f));
        layout.addFillWidget(new UILabel("version", "title", "Pre Alpha"), Rect2f.createFromMinAndSize(0.0f, 0.3f, 1.0f, 0.1f));
        layout.addFixedWidget(grid, new Vector2i(280, 192), new Vector2f(0.5f, 0.7f));

        setContents(layout);
    }

    @Override
    public void initialise() {
        find("singleplayer", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                // Open
            }
        });
        find("multiplayer", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                // Open
            }
        });
        find("settings", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                UIScreen settings = new SettingsMenuScreen();
                settings.setSkin(getSkin());
                nuiManager.pushScreen(settings);
            }
        });
        find("exit", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                engine.shutdown();
            }
        });
    }
}
