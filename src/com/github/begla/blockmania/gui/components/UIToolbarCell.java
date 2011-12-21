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

import com.github.begla.blockmania.gui.framework.UIDisplayContainer;
import com.github.begla.blockmania.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * A single cell of the toolbar with a small text label and a selection
 * rectangle.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIToolbarCell extends UIDisplayContainer {

    private final UIGraphicsElement _selectionRectangle;
    private final UIText _hotKeyLabel;

    private final int _id;
    private boolean _selected = false;

    public UIToolbarCell(int id) {
        _id = id;

        setSize(new Vector2f(48f, 48f));
        setPosition(new Vector2f((getSize().x - 8f) * _id - 2f, 2f));

        _selectionRectangle = new UIGraphicsElement("gui");
        _selectionRectangle.getTextureSize().set(new Vector2f(24f / 256f, 24f / 256f));
        _selectionRectangle.getTextureOrigin().set(new Vector2f(0.0f, 24f / 256f));
        _selectionRectangle.setSize(new Vector2f(48f, 48f));

        _hotKeyLabel = new UIText(Integer.toString(_id + 1));
        _hotKeyLabel.setVisible(true);
        _hotKeyLabel.setPosition(new Vector2f(30f, 20f));

        addDisplayElement(_selectionRectangle);
        addDisplayElement(_hotKeyLabel);
    }

    @Override
    public void update() {
        super.update();

        _selectionRectangle.setVisible(_selected);
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    public boolean getSelected() {
        return _selected;
    }
}
