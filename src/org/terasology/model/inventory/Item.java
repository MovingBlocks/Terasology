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
package org.terasology.model.inventory;

import java.util.HashMap;

import org.terasology.logic.characters.Player;
import org.terasology.model.blocks.Block;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Item {
    protected int _amount;
    protected byte _toolId;

    protected int _stackSize = 16;

    protected HashMap<Block, Byte> _extractionAmountMapping = new HashMap<Block, Byte>();

    public Item() {
        _amount = 1;
    }

    public boolean renderFirstPersonView(Player player) {
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
