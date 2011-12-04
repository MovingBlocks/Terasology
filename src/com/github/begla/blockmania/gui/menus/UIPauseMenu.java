/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.gui.menus;

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.gui.components.UIButton;
import com.github.begla.blockmania.gui.components.UIText;
import com.github.begla.blockmania.gui.components.UITransparentOverlay;
import com.github.begla.blockmania.gui.framework.UIClickListener;
import com.github.begla.blockmania.gui.framework.UIDisplayElement;
import com.github.begla.blockmania.gui.framework.UIDisplayRenderer;
import com.github.begla.blockmania.gui.framework.UIGraphicsElement;
import org.lwjgl.opengl.Display;

import javax.vecmath.Vector2f;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIPauseMenu extends UIDisplayRenderer {

    UITransparentOverlay _overlay;
    UIGraphicsElement _title;

    UIButton _exitButton, _newWorldButton, _respawnButton;

    UIText _version;

    public UIPauseMenu() {
        _title = new UIGraphicsElement("blockmania");
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText((String) ConfigurationManager.getInstance().getConfig().get("System.gameTitle"));
        _version.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f));
        _exitButton.getLabel().setText("Exit Blockmania");
        _exitButton.setVisible(true);

        _exitButton.addClickListener(new UIClickListener() {
            public void clicked(UIDisplayElement element) {
                Blockmania.getInstance().exit();
            }
        });

        _newWorldButton = new UIButton(new Vector2f(256f, 32f));
        _newWorldButton.getLabel().setText("Create New World");
        _newWorldButton.setVisible(true);

        _newWorldButton.addClickListener(new UIClickListener() {
            public void clicked(UIDisplayElement element) {
                Blockmania.getInstance().initWorld();
                setVisible(false);
            }
        });

        _respawnButton = new UIButton(new Vector2f(256f, 32f));
        _respawnButton.getLabel().setText("Respawn");
        _respawnButton.setVisible(true);

        _respawnButton.addClickListener(new UIClickListener() {
            public void clicked(UIDisplayElement element) {
                Blockmania.getInstance().getActiveWorld().getPlayer().respawn();
                setVisible(false);
            }
        });

        _overlay = new UITransparentOverlay();
        _overlay.setVisible(true);

        addDisplayElement(_overlay);

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_exitButton);
        addDisplayElement(_newWorldButton);
        addDisplayElement(_respawnButton);
    }

    @Override
    public void update() {
        super.update();

        _version.setPosition(new Vector2f(Display.getWidth() / 2 - _version.getTextWidth() / 2, 230f));

        _respawnButton.setPosition(new Vector2f(Display.getWidth() / 2 - 128f, 300f));
        _newWorldButton.setPosition(new Vector2f(Display.getWidth() / 2 - 128f, 300f + 32f + 8f));

        _exitButton.setPosition(new Vector2f(Display.getWidth() / 2 - 128f, 300f + 2 * 32f + 32f));

        _title.setPosition(new Vector2f(Display.getWidth() / 2 - 256f, 128f));
    }
}
