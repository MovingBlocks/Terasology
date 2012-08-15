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

import org.lwjgl.input.Keyboard;
import org.terasology.events.RespawnEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.components.UITransparentOverlay;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.WindowListener;

/**
 * Simple status screen with one sole text label usable for status notifications.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIScreenDeath extends UIDisplayWindow {

    private final UITransparentOverlay _overlay;
    private final UIText _meassage;
    private final UIButton _respawnButton;
    private final UIButton _exitButton;
    private final UIButton _mainMenuButton;

    public UIScreenDeath() {
        setModal(true);
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        maximize();
        
        addWindowListener(new WindowListener() {
            @Override
            public void open(UIDisplayElement element) {

            }
            
            @Override
            public void close(UIDisplayElement element) {
                respawn();
            }
        });
        
        _meassage = new UIText("You are dead");
        _meassage.setVisible(true);

        _overlay = new UITransparentOverlay(200f, 0f, 0f, 0.25f);
        _overlay.setVisible(true);
        
        _respawnButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _respawnButton.setVisible(true);
        _respawnButton.getLabel().setText("Respawn");
        _respawnButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                respawn();
                close(true);
            }
        });
        
        _mainMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _mainMenuButton.getLabel().setText("Return to Main Menu");
        _mainMenuButton.setVisible(true);
        _mainMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu());
            }
        });


        _exitButton = new UIButton(new Vector2f(256f, 32f), UIButton.eButtonType.NORMAL);
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.setVisible(true);
        _exitButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(GameEngine.class).shutdown();
            }
        });

        addDisplayElement(_overlay);
        addDisplayElement(_meassage);
        addDisplayElement(_exitButton);
        addDisplayElement(_respawnButton);
        addDisplayElement(_mainMenuButton);

        layout();
    }
    
    private void respawn() {
        CoreRegistry.get(LocalPlayer.class).getEntity().send(new RespawnEvent());
    }

    @Override
    public void layout() {
        super.layout();

        if (_meassage != null) {
        	_meassage.center();
        	_meassage.getPosition().y -= 100;
        	
            _respawnButton.centerHorizontally();
            _respawnButton.getPosition().y = 300f + 32f + 24f;
    
            _mainMenuButton.centerHorizontally();
            _mainMenuButton.getPosition().y = 300f + 2 * 32f + 24f + 4f;
    
            _exitButton.centerHorizontally();
            _exitButton.getPosition().y = 300f + 3 * 32f + 24f + 8f;
        }
    }

    public void updateStatus(String string) {
        _meassage.setText(string);
        layout();
    }
}
