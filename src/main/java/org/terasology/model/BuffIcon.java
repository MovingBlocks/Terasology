/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.model;

import org.terasology.asset.Assets;
import org.terasology.rendering.gui.widgets.UIImage;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * @author bi0hax
 * Icons for showing status effects in the buff bar.
 */
public class BuffIcon {
    private static Map<String, BuffIcon> bufficons;
    private UIImage _element;
    private int _x;
    private int _y;

    public BuffIcon() {
        _element = new UIImage(Assets.getTexture("engine:buffs"));
        _element.setSize(new Vector2f(32, 32));
        _element.setTextureSize(new Vector2f(16, 16));
        _element.setVisible(true);
        _element.setPosition(new Vector2f(-10f, -16f));

        setAtlasPosition(0, 0);
    }

    public static BuffIcon get(String name) {
        if (bufficons == null) {
            loadIcons();
        }

        return bufficons.get(name.toLowerCase(Locale.ENGLISH));
    }

    private static void loadIcons() {
        bufficons = new HashMap<String, BuffIcon>();
        //* BUFFS & DE·BUFFS *//
        BuffIcon poisoned = new BuffIcon();
        BuffIcon cured = new BuffIcon();
        BuffIcon speed = new BuffIcon();
        BuffIcon maxspeed = new BuffIcon();

        //* ATLAS POSITIONS *//
        //DeBuffs
        poisoned.setAtlasPosition(0, 0);
        //Buffs
        cured.setAtlasPosition(1, 0);
        speed.setAtlasPosition(0, 1);
        maxspeed.setAtlasPosition(1, 1);

        bufficons.put("poisoned", poisoned);
        bufficons.put("cured", cured);
        bufficons.put("speed", speed);
        bufficons.put("maxspeed", maxspeed);

    }

    //@return x-offset in icon sheet
    public int getX() {
        return _x;
    }

    //@return y-offset in icon sheet
    public int getY() {
        return _y;
    }

    private void setAtlasPosition(int x, int y) {
        _x = x;
        _y = y;

        if (_element == null) {
            return;
        }

        _element.setTextureOrigin(new Vector2f(x * 16f, y * 16f));
    }
}
