package org.terasology.logic.inventory;

import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public interface InventoryManager {

    /**
     *
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
     * @param inventoryEntity
     * @param item
     */
    void removeItem(EntityRef inventoryEntity, EntityRef item);

    /**
     *
     * @param itemA
     * @param itemB
     * @return Whether the two items can be merged (ignoring stack size limits)
     */
    boolean canStackTogether(EntityRef itemA, EntityRef itemB);

}
