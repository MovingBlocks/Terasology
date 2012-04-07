package org.terasology.componentSystem.items;

import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;

/**
 * System providing inventory related functionality
 * @author Immortius <immortius@gmail.com>
 */
public class InventorySystem implements EventHandlerSystem {

    // TODO: differ per item?
    public static final byte MAX_STACK = (byte)99;

    public void initialise() {

    }


    @ReceiveEvent(components=InventoryComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        for (EntityRef content : inventory.itemSlots) {
            content.destroy();
        }
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

        boolean itemChanged = false;

        if (!item.stackId.isEmpty()) {
            // First check for existing stacks
            for (EntityRef itemStack : inventory.itemSlots) {
                ItemComponent stackComp = itemStack.getComponent(ItemComponent.class);
                if (stackComp != null) {
                    if (item.stackId.equals(stackComp.stackId)) {
                        int stackSpace = MAX_STACK - stackComp.stackCount;
                        int amountToTransfer = Math.min(stackSpace, item.stackCount);
                        stackComp.stackCount += amountToTransfer;
                        item.stackCount -= amountToTransfer;
                        itemStack.saveComponent(stackComp);
                        itemChanged = true;

                        if (item.stackCount == 0) {
                            itemEntity.destroy();
                            return true;
                        }
                    }
                }
            }
        }

        // Then free spaces
        int freeSlot = inventory.itemSlots.indexOf(EntityRef.NULL);
        if (freeSlot != -1) {
            inventory.itemSlots.set(freeSlot, itemEntity);
            item.container = inventoryEntity;
            itemEntity.saveComponent(item);
            inventoryEntity.saveComponent(inventory);
            return true;
        }
        if (itemChanged) {
            itemEntity.saveComponent(item);
        }
        return false;
    }
}
