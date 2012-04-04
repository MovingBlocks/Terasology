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
package org.terasology.rendering.gui.menus;

import org.terasology.game.Terasology;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.*;

import javax.vecmath.Vector2f;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMainMenu extends UIDisplayWindow {

    private final UIImageOverlay _overlay;
    private final UIGraphicsElement _title;


    private final UIButton _exitButton;
    private final UIButton _singlePlayer;
    private final UIButton _configButton;

    final UIText _version;
                                                                  
    public UIMainMenu() {
        maximaze();
        _title = new UIGraphicsElement("terasology");
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Pre Alpha");
        _version.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f));
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.setVisible(true);


        _configButton = new UIButton(new Vector2f(256f, 32f));
        _configButton.getLabel().setText("Settings");
        _configButton.setVisible(true);

        _singlePlayer = new UIButton(new Vector2f(256f, 32f));
        _singlePlayer.getLabel().setText("Single player");
        _singlePlayer.setVisible(true);

        _overlay = new UIImageOverlay("menuBackground");
       // _overlay.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);
        addDisplayElement(_configButton);
        addDisplayElement(_exitButton);
        addDisplayElement(_singlePlayer);

        update();
    }

    @Override
    public void update() {
        //System.out.println("update");
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _singlePlayer.centerHorizontally();
        _singlePlayer.getPosition().y = 300f + 40f;

        _exitButton.centerHorizontally();
        _exitButton.getPosition().y = 300f + 3 * 32f + 64f;
        _configButton.centerHorizontally();
        _configButton.getPosition().y = 300f + 2 * 40f;

        _exitButton.centerHorizontally();
        _exitButton.getPosition().y = 300f + 4 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }

    public UIButton getExitButton() {
        return _exitButton;
    }

    public UIButton getSinglePlayerButton() {
        return _singlePlayer;
    }

    public UIButton getConfigButton() {
        return _configButton;
    }
}
