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
package com.github.begla.blockmania.gui.components;

import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.gui.framework.UIDisplayContainer;
import com.github.begla.blockmania.gui.framework.UIGraphicsElement;
import org.lwjgl.opengl.Display;

import javax.vecmath.Vector2f;

/**
 * A small toolbar placed on the bottom of the screen.
 *
 * TODO: Currently visualizes the selected tool in the tool belt. Will be used for showing item icons later on.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIToolbar extends UIDisplayContainer {
    private final UIGraphicsElement _backgroundTexture;
    private final UIToolbarCell[] _cells;

    public UIToolbar() {
        setSize(new Vector2f(364f, 44f));

        _backgroundTexture = new UIGraphicsElement("gui");
        _backgroundTexture.setVisible(true);
        _backgroundTexture.getTextureSize().set(new Vector2f(182f / 256f, 22f / 256f));
        _backgroundTexture.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        _backgroundTexture.setSize(getSize());

        addDisplayElement(_backgroundTexture);

        _cells = new UIToolbarCell[9];

        // Create the toolbar cells
        for (int i=0; i < 9; i++) {
            _cells[i] = new UIToolbarCell(i);
            _cells[i].setVisible(true);
            addDisplayElement(_cells[i]);
        }
    }

    @Override
    public void update() {
        super.update();

        // Select the cell based on the selected tool of the tool belt
        for (int i=0; i<9; i++) {
            if (Blockmania.getInstance().getActiveWorldRenderer().getPlayer().getSelectedTool() == i+1) {
                _cells[i].setSelected(true);
            }
            else {
                _cells[i].setSelected(false);
            }
        }

        setPosition(new Vector2f(calcCenterPosition().x, Display.getHeight() - getSize().y));
    }
}
