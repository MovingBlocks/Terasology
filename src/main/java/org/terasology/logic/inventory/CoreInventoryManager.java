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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CoreInventoryManager implements ComponentSystem, SlotBasedInventoryManager {
    private static final Logger logger = LoggerFactory.getLogger(CoreInventoryManager.class);

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
                    putItemInSlot(inventoryEntity, inventory, freeSlot, item, itemComponent);
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
    public void moveItemAmount(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot, int amount) {
        InventoryComponent fromInvComp = fromInventory.getComponent(InventoryComponent.class);
        InventoryComponent toInvComp = toInventory.getComponent(InventoryComponent.class);
        if (fromInvComp == null || fromSlot < 0 || fromSlot >= fromInvComp.itemSlots.size()
                || toInvComp == null || toSlot < 0 || toSlot >= toInvComp.itemSlots.size() || amount <= 0) {
            return;
        }

        EntityRef fromItem = fromInvComp.itemSlots.get(fromSlot);
        ItemComponent fromItemComp = fromItem.getComponent(ItemComponent.class);
        EntityRef toItem = toInvComp.itemSlots.get(toSlot);
        ItemComponent toItemComp = toItem.getComponent(ItemComponent.class);
        if (fromItemComp != null) {
            int amountToTransfer = Math.min(amount, fromItemComp.stackCount);
            if (toItemComp == null) {
                if (amountToTransfer < fromItemComp.stackCount) {
                    EntityRef newItem = entityManager.copy(fromItem);
                    ItemComponent newItemComponent = newItem.getComponent(ItemComponent.class);
                    newItemComponent.stackCount = (byte)amountToTransfer;
                    putItemInSlot(toInventory, toInvComp, toSlot, newItem, newItemComponent);
                    fromItemComp.stackCount -= amountToTransfer;
                    fromItem.saveComponent(fromItemComp);
                } else {
                    putItemInSlot(toInventory, toInvComp, toSlot, fromItem, fromItemComp);
                }
            } else if (!fromItemComp.stackId.isEmpty() && Objects.equal(fromItemComp.stackId, toItemComp.stackId)) {
                amountToTransfer = Math.min(amountToTransfer, MAX_STACK - toItemComp.stackCount);
                if (amountToTransfer > 0) {
                    toItemComp.stackCount += amountToTransfer;
                    toItem.saveComponent(toItemComp);
                    if (amountToTransfer == fromItemComp.stackCount) {
                        fromItem.destroy();
                    } else {
                        fromItemComp.stackCount -= amountToTransfer;
                        fromItem.saveComponent(fromItemComp);
                    }
                }
            }
        }
    }

    @Override
    public void moveItem(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot) {
        InventoryComponent fromInvComp = fromInventory.getComponent(InventoryComponent.class);
        InventoryComponent toInvComp = toInventory.getComponent(InventoryComponent.class);
        if (fromInvComp == null || fromSlot < 0 || fromSlot >= fromInvComp.itemSlots.size()
                || toInvComp == null || toSlot < 0 || toSlot >= toInvComp.itemSlots.size()) {
            return;
        }


        EntityRef fromItem = fromInvComp.itemSlots.get(fromSlot);
        ItemComponent fromItemComp = fromItem.getComponent(ItemComponent.class);
        EntityRef toItem = toInvComp.itemSlots.get(toSlot);
        ItemComponent toItemComp = toItem.getComponent(ItemComponent.class);

        if (fromItemComp != null) {
            if (toItemComp == null) {
                putItemInSlot(toInventory, toSlot, fromInvComp.itemSlots.get(fromSlot));
            } else if (!fromItemComp.stackId.isEmpty() && Objects.equal(fromItemComp.stackId, toItemComp.stackId)) {
                int amountToTransfer = Math.min(MAX_STACK - toItemComp.stackCount, fromItemComp.stackCount);
                if (amountToTransfer == 0) {
                    putItemInSlot(fromInventory, fromInvComp, fromSlot, toItem, toItemComp);
                    putItemInSlot(toInventory, toInvComp, toSlot, fromItem, fromItemComp);
                } else {
                    toItemComp.stackCount += amountToTransfer;
                    toItem.saveComponent(toItemComp);
                    if (amountToTransfer == fromItemComp.stackCount) {
                        fromItem.destroy();
                    } else {
                        fromItemComp.stackCount -= amountToTransfer;
                        fromItem.saveComponent(fromItemComp);
                    }
                }
            } else {
                putItemInSlot(fromInventory, fromInvComp, fromSlot, toItem, toItemComp);
                putItemInSlot(toInventory, toInvComp, toSlot, fromItem, fromItemComp);
            }
        }


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
                putItemInSlot(inventoryEntity, inventory, slot, item, itemComponent);
                return existingItem;
            }
        }
        return item;
    }

    private void putItemInSlot(EntityRef inventoryEntity, InventoryComponent inventory, int slot, EntityRef item, ItemComponent itemComponent) {
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
    public int findSlotWithItem(EntityRef inventoryEntity, EntityRef item) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            return inventory.itemSlots.indexOf(item);
        }
        return -1;
    }

    @Override
    public int getNumSlots(EntityRef inventoryEntity) {
        InventoryComponent inventory = inventoryEntity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            return inventory.itemSlots.size();
        }
        return 0;
    }


    @ReceiveEvent(components = InventoryComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        for (EntityRef content : inventory.itemSlots) {
            if (content != entity) {
                content.destroy();
            } else {
                logger.warn("Inventory contained itself: {}", entity);
            }
        }
    }

}
