package org.terasology.logic.systems.items;

import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.componentSystem.ComponentSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class InventorySystem implements ComponentSystem {

    public static final byte MAX_STACK = (byte)99;

    public void initialise() {

    }

    /**
     * Adds an item to an inventory. If the item stacks it may be destroyed or partially moved (stack count diminished).
     * @param inventoryEntity
     * @param itemEntity
     * @return Whether the item was successfully added to the container in full
     */
    public boolean addItem(EntityRef inventoryEntity, EntityRef itemEntity) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        ItemComponent item = itemEntity.getComponent(ItemComponent.class);
        if (inventory == null || item == null)
            return false;

        // First check for existing stacks
        for (EntityRef itemStack : inventory.itemSlots) {
            if (itemStack != null) {
                ItemComponent stackComp = itemStack.getComponent(ItemComponent.class);
                if (item.stackId.equals(stackComp.stackId)) {
                    int stackSpace = MAX_STACK - stackComp.stackCount;
                    int amountToTransfer = Math.min(stackSpace, item.stackCount);
                    stackComp.stackCount += amountToTransfer;
                    item.stackCount -= amountToTransfer;

                    if (item.stackCount == 0) {
                        itemEntity.destroy();
                        return true;
                    }
                }
            }
        }

        // Then free spaces
        int freeSlot = inventory.itemSlots.indexOf(null);
        if (freeSlot != -1) {
            inventory.itemSlots.set(freeSlot, itemEntity);
            item.container = inventoryEntity;
            return true;
        }
        return false;
    }
}
