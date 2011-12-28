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
import com.github.begla.blockmania.model.blocks.Block;
import com.github.begla.blockmania.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.HashMap;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Item {

    protected Player _parent;
    protected UIGraphicsElement _icon;

    protected int _amount;
    protected byte _toolId;

    protected int _stackSize = 16;

    protected int _iconX, _iconY;

    protected HashMap<Block, Byte> _extractionAmountMapping = new HashMap<Block, Byte>();

    public Item(Player parent) {
        _amount = 1;
        _parent = parent;

        _icon = new UIGraphicsElement("items");
        _icon.setSize(new Vector2f(32, 32));
        _icon.getTextureSize().set(new Vector2f(0.0624f, 0.0624f));
        _icon.setVisible(true);
        _icon.setPosition(new Vector2f(-10f, -16f));

        setIconWithAtlasPos(0, 0);
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
        _iconX = x;
        _iconY = y;
        _icon.getTextureOrigin().set(new Vector2f(x * 0.0625f, y * 0.0625f));
    }

    public int getStackSize() {
        return _stackSize;
    }

    public void setExtractionAmountForBlock(Block block, byte amount) {
        _extractionAmountMapping.put(block, amount);
    }

    public byte getExtractionAmountForBlock(Block block) {
        if (_extractionAmountMapping.containsKey(block))
            return _extractionAmountMapping.get(block);

        return 1;
    }
}
