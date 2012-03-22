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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 * A simple graphical button usable for creating user interface.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIButton extends UIDisplayContainer {

    private final ArrayList<IClickListener> _clickListeners = new ArrayList<IClickListener>();

    private final UIText _label;

    public UIButton(Vector2f size) {
        setSize(size);
        setStyle("background-image","gui_menu 256/512 30/512 0 90/512");

        _label = new UIText("Untitled");
        _label.setVisible(true);
        addDisplayElement(_label);
    }

    @Override
    public void update() {
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if (intersects(mousePos)) {

            if (!_clickSoundPlayed) {
                AudioManager.play("Click", 1.0f);
                _clickSoundPlayed = true;
            }

            if (_mouseUp) {
                _mouseUp = false;
                clicked();
            }

            if (_mouseDown) {
                setStyle("background-position","0 60/512");
            } else {
                setStyle("background-position","0 30/512");
            }

        } else {
            _clickSoundPlayed = false;
            _mouseUp = false;
            _mouseDown = false;
            setStyle("background-position","0 0");
        }

        // Position the label in the center of the button
        _label.setPosition(new Vector2f(getSize().x / 2 - getLabel().getTextWidth() / 2, getSize().y / 2 - getLabel().getTextHeight() / 2));
    }

    public void clicked() {
        for (int i = 0; i < _clickListeners.size(); i++) {
            _clickListeners.get(i).clicked(this);
        }
    }

    public UIText getLabel() {
        return _label;
    }

    public void addClickListener(IClickListener listener) {
        _clickListeners.add(listener);
    }

    public void removeClickListener(IClickListener listener) {
        _clickListeners.remove(listener);
    }
}
