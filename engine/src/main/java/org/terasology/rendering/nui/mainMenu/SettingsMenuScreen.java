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
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIWidget;
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

    public SettingsMenuScreen() {
        ColumnLayout grid = new ColumnLayout();
        grid.addWidget(new UIButton("video", "Video"));
        grid.addWidget(new UIButton("audio", "Audio"));
        grid.addWidget(new UIButton("input", "Input"));
        grid.addWidget(new UISpace());
        grid.addWidget(new UISpace());
        grid.addWidget(new UIButton("close", "Return to Main Menu"));
        grid.setPadding(new Border(0, 0, 4, 4));

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.2f));
        layout.addFillWidget(new UILabel("title", "title", "Settings"), Rect2f.createFromMinAndSize(0.0f, 0.3f, 1.0f, 0.1f));
        layout.addFixedWidget(grid, new Vector2i(280, 192), new Vector2f(0.5f, 0.7f));

        setContents(layout);
    }

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        find("video", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                UIScreen videoScreen = new VideoSettingsScreen();
                videoScreen.setSkin(getSkin());
                nuiManager.pushScreen(videoScreen);
            }
        });
        find("audio", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                UIScreen audioScreen = new AudioSettingsScreen();
                audioScreen.setSkin(getSkin());
                nuiManager.pushScreen(audioScreen);
            }
        });
        find("input", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                UIScreen inputScreen = new InputSettingsScreen();
                inputScreen.setSkin(getSkin());
                nuiManager.pushScreen(inputScreen);
            }
        });
        find("close", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                config.save();
                nuiManager.popScreen();
            }
        });
    }
}
