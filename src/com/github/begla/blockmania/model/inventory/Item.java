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
package com.github.begla.blockmania.model.inventory;

import com.github.begla.blockmania.logic.characters.Player;
import com.github.begla.blockmania.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Item {

    protected int _amount;
    protected byte _toolId;
    protected Player _parent;
    protected int _stackSize = 32;

    protected UIGraphicsElement _icon;

    public Item(Player parent) {
        _amount = 1;
        _parent = parent;

        _icon = new UIGraphicsElement("items");
        _icon.setSize(new Vector2f(20, 20));
        _icon.getTextureSize().set(new Vector2f(0.0624f, 0.0624f));
        _icon.setVisible(true);
        _icon.setPosition(new Vector2f(-8f, -10f));
    }

    public boolean renderIcon() {
        _icon.renderTransformed();
        return true;
    }

    public boolean renderFirstPersonView() {
        return false;
    }

    public void setAmount(int amount) {
        _amount = amount;
    }

    public int getAmount() {
        return _amount;
    }

    public void increaseAmount() {
        increaseAmount(1);
    }

    public void decreaseAmount() {
        decreaseAmount(1);
    }

    public void increaseAmount(int i) {
        if (i <= 0)
            return;

        _amount += i;
    }

    public void decreaseAmount(int i) {
        if (i <= 0)
            return;

        _amount -= i;
    }

    public byte getToolId() {
        return _toolId;
    }

    public Player getParent() {
        return _parent;
    }

    public void setIconWithAtlasPos(int x, int y) {
        _icon.getTextureOrigin().set(new Vector2f(x * 0.0625f, y * 0.0625f));
    }

    public int getStackSize() {
        return _stackSize;
    }
}
