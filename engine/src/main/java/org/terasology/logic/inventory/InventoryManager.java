/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityRef;

/**
 * @author Immortius
 */
public interface InventoryManager {

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
     * @param inventoryEntity
     * @param slot
     * @return The item in the given slot
     */
    EntityRef getItemInSlot(EntityRef inventoryEntity, int slot);

    /**
     * @param inventoryEntity
     * @param item
     * @return The slot containing the given item, or -1 if it wasn't found in the inventory
     */
    int findSlotWithItem(EntityRef inventoryEntity, EntityRef item);

    /**
     * @param inventoryEntity
     * @return The number of slots the given entity has
     */
    int getNumSlots(EntityRef inventoryEntity);
}
