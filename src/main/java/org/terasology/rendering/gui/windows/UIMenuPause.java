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

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.event.RespawnRequestEvent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * Simple pause menu providing buttons for respawning the player and creating a new world.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIMenuPause extends UIWindow {

    final UIImage title;

    final UIButton exitButton;
    final UIButton mainMenuButton;
    final UIButton respawnButton;
    final UIButton backToGameButton;

    final UILabel version;

    public UIMenuPause() {
        setId("pause");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        setCloseBinds(new String[]{"engine:pause"});
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});
        maximize();

        title = new UIImage(Assets.getTexture("engine:terasology"));
        title.setSize(new Vector2f(512f, 128f));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 128f));
        title.setVisible(true);

        version = new UILabel("Pre Alpha");
        version.setHorizontalAlign(EHorizontalAlign.CENTER);
        version.setPosition(new Vector2f(0f, 230f));
        version.setVisible(true);

        exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        exitButton.getLabel().setText("Exit Terasology");
        exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });
        exitButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        exitButton.setPosition(new Vector2f(0f, 300f + 3 * 32f + 24f + 8f));
        exitButton.setVisible(true);

        respawnButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        respawnButton.getLabel().setText("Respawn");
        respawnButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(LocalPlayer.class).getClientEntity().send(new RespawnRequestEvent());
                setVisible(false);
            }
        });
        respawnButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        respawnButton.setPosition(new Vector2f(0f, 300f + 32f + 24f));
        respawnButton.setVisible(false);

        mainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        mainMenuButton.getLabel().setText("Return to Main Menu");
        mainMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu());
            }
        });
        mainMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        mainMenuButton.setPosition(new Vector2f(0f, 300f + 2 * 32f + 24f + 4f));
        mainMenuButton.setVisible(true);

        backToGameButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        backToGameButton.getLabel().setText("Back to game");
        backToGameButton.addClickListener(new ClickListener() {
            public void click(UIDisplayElement element, int button) {
                setVisible(false);
            }
        });
        backToGameButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backToGameButton.setPosition(new Vector2f(0f, 300f));
        backToGameButton.setVisible(true);


        addDisplayElement(title);
        addDisplayElement(version);
        addDisplayElement(exitButton);
        addDisplayElement(respawnButton);
        addDisplayElement(mainMenuButton);
        addDisplayElement(backToGameButton);
    }

    @Override
    public void update() {
        super.update();
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        respawnButton.setVisible(!localPlayer.getCharacterEntity().exists());
    }
}
