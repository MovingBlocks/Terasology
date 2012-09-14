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
import org.terasology.input.binds.PauseButton;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.logic.LocalPlayer;
import org.terasology.asset.AssetManager;
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

    final UIImage _title;

    final UIButton _exitButton;
    final UIButton _mainMenuButton;
    final UIButton _respawnButton;
    final UIButton _backToGameButton;

    final UILabel _version;

    public UIMenuPause() {
        setId("pause");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        setCloseBinds(new String[] {PauseButton.ID});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
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
        _exitButton.setPosition(new Vector2f(0f, 300f + 3 * 32f + 24f + 8f));
        _exitButton.setVisible(true);
        
        _respawnButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _respawnButton.getLabel().setText("Respawn");
        _respawnButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(LocalPlayer.class).getEntity().send(new RespawnEvent());
                
                setVisible(false);
            }
        });
        _respawnButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _respawnButton.setPosition(new Vector2f(0f, 300f + 32f + 24f));
        _respawnButton.setVisible(true);

        _mainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
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

        _backToGameButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _backToGameButton.getLabel().setText("Back to game");
        _backToGameButton.addClickListener(new ClickListener() {
            public void click(UIDisplayElement element, int button) {
                setVisible(false);
            }
        });
        _backToGameButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _backToGameButton.setPosition(new Vector2f(0f, 300f));
        _backToGameButton.setVisible(true);


        addDisplayElement(_title);
        addDisplayElement(_version);
        addDisplayElement(_exitButton);
        addDisplayElement(_respawnButton);
        addDisplayElement(_mainMenuButton);
        addDisplayElement(_backToGameButton);
    }
}
