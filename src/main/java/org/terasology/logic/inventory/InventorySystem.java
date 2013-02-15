/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.base.Objects;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.Share;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.network.NetworkComponent;

/**
 * System providing inventory related functionality
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share({InventoryManager.class, SlotBasedInventoryManager.class})
public class InventorySystem implements ComponentSystem, SlotBasedInventoryManager {

    // TODO: differ per item?
    public static final byte MAX_STACK = (byte) 99;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean canTakeItem(EntityRef inventoryEntity, EntityRef item) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (inventory != null && itemComponent != null) {
            for (EntityRef entity : inventory.itemSlots) {
                ItemComponent otherItem = entity.getComponent(ItemComponent.class);
                if (otherItem == null) {
                    return true;
                } else if (!otherItem.stackId.isEmpty() && otherItem.stackId.equals(itemComponent.stackId) && otherItem.stackCount < MAX_STACK) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean giveItem(EntityRef inventoryEntity, EntityRef item) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (inventory != null && itemComponent != null) {
            boolean itemChanged = false;
            if (!itemComponent.stackId.isEmpty()) {
                for (EntityRef entity : inventory.itemSlots) {
                    ItemComponent otherItem = entity.getComponent(ItemComponent.class);
                    if (otherItem != null && otherItem.stackId.equals(itemComponent.stackId) && otherItem.stackCount < MAX_STACK) {
                        mergeItems(itemComponent, entity, otherItem);
                        itemChanged = true;
                    }
                    if (itemComponent.stackCount == 0) {
                        item.destroy();
                        return true;
                    }
                }
            }
            if (itemComponent.stackCount > 0) {
                int freeSlot = inventory.itemSlots.indexOf(EntityRef.NULL);
                if (freeSlot > -1) {
                    putItemInSlot(item, itemComponent, freeSlot, inventoryEntity, inventory);
                    return true;
                } else if (itemChanged) {
                    item.saveComponent(itemComponent);
                }
            }
        }
        return false;
    }

    private void mergeItems(ItemComponent itemComponent, EntityRef targetItem, ItemComponent targetItemComponent) {
        int stackSpace = MAX_STACK - targetItemComponent.stackCount;
        int amountToTransfer = Math.min(stackSpace, itemComponent.stackCount);
        targetItemComponent.stackCount += amountToTransfer;
        itemComponent.stackCount -= amountToTransfer;
        targetItem.saveComponent(targetItemComponent);
    }

    @Override
    public void removeItem(EntityRef inventoryEntity, EntityRef item) {
        InventoryComponent inventoryComponent = inventoryEntity.getComponent(InventoryComponent.class);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (inventoryComponent != null && itemComponent != null && itemComponent.container.equals(inventoryEntity)) {
            int slot = inventoryComponent.itemSlots.indexOf(item);
            if (slot > -1) {
                inventoryComponent.itemSlots.set(slot, EntityRef.NULL);
                inventoryEntity.saveComponent(inventoryComponent);
            }
            itemComponent.container = EntityRef.NULL;
            item.saveComponent(itemComponent);
        }
    }

    @Override
    public boolean canStackTogether(EntityRef itemA, EntityRef itemB) {
        ItemComponent itemAComp = itemA.getComponent(ItemComponent.class);
        if (itemAComp.stackId == null || itemAComp.stackId.isEmpty()) {
            return false;
        }
        ItemComponent itemBComp = itemB.getComponent(ItemComponent.class);
        return itemBComp != null && Objects.equal(itemAComp.stackId, itemBComp.stackId);
    }

    @Override
    public EntityRef getItemInSlot(EntityRef inventoryEntity, int slot) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        if (inventory != null && slot >= 0 && slot < inventory.itemSlots.size()) {
            return inventory.itemSlots.get(slot);
        }
        return EntityRef.NULL;
    }

    @Override
    public EntityRef putItemInSlot(EntityRef inventoryEntity, int slot, EntityRef item) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (inventory != null && itemComponent != null && slot >= 0 && slot < inventory.itemSlots.size()) {
            EntityRef existingItem = inventory.itemSlots.get(slot);
            ItemComponent slotItemComponent = existingItem.getComponent(ItemComponent.class);

            if (!itemComponent.stackId.isEmpty() && slotItemComponent != null && Objects.equal(itemComponent.stackId, slotItemComponent.stackId) && slotItemComponent.stackCount < MAX_STACK) {
                mergeItems(itemComponent, existingItem, slotItemComponent);
                if (itemComponent.stackCount == 0) {
                    item.destroy();
                } else {
                    item.saveComponent(itemComponent);
                }
                return item;
            } else {
                removeItem(inventoryEntity, existingItem);
                putItemInSlot(item, itemComponent, slot, inventoryEntity, inventory);
                return existingItem;
            }
        }
        return item;
    }

    @Override
    public boolean putAmountInSlot(EntityRef inventoryEntity, int slot, EntityRef item, int amount) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (inventory != null && itemComponent != null) {
            ItemComponent targetItemComponent = inventory.itemSlots.get(slot).getComponent(ItemComponent.class);
            if (itemComponent.stackId.isEmpty() || amount >= itemComponent.stackCount) {
                // Move the whole thing
                putItemInSlot(inventoryEntity, slot, item);
                return true;
            } else if (targetItemComponent == null) {
                EntityRef newItem = entityManager.copy(item);
                ItemComponent newItemComponent = newItem.getComponent(ItemComponent.class);
                newItemComponent.stackCount = (byte) amount;
                itemComponent.stackCount -= amount;
                putItemInSlot(newItem, newItemComponent, slot, inventoryEntity, inventory);
                item.saveComponent(itemComponent);
            } else if (Objects.equal(targetItemComponent.stackId, itemComponent.stackId)) {
                int amountToTransfer = Math.min(amount, MAX_STACK - targetItemComponent.stackCount);
                if (amountToTransfer > 0) {
                    targetItemComponent.stackCount += amountToTransfer;
                    itemComponent.stackCount -= amountToTransfer;
                    inventory.itemSlots.get(slot).saveComponent(targetItemComponent);
                    item.saveComponent(itemComponent);
                }
            }
        }
        return false;
    }

    private void putItemInSlot(EntityRef item, ItemComponent itemComponent, int slot, EntityRef inventoryEntity, InventoryComponent inventory) {
        inventory.itemSlots.set(slot, item);
        removeItem(itemComponent.container, item);
        itemComponent.container = inventoryEntity;
        item.saveComponent(itemComponent);
        inventoryEntity.saveComponent(inventory);

        // Update item ownership
        NetworkComponent networkComponent = item.getComponent(NetworkComponent.class);
        if (networkComponent != null) {
            networkComponent.owner = inventoryEntity;
            item.saveComponent(networkComponent);
        }
        inventoryEntity.send(new ReceivedItemEvent(item, slot));
    }


    @Override
    public int findSlotForItem(EntityRef inventoryEntity, EntityRef item) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            return inventory.itemSlots.indexOf(item);
        }
        return -1;
    }


    @ReceiveEvent(components = InventoryComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        for (EntityRef content : inventory.itemSlots) {
            content.destroy();
        }
    }

}
