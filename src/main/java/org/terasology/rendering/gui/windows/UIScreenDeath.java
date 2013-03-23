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

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.events.RespawnEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.VisibilityListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIScreenDeath extends UIWindow {

    private final UILabel _meassage;
    private final UIButton _respawnButton;
    private final UIButton _exitButton;
    private final UIButton _mainMenuButton;

    public UIScreenDeath() {
        setId("death");
        setBackgroundColor(new Color(70, 0, 0, 200));
        setModal(true);
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});
        maximize();

        addVisibilityListener(new VisibilityListener() {

            @Override
            public void changed(UIDisplayElement element, boolean visibility) {
                if (!visibility) {
                    respawn();
                }
            }
        });

        _meassage = new UILabel("You are dead");
        _meassage.setHorizontalAlign(EHorizontalAlign.CENTER);
        _meassage.setPosition(new Vector2f(0f, 300f));
        _meassage.setVisible(true);

        _respawnButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        _respawnButton.getLabel().setText("Respawn");
        _respawnButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                respawn();
                setVisible(false);
            }
        });
        _respawnButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _respawnButton.setPosition(new Vector2f(0f, 300f + 32f + 24f));
        _respawnButton.setVisible(true);

        _mainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        _mainMenuButton.getLabel().setText("Return to Main Menu");
        _mainMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu());
            }
        });
        _mainMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _mainMenuButton.setPosition(new Vector2f(0f, 300f + 2 * 32f + 24f + 4f));
        _mainMenuButton.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });
        _exitButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _exitButton.setPosition(new Vector2f(0f, 300f + 3 * 32f + 24f + 8f));
        _exitButton.setVisible(true);

        addDisplayElement(_meassage);
        addDisplayElement(_exitButton);
        addDisplayElement(_respawnButton);
        addDisplayElement(_mainMenuButton);
    }

    private void respawn() {
        CoreRegistry.get(LocalPlayer.class).getCharacterEntity().send(new RespawnEvent());
    }
}
