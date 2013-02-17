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
     *
     * @param inventoryEntity
     * @param slot
     * @return The item in the given slot
     */
    EntityRef getItemInSlot(EntityRef inventoryEntity, int slot);

    /**
     * Attempts to move an amount of an item from one location to another. If the target location cannot except the item
     * no change occurs.
     * @param fromInventory
     * @param fromSlot
     * @param toInventory
     * @param toSlot
     * @param amount
     */
    void moveItemAmount(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot, int amount);

    /**
     * Moves an item into the target slot. If possible, the item is merged partially or fully with the target item.
     * Otherwise the items are swapped.
     * @param fromInventory
     * @param fromSlot
     * @param toInventory
     * @param toSlot
     */
    void moveItem(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot);

    /**
     *
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
