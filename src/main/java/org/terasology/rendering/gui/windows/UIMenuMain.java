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
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.ClickListener;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuMain extends UIDisplayWindow {

    private final UIImageOverlay _overlay;
    private final UIGraphicsElement _title;


    private final UIButton _exitButton;
    private final UIButton _singlePlayerButton;
    private final UIButton _configButton;

    final UIText _version;

    public UIMenuMain() {
        setModal(true);
        maximize();
        
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Pre Alpha");
        _version.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.setVisible(true);
        _exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });

        _configButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _configButton.getLabel().setText("Settings");
        _configButton.setVisible(true);
        _configButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfig"));
            }
        });

        _singlePlayerButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _singlePlayerButton.getLabel().setText("Single player");
        _singlePlayerButton.setVisible(true);
        _singlePlayerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("selectWorld"));
            }
        });

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:menuBackground"));
        _overlay.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);
        addDisplayElement(_configButton, "configButton");
        addDisplayElement(_exitButton, "exitButton");
        addDisplayElement(_singlePlayerButton, "singlePlayerButton");

        layout();
    }

    @Override
    public void layout() {
        super.layout();

        if (_version != null) {
            _version.centerHorizontally();
            _version.getPosition().y = 230f;
    
            _singlePlayerButton.centerHorizontally();
            _singlePlayerButton.getPosition().y = 300f + 40f;
    
            _configButton.centerHorizontally();
            _configButton.getPosition().y = 300f + 2 * 40f;
    
            _exitButton.centerHorizontally();
            _exitButton.getPosition().y = 300f + 4 * 40f;
    
            _title.centerHorizontally();
            _title.getPosition().y = 128f;
        }
    }
}
