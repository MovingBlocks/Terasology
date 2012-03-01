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
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Main menu screen.
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UIMainMenu extends UIDisplayRenderer {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    final UIButton _exitButton;
    final UIButton _startButton;

    final UIText _version;

    public UIMainMenu() {
        _title = new UIGraphicsElement("terasology");
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Pre Alpha");
        _version.setVisible(true);

        _exitButton = new UIButton(new Vector2f(256f, 32f));
        _exitButton.getLabel().setText("Exit Terasology");
        _exitButton.setVisible(true);

        _exitButton.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                Terasology.getInstance().exit();
            }
        });

        _startButton = new UIButton(new Vector2f(256f, 32f));
        _startButton.getLabel().setText("Play Terasology");
        _startButton.setVisible(true);

        _startButton.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                Terasology.getInstance().setGameState(Terasology.GAME_STATE.SINGLE_PLAYER);
            }
        });

        _overlay = new UIImageOverlay("menuBackground");
        _overlay.setVisible(true);

        addDisplayElement(_overlay);

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_exitButton);
        addDisplayElement(_startButton);

        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _startButton.centerHorizontally();
        _startButton.getPosition().y = 300f + 32f + 8f;

        _exitButton.centerHorizontally();
        _exitButton.getPosition().y = 300f + 2 * 32f + 32f;


        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
