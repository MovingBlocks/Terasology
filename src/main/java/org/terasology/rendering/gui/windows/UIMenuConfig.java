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
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
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
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIMenuConfig extends UIWindow {

    final UIImage title;
    final UILabel version;

    private final UIButton backToMainMenuButton;
    private final UIButton videoButton;
    private final UIButton audioButton;
    private final UIButton controlsButton;

    public UIMenuConfig() {
        setId("config");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 128f));
        title.setVisible(true);
        title.setSize(new Vector2f(512f, 128f));

        version = new UILabel("Settings");
        version.setHorizontalAlign(EHorizontalAlign.CENTER);
        version.setPosition(new Vector2f(0f, 230f));
        version.setVisible(true);

        videoButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        videoButton.getLabel().setText("Video");
        videoButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        videoButton.setPosition(new Vector2f(0f, 300f));
        videoButton.setVisible(true);
        videoButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config:video");
            }
        });

        audioButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        audioButton.getLabel().setText("Audio");
        audioButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        audioButton.setPosition(new Vector2f(0f, 300f + 40f));
        audioButton.setVisible(true);
        audioButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config:audio");
            }
        });

        controlsButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        controlsButton.getLabel().setText("Controls");
        controlsButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        controlsButton.setPosition(new Vector2f(0f, 300f + 2 * 40f));
        controlsButton.setVisible(true);
        controlsButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config:controls");
            }
        });

        backToMainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        backToMainMenuButton.getLabel().setText("Return to Main Menu");
        backToMainMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backToMainMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        backToMainMenuButton.setVisible(true);
        backToMainMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(Config.class).save();
                getGUIManager().openWindow("main");
            }
        });

        addDisplayElement(title);
        addDisplayElement(version);

        addDisplayElement(videoButton);
        addDisplayElement(audioButton);
        addDisplayElement(controlsButton);
        addDisplayElement(backToMainMenuButton);
    }
}
