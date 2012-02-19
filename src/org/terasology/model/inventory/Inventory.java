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


/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public class Inventory {

    private final Item[] _inventory = new Item[27];

    public Inventory() {
    	
    }

    /**
     * Store an item at a given slot position. Returns true if the item could be stored.
     *
     * @param slot The slot
     * @param item The item to store
     * @return True if item could be stored
     */
    public boolean storeItemInSlot(int slot, Item item) {
        if (slot < 0 || slot >= size())
            return false;

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
        if (slot < 0 || slot >= size())
            return null;

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
        if (slot < 0 || slot >= size())
            return null;

        if (_inventory[slot] != null) {
            Item item = _inventory[slot];
            _inventory[slot] = null;

            return item;
        }

        return null;
    }

    public Item getItemInSlot(int slot) {
        if (slot < 0 || slot >= size())
            return null;

        return _inventory[slot];
    }

    public int size() {
        return _inventory.length;
    }
    
    /**
     * Returns the first free slot for the given item.
     *
     * @param item The item
     * @return The slot if at least one is available. Returns -1 otherwise.
     */
    private int findFirstFreeSlot(Item item) {
        for (int i = 0; i < size(); i++) {
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
}
