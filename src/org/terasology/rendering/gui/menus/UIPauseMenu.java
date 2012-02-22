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
import org.terasology.logic.manager.SettingsManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.components.UITransparentOverlay;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Simple pause menu providing buttons for respawning the player and creating a new world.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIPauseMenu extends UIDisplayRenderer {

    final UITransparentOverlay _overlay;
    final UIGraphicsElement _title;

    final UIButton _exitButton;
    //final UIButton _newWorldButton;
    final UIButton _respawnButton;

    final UIText _version;

    public UIPauseMenu() {
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


        _respawnButton = new UIButton(new Vector2f(256f, 32f));
        _respawnButton.getLabel().setText("Respawn");
        _respawnButton.setVisible(true);

        _respawnButton.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                setVisible(false);
                Terasology.getInstance().getActiveWorldRenderer().getPlayer().respawn();
            }
        });

        _overlay = new UITransparentOverlay();
        _overlay.setVisible(true);

        addDisplayElement(_overlay);

        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_exitButton);
        //addDisplayElement(_newWorldButton);
        addDisplayElement(_respawnButton);

        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _respawnButton.centerHorizontally();
        _respawnButton.getPosition().y = 300f;
//        _newWorldButton.centerHorizontally();
    //    _newWorldButton.getPosition().y = 300f + 32f + 8f;

        _exitButton.centerHorizontally();
        _exitButton.getPosition().y = 300f + 2 * 32f + 32f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
