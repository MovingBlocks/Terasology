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

package org.terasology.logic.inventory;

import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public interface InventoryManager {

    /**
     * @param inventoryEntity
     * @param item
     * @return Whether the given item can be added to the inventory
     */
    boolean canTakeItem(EntityRef inventoryEntity, EntityRef item);

    /**
     * @param inventoryEntity
     * @param item
     * @return Whether the item was fully consumed in being added to the inventory
     */
    boolean giveItem(EntityRef inventoryEntity, EntityRef item);

    /**
     * Removes an item from the inventory (but doesn't destroy it)
     *
     * @param inventoryEntity
     * @param item
     */
    void removeItem(EntityRef inventoryEntity, EntityRef item);

    /**
     * Removes an item from the inventory and destroys it
     *
     * @param inventoryEntity
     * @param item
     */
    void destroyItem(EntityRef inventoryEntity, EntityRef item);

    /**
     * @param itemA
     * @param itemB
     * @return Whether the two items can be merged (ignoring stack size limits)
     */
    boolean canStackTogether(EntityRef itemA, EntityRef itemB);

    /**
     * @param item
     * @return The size of the stack of items represented by the given item. 0 if the entity is not an item.
     */
    int getStackSize(EntityRef item);

    /**
     * @param item
     * @param newStackSize
     */
    void setStackSize(EntityRef item, int newStackSize);

    /**
     *  This version of setStackSize will destroy the item if newStackSize is <=0
     * @param item
     * @param inventoryEntity
     * @param newStackSize
     */
    void setStackSize(EntityRef item, EntityRef inventoryEntity, int newStackSize);
}
