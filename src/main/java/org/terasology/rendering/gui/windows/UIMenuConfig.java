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
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIText;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIMenuConfig extends UIDisplayWindow {

    final UIImage _title;
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
        
        _title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        _title.setHorizontalAlign(EHorizontalAlign.CENTER);
        _title.setPosition(new Vector2f(0f, 128f));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Settings");
        _version.setHorizontalAlign(EHorizontalAlign.CENTER);
        _version.setPosition(new Vector2f(0f, 230f));
        _version.setVisible(true);

        _videoButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _videoButton.getLabel().setText("Video");
        _videoButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _videoButton.setPosition(new Vector2f(0f, 300f));
        _videoButton.setVisible(true);
        _videoButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigVideo"));
            }
        });

        _audioButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _audioButton.getLabel().setText("Audio");
        _audioButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _audioButton.setPosition(new Vector2f(0f, 300f + 40f));
        _audioButton.setVisible(true);
        _audioButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigAudio"));
            }
        });

        _controlsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _controlsButton.getLabel().setText("Controls");
        _controlsButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _controlsButton.setPosition(new Vector2f(0f, 300f + 2 * 40f));
        _controlsButton.setVisible(true);
        _controlsButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigControls"));
            }
        });

        _modsButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _modsButton.getLabel().setText("Mods");
        _modsButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _modsButton.setPosition(new Vector2f(0f, 300f + 3 * 40f));
        _modsButton.setVisible(true);
        _modsButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuConfigMods"));
            }
        });

        _backToMainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _backToMainMenuButton.getLabel().setText("Return to Main Menu");
        _backToMainMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _backToMainMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        _backToMainMenuButton.setVisible(true);
        _backToMainMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().setFocusedWindow(GUIManager.getInstance().getWindowById("menuMain"));
            }
        });

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_videoButton);
        addDisplayElement(_audioButton);
        addDisplayElement(_controlsButton);
        addDisplayElement(_modsButton);
        addDisplayElement(_backToMainMenuButton);
    }
}
