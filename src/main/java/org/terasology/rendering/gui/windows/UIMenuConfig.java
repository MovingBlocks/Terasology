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
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.ClickListener;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIMenuConfig extends UIDisplayWindow {

    final UIGraphicsElement _title;
    final UIText _version;

    private final UIButton _backToMainMenuButton;
    private final UIButton _videoButton;
    private final UIButton _audioButton;
    private final UIButton _controlsButton;
    private final UIButton _modsButton;

    public UIMenuConfig() {
    	setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Settings");
        _version.setVisible(true);

        _videoButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _videoButton.getLabel().setText("Video");
        _videoButton.setVisible(true);
        _videoButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigVideo"));
            }
        });

        _audioButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _audioButton.getLabel().setText("Audio");
        _audioButton.setVisible(true);
        _audioButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigAudio"));
            }
        });

        _controlsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _controlsButton.getLabel().setText("Controls");
        _controlsButton.setVisible(true);
        _controlsButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigControls"));
            }
        });

        _modsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _modsButton.getLabel().setText("Mods");
        _modsButton.setVisible(true);
        _modsButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigMods"));
            }
        });

        _backToMainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _backToMainMenuButton.getLabel().setText("Return to Main Menu");
        _backToMainMenuButton.setVisible(true);
        _backToMainMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuMain"));
            }
        });

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_videoButton, "videoButton");
        addDisplayElement(_audioButton, "audioButton");
        addDisplayElement(_controlsButton, "controlsButton");
        addDisplayElement(_modsButton, "modsButton");
        addDisplayElement(_backToMainMenuButton, "backToMainMenuButton");

        layout();
    }
    
    @Override
    public void layout() {
        super.layout();
        
        if (_version != null) {
            _version.centerHorizontally();
            _version.getPosition().y = 230f;
    
            _videoButton.centerHorizontally();
            _videoButton.getPosition().y = 300f;
    
            _audioButton.centerHorizontally();
            _audioButton.getPosition().y = 300f + 40f;
    
            _controlsButton.centerHorizontally();
            _controlsButton.getPosition().y = 300f + 2 * 40f;
    
            _modsButton.centerHorizontally();
            _modsButton.getPosition().y = 300f + 3 * 40f;
    
            _backToMainMenuButton.centerHorizontally();
            _backToMainMenuButton.getPosition().y = 300f + 7 * 40f;
    
            _title.centerHorizontally();
            _title.getPosition().y = 128f;
        }
    }
}
