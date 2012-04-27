package org.terasology.componentSystem.items;

import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.inventory.ReceiveItemEvent;

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
    @ReceiveEvent(components=InventoryComponent.class)
    public void onReceiveItem(ReceiveItemEvent event, EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        ItemComponent item = event.getItem().getComponent(ItemComponent.class);
        if (inventory == null || item == null)
            return;

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
                            event.getItem().destroy();
                            return;
                        }
                    }
                }
            }
        }

        // Then free spaces
        int freeSlot = inventory.itemSlots.indexOf(EntityRef.NULL);
        if (freeSlot != -1) {
            inventory.itemSlots.set(freeSlot, event.getItem());
            InventoryComponent otherInventory = item.container.getComponent(InventoryComponent.class);
            if (otherInventory != null) {
                int heldSlot = otherInventory.itemSlots.indexOf(event.getItem());
                if (heldSlot != -1) {
                    otherInventory.itemSlots.set(heldSlot, EntityRef.NULL);
                    item.container.saveComponent(otherInventory);
                }
            }
            item.container = entity;
            event.getItem().saveComponent(item);
            entity.saveComponent(inventory);
            return;
        }
        if (itemChanged) {
            event.getItem().saveComponent(item);
        }
    }
}
