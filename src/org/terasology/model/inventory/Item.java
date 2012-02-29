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
import java.util.Map;

import org.terasology.logic.characters.Player;
import org.terasology.model.blocks.Block;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Item {
    protected byte _toolId;
    protected int _stackSize;
    protected Map<Block, Integer> _extraction;

    public Item() {
        _stackSize = 99;
        _extraction = new HashMap<Block, Integer>();
    }

    /**
     * 
     * @param player the Player from whose perspective rendering will happen
     */
    public void renderFirstPersonView(Player player) {
        // NO-OP
    }

    public byte getToolId() {
        return _toolId;
    }

    public int getStackSize() {
        return _stackSize;
    }

    /**
     * 
     * @param block the block to set extraction for
     * @param amount the amount of extraction
     */
    public void setExtraction(Block block, int amount) {
        _extraction.put(block, amount);
    }

    public int getExtraction(Block block) {
    	return _extraction.containsKey(block) ? _extraction.get(block) : 1;
    }
}
