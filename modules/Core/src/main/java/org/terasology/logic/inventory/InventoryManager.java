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

import java.util.List;

/**
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

    /**
     * Puts item into an inventory.
     *
     * @param inventory  Inventory to put item into.
     * @param instigator Instigator of the action.
     * @param item       Item to put into inventory.
     * @return If the action was successful.
     */
    boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item);

    /**
     * Puts item into an inventory, into specified slot.
     *
     * @param inventory  Inventory to put item into.
     * @param instigator Instigator of the action.
     * @param item       Item to put into inventory.
     * @param slot       Slot to which put the item into.
     * @return If the action was successful.
     */
    boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item, int slot);

    /**
     * Puts item into an inventory, into specified range of slots.
     *
     * @param inventory  Inventory to put item into.
     * @param instigator Instigator of the action.
     * @param item       Item to put into inventory.
     * @param slots      Range of slots to which put the item into.
     * @return If the action was successful.
     */
    boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item, List<Integer> slots);

    /**
     * Removes whole stack of item from an inventory.
     *
     * @param inventory      Inventory to remove item from.
     * @param instigator     Instigator of the action.
     * @param item           Item to remove from inventory.
     * @param destroyRemoved If the removed item should be destroyed.
     * @return If action fails - <code>null</code> value will be returned. If successful and destroyRemoved is true -
     *         EntityRef.NULL will be returned, otherwise the removed item entity will be returned instead.
     */
    EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved);

    /**
     * Removes specified amount of the item from an inventory.
     *
     * @param inventory      Inventory to remove item from.
     * @param instigator     Instigator of the action.
     * @param item           Item to remove from inventory.
     * @param destroyRemoved If the removed item should be destroyed.
     * @param count          Amount of items to remove.
     * @return If action fails - <code>null</code> value will be returned. If successful and destroyRemoved is true -
     *         EntityRef.NULL will be returned, otherwise the removed item entity will be returned instead.
     */
    EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved, int count);

    /**
     * Removes whole stacks of item from an inventory. Items passed should be the same (indistinguishable).
     *
     * @param inventory      Inventory to remove item from.
     * @param instigator     Instigator of the action.
     * @param items          Item to remove from inventory.
     * @param destroyRemoved If the removed item should be destroyed.
     * @return If action fails - <code>null</code> value will be returned. If successful and destroyRemoved is true -
     *         EntityRef.NULL will be returned, otherwise the removed item entity will be returned instead.
     */
    EntityRef removeItem(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved);

    /**
     * Removes specified amount of item from an inventory. Items passed should be the same (indistinguishable).
     *
     * @param inventory      Inventory to remove item from.
     * @param instigator     Instigator of the action.
     * @param items          Item to remove from inventory.
     * @param destroyRemoved If the removed item should be destroyed.
     * @param count          Amount of items to remove.
     * @return If action fails - <code>null</code> value will be returned. If successful and destroyRemoved is true -
     *         EntityRef.NULL will be returned, otherwise the removed item entity will be returned instead.
     */
    EntityRef removeItem(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved, int count);

    /**
     * Removes specified amount of item from an inventory's slot.
     * @param inventory       Inventory to remove item from.
     * @param instigator      Instigator of the action.
     * @param slotNo          Slot to remove the item from.
     * @param destroyRemoved  If the removed item should be destroyed.
     * @param count           Amount of items to remove.
     * @return If action fails - <code>null</code> value will be returned. If successful and destroyRemoved is true -
     *         EntityRef.NULL will be returned, otherwise the removed item entity will be returned instead.
     */
    EntityRef removeItem(EntityRef inventory, EntityRef instigator, int slotNo, boolean destroyRemoved, int count);

    /**
     * Moves a specified amount of items from one inventory to another.
     *
     * @param fromInventory Inventory to move item from.
     * @param instigator    Instigator of the action.
     * @param slotFrom      Slot to move from.
     * @param toInventory   Inventory to move item to.
     * @param slotTo        Slot to move to.
     * @param count         Amount of items to move.
     * @return If the action was successful.
     */
    boolean moveItem(EntityRef fromInventory, EntityRef instigator, int slotFrom, EntityRef toInventory, int slotTo, int count);

    /**
     * Tries to move a item smartly to the specified slots.
     * It will try to fill up existing stacks if possible.
     *
     * @param fromInventory Inventory to move item from.
     * @param instigator    Instigator of the action.
     * @param slotFrom      Slot to move from.
     * @param toInventory   Inventory to move item to.
     * @param toSlots   slots to move the item to.     *
     * @return If the action was successful. The action counts as successful if at least one item got moved.
     */
    boolean moveItemToSlots(EntityRef instigator, EntityRef fromInventory, int slotFrom, EntityRef toInventory, List<Integer> toSlots);

    /**
     * Switches items in two inventories.
     *
     * @param fromInventory Inventory to switch item from.
     * @param instigator    Instigator of the action.
     * @param slotFrom      Slot to switch item from.
     * @param toInventory   Inventory switch item to.
     * @param slotTo        Slot to switch item to.
     * @return If the action was successful.
     */
    boolean switchItem(EntityRef fromInventory, EntityRef instigator, int slotFrom, EntityRef toInventory, int slotTo);
}
