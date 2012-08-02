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
package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

public class UIListItem extends UIDisplayContainer {
    private Object _value;
    private String _text;
    private boolean _isSelected;
    private Vector2f _padding = new Vector2f(5f, 15f);

    //UI
    private final UIText _label;

    public UIListItem(Vector2f size, String text, Object value) {
        setSize(size);
        _text = text;
        _value = value;

        _label = new UIText();
        _label.setVisible(true);
        _label.setColor(Color.lightGray);
        _label.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));
        _label.setText(_text);

        if (getSize().x < _label.getTextWidth()) {
            setSize(new Vector2f(_label.getTextWidth(), getSize().y));
        }

        addDisplayElement(_label);

    }

    public void update(){
        if(_isSelected){
            return;
        }
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if (intersects(mousePos)) {
            _label.setColor(Color.orange);
        }else{
            _label.setColor(Color.lightGray);
        }
    }

    public Object getValue() {
        return _value;
    }

    public void setValue(Object value) {
        _value = value;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _label.setText(_text);
        _text = text;
    }

    public boolean isSelected() {
        return _isSelected;
    }

    public void setSelected(boolean selected) {
        _isSelected = selected;

        if (_isSelected) {
            setStyle("background-color", "#e1ddd4 1");
            _label.setColor(Color.orange);
        } else {
            setStyle("background", "none");
            _label.setColor(Color.lightGray);
        }
    }
}
