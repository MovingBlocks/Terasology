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

import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * 
 * TODO get rid of this. put it directly into the UIDialogBox.
 */
public class UIWindowTitle extends UIDisplayContainer {
    private UIGraphicsElement _leftBackground;
    private UIGraphicsElement _centerBackground;
    private UIGraphicsElement _rightBackground;
    private UIText _text;

    public UIWindowTitle(Vector2f size, String title) {
        setSize(size);

        _text = new UIText(getPosition());
        setTitle(title);
        _text.setColor(Color.orange);
        _text.setVisible(true);

        _leftBackground = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
        _leftBackground.setSize(new Vector2f(7f, 19f));
        _leftBackground.getTextureSize().set(new Vector2f(7f / 512f, 19f / 512f));
        _leftBackground.getTextureOrigin().set(new Vector2f(111f / 512f, 155f / 512f));
        _leftBackground.setVisible(true);

        _centerBackground = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
        _centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
        _centerBackground.getTextureSize().set(new Vector2f(51f / 512f, 19f / 512f));
        _centerBackground.getTextureOrigin().set(new Vector2f(118f / 512f, 155f / 512f));
        _centerBackground.getPosition().x += _leftBackground.getSize().x;
        _centerBackground.setVisible(true);

        _rightBackground = new UIGraphicsElement(AssetManager.loadTexture("engine:gui_menu"));
        _rightBackground.setSize(new Vector2f(8f, 19f));
        _rightBackground.getTextureSize().set(new Vector2f(8f / 512f, 19f / 512f));
        _rightBackground.getTextureOrigin().set(new Vector2f(189f / 512f, 155f / 512f));
        _rightBackground.setVisible(true);
        _rightBackground.getPosition().x = _centerBackground.getPosition().x + _centerBackground.getSize().x;
        addDisplayElement(_leftBackground);
        addDisplayElement(_centerBackground);
        addDisplayElement(_rightBackground);
        addDisplayElement(_text);
    }


    public void setTitle(String title) {
        _text.setText(title);
        _text.getPosition().x = getSize().x / 2 - _text.getTextWidth() / 2;
    }

    public void resize() {
        _centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
        _centerBackground.getPosition().x = _leftBackground.getPosition().x + _leftBackground.getSize().x;
        _rightBackground.getPosition().x = _centerBackground.getPosition().x + _centerBackground.getSize().x;
        _text.getPosition().x = getSize().x / 2 - _text.getTextWidth() / 2;
    }

}
