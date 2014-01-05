/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.version.TerasologyVersion;

import javax.vecmath.Vector2f;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMenuMain extends UIWindow {

    private final UIImage title;

    private final UIButton exitButton;
    private final UIButton singlePlayerButton;
    private final UIButton configButton;
    private final UIButton joinButton;
    private final UIButton multiplayerButton;

    private final UILabel version;

    public UIMenuMain() {
        setId("main");
        setBackgroundImage("engine:menubackground");
        setModal(true);
        maximize();

        title = new UIImage(Assets.getTexture("engine:terasology"));
        title.setSize(new Vector2f(512f, 128f));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 128f));
        title.setVisible(true);

        version = new UILabel(TerasologyVersion.getInstance().getHumanVersion());
        version.setHorizontalAlign(EHorizontalAlign.CENTER);
        version.setPosition(new Vector2f(0f, 230f));
        version.setVisible(true);
        version.setTextShadow(true);

        exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        exitButton.getLabel().setText("Exit Terasology");
        exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });
        exitButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        exitButton.setPosition(new Vector2f(0f, 300f + 6 * 40f));
        exitButton.setVisible(true);

        configButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        configButton.getLabel().setText("Settings");
        configButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config");
            }
        });
        configButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        configButton.setPosition(new Vector2f(0f, 300f + 4 * 40f));
        configButton.setVisible(true);

        singlePlayerButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        singlePlayerButton.getLabel().setText("Single player");
        singlePlayerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("selectworld");
                ((UIMenuSelectWorld) getGUIManager().getWindowById("selectworld")).setCreateServerGame(false);
            }
        });

        singlePlayerButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        singlePlayerButton.setPosition(new Vector2f(0f, 300f + 40f));
        singlePlayerButton.setVisible(true);

        multiplayerButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        multiplayerButton.getLabel().setText("Host Game");
        multiplayerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("selectworld");
                ((UIMenuSelectWorld) getGUIManager().getWindowById("selectworld")).setCreateServerGame(true);
            }
        });

        multiplayerButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        multiplayerButton.setPosition(new Vector2f(0f, 300f + 2 * 40f));
        multiplayerButton.setVisible(true);

        joinButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        joinButton.getLabel().setText("Join Game");
        joinButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        joinButton.setPosition(new Vector2f(0f, 300f + 3 * 40f));
        joinButton.setVisible(true);
        joinButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {

                getGUIManager().openWindow("joinserver");
            }
        });


        addDisplayElement(title);
        addDisplayElement(version);
        addDisplayElement(configButton);
        addDisplayElement(exitButton);
        addDisplayElement(singlePlayerButton);
        addDisplayElement(multiplayerButton);
        addDisplayElement(joinButton);
    }
}
