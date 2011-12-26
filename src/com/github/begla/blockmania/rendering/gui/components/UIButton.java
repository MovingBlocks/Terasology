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
package com.github.begla.blockmania.rendering.gui.components;

import com.github.begla.blockmania.logic.manager.AudioManager;
import com.github.begla.blockmania.rendering.gui.framework.UIClickListener;
import com.github.begla.blockmania.rendering.gui.framework.UIDisplayContainer;
import com.github.begla.blockmania.rendering.gui.framework.UIGraphicsElement;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

/**
 * A simple graphical button usable for creating user interface.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIButton extends UIDisplayContainer {

    private final ArrayList<UIClickListener> _clickListeners = new ArrayList<UIClickListener>();

    private final UIGraphicsElement _defaultTexture;
    private final UIText _label;

    public UIButton(Vector2f size) {
        setSize(size);

        _defaultTexture = new UIGraphicsElement("gui_menu");
        _defaultTexture.setVisible(true);
        _defaultTexture.getTextureSize().set(new Vector2f(256f / 512f, 30f / 512f));
        addDisplayElement(_defaultTexture);

        _label = new UIText("Untitled");
        _label.setVisible(true);
        addDisplayElement(_label);
    }

    @Override
    public void update() {
        _defaultTexture.setSize(getSize());

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if (intersects(mousePos)) {

            if (!_clickSoundPlayed) {
                AudioManager.getInstance().getAudio("PlaceBlock").playAsSoundEffect(1.0f, 0.5f, false);
                _clickSoundPlayed = true;
            }

            if (_mouseUp) {
                _mouseUp = false;
                clicked();
            }

            if (_mouseDown) {
                _defaultTexture.getTextureOrigin().set(0f, 60f / 512f);
            } else {
                _defaultTexture.getTextureOrigin().set(0f, 30f / 512f);
            }

        } else {
            _clickSoundPlayed = false;
            _mouseUp = false;
            _mouseDown = false;

            _defaultTexture.getTextureOrigin().set(0f, 0f);
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

    public void addClickListener(UIClickListener listener) {
        _clickListeners.add(listener);
    }

    public void removeClickListener(UIClickListener listener) {
        _clickListeners.remove(listener);
    }
}
