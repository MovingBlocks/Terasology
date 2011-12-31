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

import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * The player's inventory.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIInventory extends UIDisplayContainer {

    private final UIGraphicsElement _background;
    private final UIToolbarCell[] _cells;

    public UIInventory() {
        setSize(new Vector2f(176.0f * 2.5f, 167.0f * 2.5f));

        _background = new UIGraphicsElement("inventory");
        _background.setSize(getSize());
        _background.getTextureSize().set(new Vector2f(176.0f / 256.0f, 167.0f / 256.0f));
        _background.setVisible(true);

        addDisplayElement(_background);

        _cells = new UIToolbarCell[27];

        for (int i = 0; i < _cells.length; i++) {
            UIInventoryCell cell = new UIInventoryCell(i);
            cell.setVisible(true);
            addDisplayElement(cell);
        }
    }
}
