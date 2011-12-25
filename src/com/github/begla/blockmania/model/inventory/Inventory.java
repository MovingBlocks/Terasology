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
import com.github.begla.blockmania.model.blocks.BlockManager;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class Inventory {

    private final Item[] _inventory = new Item[64];
    private final Player _parent;

    public Inventory(Player parent) {
        _parent = parent;
        initDefaultItems();
    }

    private void initDefaultItems() {
        _inventory[0] = new BlockItem(_parent, BlockManager.getInstance().getBlock("Companion").getId(), 1);
        _inventory[1] = new BlockItem(_parent, BlockManager.getInstance().getBlock("Torch").getId(), 16);

        _inventory[7] = new BlueprintItem(_parent);
        _inventory[8] = new BlueprintItem(_parent);
    }

    /**
     * Store an item at a given slot position. Returns true if the item could be stored.
     *
     * @param slot The slot
     * @param item The item to store
     * @return True if item could be stored
     */
    public boolean storeItemInSlot(int slot, Item item) {
        // The slot is empty so no problem here
        if (_inventory[slot] == null) {
            _inventory[slot] = item;
            return true;
        } else {
            if (_inventory[slot].equals(item) && _inventory[slot].getAmount() < item.getStackSize()) {
                _inventory[slot].increaseAmount();
                return true;
            }
        }

        return false;
    }

    /**
     * Stores the item in the first available slot position.
     *
     * @param item The item to store
     * @return True if the item could be stored
     */
    public boolean storeItemInFreeSlot(Item item) {
        return storeItemInSlot(findFirstFreeSlot(item), item);
    }

    /**
     * Removes one item at the given slot position.
     *
     * @param slot The slot
     * @return The removed object. Null if nothing could be removed.
     */
    public Item removeOneItemInSlot(int slot) {
        if (_inventory[slot] != null) {
            Item item = _inventory[slot];
            item.decreaseAmount();

            if (item.getAmount() == 0) {
                _inventory[slot] = null;
            }

            return item;
        }

        return null;
    }

    /**
     * Removes one item at the given slot position.
     *
     * @param slot The slot
     * @return The removed object. Null if nothing could be removed.
     */
    public Item removeAllItemsInSlot(int slot) {
        if (_inventory[slot] != null) {
            Item item = _inventory[slot];
            _inventory[slot] = null;

            return item;
        }

        return null;
    }

    /**
     * Returns the first free slot for the given item.
     *
     * @param item The item
     * @return The slot if at least one is available. Returns -1 otherwise.
     */
    public int findFirstFreeSlot(Item item) {
        for (int i = 0; i < 64; i++) {
            if (_inventory[i] == null) {
                return i;
            } else {
                if (_inventory[i].equals(item) && _inventory[i].getAmount() < item.getStackSize()) {
                    return i;
                }
            }
        }

        return -1;
    }

    public Item getItemInSlot(int slot) {
        return _inventory[slot];
    }

    public Player getParent() {
        return _parent;
    }

    public int size() {
        return _inventory.length;
    }
}
