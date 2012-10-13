/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.windows;

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuMain extends UIWindow {

    private final UIImage _title;

    private final UIButton _exitButton;
    private final UIButton _singlePlayerButton;
    private final UIButton _configButton;

    final UILabel _version;

    public UIMenuMain() {
        setId("main");
        setBackgroundImage("engine:menubackground");
        setModal(true);
        maximize();
        
        _title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        _title.setSize(new Vector2f(512f, 128f));
        _title.setHorizontalAlign(EHorizontalAlign.CENTER);
        _title.setPosition(new Vector2f(0f, 128f));
        _title.setVisible(true);

        _version = new UILabel("Pre Alpha");
        _version.setHorizontalAlign(EHorizontalAlign.CENTER);
        _version.setPosition(new Vector2f(0f, 230f));
        _version.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });
        _exitButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _exitButton.setPosition(new Vector2f(0f, 300f + 4 * 40f));
        _exitButton.setVisible(true);
        
        _configButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _configButton.getLabel().setText("Settings");
        _configButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config");
            }
        });
        _configButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _configButton.setPosition(new Vector2f(0f, 300f + 2 * 40f));
        _configButton.setVisible(true);

        _singlePlayerButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _singlePlayerButton.getLabel().setText("Single player");
        _singlePlayerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("singleplayer");
            }
        });

        _singlePlayerButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _singlePlayerButton.setPosition(new Vector2f(0f, 300f + 40f));
        _singlePlayerButton.setVisible(true);

        addDisplayElement(_title);
        addDisplayElement(_version);
        addDisplayElement(_configButton);
        addDisplayElement(_exitButton);
        addDisplayElement(_singlePlayerButton);
    }
}
