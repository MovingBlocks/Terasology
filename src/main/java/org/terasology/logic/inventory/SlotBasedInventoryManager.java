package org.terasology.logic.inventory;

import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public interface SlotBasedInventoryManager extends InventoryManager {

    /**
     * Adds an item to a specific slot, "swappping" it with the item in the slot (which is removed from the inventory
     * and returned.
     * If the items are stackable, as much as possible will be put into the slot and the residual returned
     * @param inventoryEntity
     * @param slot
     * @param item
     * @return The item previously in the slot
     */
    EntityRef putItemInSlot(EntityRef inventoryEntity, int slot, EntityRef item);

    /**
     * Moves some of an item to a specific slot. If that slot cannot accept the item, then no transfer happens.
     *
     * @param inventoryEntity
     * @param slot
     * @param item
     * @param amount
     * @return Whether the item was completely consumed in the transfer process
     */
    boolean putAmountInSlot(EntityRef inventoryEntity, int slot, EntityRef item, int amount);

    /**
     *
     * @param inventoryEntity
     * @param slot
     * @return The item in the given slot
     */
    EntityRef getItemInSlot(EntityRef inventoryEntity, int slot);

    /**
     *
     * @param inventoryEntity
     * @param item
     * @return The slot containing the given item, or -1 if it wasn't found in the inventory
     */
    int findSlotForItem(EntityRef inventoryEntity, EntityRef item);

}
