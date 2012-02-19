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

	private Cubbyhole[] _cubbies;
    private final Item[] _items = new Item[27];
    private final int[] _counts = new int[27];

    public Inventory() {
    	_cubbies = new Cubbyhole[27];
    }
    
    public int getItemCount(int slot) {
    	validateSlot(slot);
    	
    	return _counts[slot];
    }
    
    public int getItemCount(Item item) {
    	for (int i = 0; i < _items.length; i++) {
    		if (item.equals(_items[i])) {
    			return _counts[i];
    		}
    	}
    	
    	return 0;
    }

    /**
     * Store an item at a given slot position. Returns true if the item could be stored.
     *
     * @param slot The slot
     * @param item The item to store
     * @return True if item could be stored
     */
    public boolean addItemAt(int slot, Item item) {
        if (slot < 0 || slot >= size())
            return false;

        // The slot is empty so no problem here
        if (_items[slot] == null) {
            _items[slot] = item;
            _counts[slot]++;
            return true;
        } else {
            if (_items[slot].equals(item) && _counts[slot] < item.getStackSize()) {
                _counts[slot]++;
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
    public boolean addItem(Item item) {
        return addItemAt(findFirstFreeSlot(item), item);
    }
    
    public boolean addItem(Item item, int count) {
    	int slot = findFirstFreeSlot(item);
    	boolean result = addItemAt(slot, item);
    	
    	_counts[slot] = _counts[slot] + count - 1;
    	
    	return result;
    }

    /**
     * Removes one item at the given slot position.
     *
     * @param slot The slot
     * @return The removed object. Null if nothing could be removed.
     */
    public Item removeOneItemAt(int slot) {
        if (slot < 0 || slot >= size())
            return null;

        if (_items[slot] != null) {
            Item item = _items[slot];
            _counts[slot]--;

            if (_counts[slot] == 0) {
                _items[slot] = null;
            }

            return item;
        }

        return null;
    }

    /**
     * Removes all items at slot.
     *
     * @param slot The slot
     * @return The removed object. Null if nothing could be removed.
     */
    public Item clearSlot(int slot) {
        if (slot < 0 || slot >= size())
            return null;

        if (_items[slot] != null) {
            Item item = _items[slot];
            _items[slot] = null;

            return item;
        }

        return null;
    }

    public Item getItemAt(int slot) {
        if (slot < 0 || slot >= size())
            return null;

        return _items[slot];
    }

    public int size() {
        return _items.length;
    }
    
    /**
     * Returns the first free slot for the given item.
     *
     * @param item The item
     * @return The slot if at least one is available. Returns -1 otherwise.
     */
    private int findFirstFreeSlot(Item item) {
        for (int i = 0; i < size(); i++) {
            if (_items[i] == null) {
                return i;
            } else {
                if (_items[i].equals(item) && _counts[i] < item.getStackSize()) {
                    return i;
                }
            }
        }

        return -1;
    }
    
    private void validateSlot(int slot) {
    	
    }
}
