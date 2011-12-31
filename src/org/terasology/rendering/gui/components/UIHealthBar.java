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
package org.terasology.rendering.gui.components;

import org.terasology.game.Terasology;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Small health bar that visualizes the current amount of health points of the player
 * with ten small heart icons.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIHealthBar extends UIDisplayContainer {
    private final UIGraphicsElement[] _hearts;

    public UIHealthBar() {
        setSize(new Vector2f(180f, 18f));

        _hearts = new UIGraphicsElement[10];

        // Create hearts
        for (int i = 0; i < 10; i++) {
            _hearts[i] = new UIGraphicsElement("icons");
            _hearts[i].setVisible(true);
            _hearts[i].getTextureSize().set(new Vector2f(9f / 256f, 9f / 256f));
            _hearts[i].getTextureOrigin().set(new Vector2f(52f / 256f, 0.0f));
            _hearts[i].setSize(new Vector2f(18f, 18f));
            _hearts[i].setPosition(new Vector2f(18f * i, 18f));

            addDisplayElement(_hearts[i]);
        }
    }

    @Override
    public void update() {
        super.update();

        // Show/Hide hearts relatively to the available health points of the player
        for (int i = 0; i < 10; i++) {
            if (i < Terasology.getInstance().getActiveWorldRenderer().getPlayer().getHealthPercentage() * 10f)
                _hearts[i].setVisible(true);
            else
                _hearts[i].setVisible(false);
        }
    }
}
