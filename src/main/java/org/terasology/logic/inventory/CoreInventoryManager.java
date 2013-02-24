/*
 * Copyright 2013 Moving Blocks
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.Share;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.logic.inventory.events.InventoryChangeAcknowledgedRequest;
import org.terasology.logic.inventory.events.MoveItemAmountRequest;
import org.terasology.logic.inventory.events.MoveItemRequest;
import org.terasology.logic.inventory.events.ReceivedItemEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * System providing inventory related functionality
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.ALWAYS)
@Share({InventoryManager.class, SlotBasedInventoryManager.class})
public class CoreInventoryManager implements ComponentSystem, SlotBasedInventoryManager {
    private static final Logger logger = LoggerFactory.getLogger(CoreInventoryManager.class);

    // TODO: differ per item?
    public static final byte MAX_STACK = (byte) 99;

    @In
    private EntityManager entityManager;

    @In
    private NetworkSystem networkSystem;

    @In
    private LocalPlayer localPlayer;

    @In
    private EntitySystemLibrary entitySystemLibrary;

    // Client side prediction
    private int nextChangeId = 1;
    private Deque<MoveItemRequest> pendingMoves = Queues.newArrayDeque();

    private Map<EntityRef, InventoryComponent> predictedState = Maps.newHashMap();
    private TObjectIntMap<EntityRef> predictedStackCounts = new TObjectIntHashMap<EntityRef>();
    private List<EntityRef> predictedNewItems = Lists.newArrayList();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean giveItem(EntityRef inventoryEntity, EntityRef item) {
        if (networkSystem.getMode().isAuthority()) {
            // Send notification
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
                        putItemInSlot(inventoryEntity, inventory, freeSlot, item);
                        inventoryEntity.saveComponent(inventory);
                        return true;
                    } else if (itemChanged) {
                        item.saveComponent(itemComponent);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void removeItem(EntityRef inventoryEntity, EntityRef item) {
        if (networkSystem.getMode().isAuthority()) {
            InventoryComponent inventoryComponent = inventoryEntity.getComponent(InventoryComponent.class);
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);
            if (inventoryComponent != null && itemComponent != null) {
                int slot = inventoryComponent.itemSlots.indexOf(item);
                if (slot > -1) {
                    putItemInSlot(inventoryEntity, inventoryComponent, slot, item);
                    inventoryEntity.saveComponent(inventoryComponent);
                }
            }
        }
    }

    @Override
    public void moveItemAmount(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot, int amount) {
        if (networkSystem.getMode().isAuthority()) {
            InventoryComponent from = fromInventory.getComponent(InventoryComponent.class);
            InventoryComponent to = toInventory.getComponent(InventoryComponent.class);
            if (from != null && to != null) {
                moveItemAmountCommon(fromInventory, from, fromSlot, toInventory, to, toSlot, amount);
            }
        } else {
            MoveItemAmountRequest request = new MoveItemAmountRequest(fromInventory, fromSlot, toInventory, toSlot, amount, nextChangeId++);
            localPlayer.getClientEntity().send(request);
            pendingMoves.addLast(request);
            InventoryComponent from = getPredictedInventoryComponent(request.getFromInventory());
            InventoryComponent to = getPredictedInventoryComponent(request.getToInventory());
            moveItemAmountCommon(fromInventory, from, request.getFromSlot(), toInventory, to, request.getToSlot(), amount);
        }
    }

    @Override
    public void moveItem(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot) {
        if (networkSystem.getMode().isAuthority()) {
            InventoryComponent from = fromInventory.getComponent(InventoryComponent.class);
            InventoryComponent to = toInventory.getComponent(InventoryComponent.class);
            if (from != null && to != null) {
                moveItemCommon(fromInventory, from, fromSlot, toInventory, to, toSlot);
            }
        } else {
            MoveItemRequest request = new MoveItemRequest(fromInventory, fromSlot, toInventory, toSlot, nextChangeId++);
            localPlayer.getClientEntity().send(request);
            InventoryComponent from = getPredictedInventoryComponent(request.getFromInventory());
            InventoryComponent to = getPredictedInventoryComponent(request.getToInventory());
            moveItemCommon(fromInventory, from, request.getFromSlot(), toInventory, to, request.getToSlot());
        }
    }

    @Override
    public EntityRef putItemInSlot(EntityRef inventoryEntity, int slot, EntityRef item) {
        if (networkSystem.getMode().isAuthority()) {
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
                } else {
                    removeItem(inventoryEntity, existingItem);
                    putItemInSlot(inventoryEntity, inventory, slot, item);
                }
                return existingItem;
            }
        }
        return EntityRef.NULL;
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
    public int getStackSize(EntityRef item) {
        return getStackSize(item, item.getComponent(ItemComponent.class));
    }

    @Override
    public void setStackSize(EntityRef item, int newStackSize) {
        setStackSize(item, item.getComponent(ItemComponent.class), newStackSize);
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
                } else if (!otherItem.stackId.isEmpty() && otherItem.stackId.equals(itemComponent.stackId) && getStackSize(entity, otherItem) < MAX_STACK) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EntityRef getItemInSlot(EntityRef inventoryEntity, int slot) {
        InventoryComponent inventory = getInventoryComponent(inventoryEntity);
        if (inventory != null && slot >= 0 && slot < inventory.itemSlots.size()) {
            return inventory.itemSlots.get(slot);
        }
        return EntityRef.NULL;
    }

    @Override
    public int findSlotWithItem(EntityRef inventoryEntity, EntityRef item) {
        InventoryComponent inventory = getInventoryComponent(inventoryEntity);
        if (inventory != null) {
            return inventory.itemSlots.indexOf(item);
        }
        return -1;
    }

    @Override
    public int getNumSlots(EntityRef inventoryEntity) {
        InventoryComponent inventory = getInventoryComponent(inventoryEntity);
        if (inventory != null) {
            return inventory.itemSlots.size();
        }
        return 0;
    }

    private void mergeItems(ItemComponent itemComponent, EntityRef targetItem, ItemComponent targetItemComponent) {
        int stackSpace = MAX_STACK - targetItemComponent.stackCount;
        int amountToTransfer = Math.min(stackSpace, itemComponent.stackCount);
        targetItemComponent.stackCount += amountToTransfer;
        itemComponent.stackCount -= amountToTransfer;
        targetItem.saveComponent(targetItemComponent);
    }

    /*
     * Event handling
     */

    @ReceiveEvent(components = InventoryComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
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

    @ReceiveEvent(components = ClientComponent.class)
    public void onChange(InventoryChangeAcknowledgedRequest event, EntityRef inventoryEntity) {
        if (!networkSystem.getMode().isAuthority()) {
            logger.info("Received InventoryChangeAcknowledged for request {}", event.getChangeId());
            Iterator<MoveItemRequest> i = pendingMoves.iterator();
            while (i.hasNext()) {
                if (i.next().getChangeId() == event.getChangeId()) {
                    i.remove();
                }
            }
            recalculatePredictedState();
        }
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void onMoveItemRequest(MoveItemRequest request, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            logger.info("Received move item request");
            // TODO: Check if allowed to move item - must own or have open both inventories
            if (request instanceof MoveItemAmountRequest) {
                MoveItemAmountRequest moveAmount = (MoveItemAmountRequest) request;
                moveItemAmount(request.getFromInventory(), request.getFromSlot(), request.getToInventory(), request.getToSlot(), moveAmount.getAmount());
            } else {
                moveItem(request.getFromInventory(), request.getFromSlot(), request.getToInventory(), request.getToSlot());
            }
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }

    /**
     * Clears the current predicted state and recalculates it using remaining pending actions
     */
    private void recalculatePredictedState() {
        predictedStackCounts.clear();
        predictedState.clear();
        for (EntityRef item : predictedNewItems) {
            item.destroy();
        }
        predictedNewItems.clear();
        for (MoveItemRequest request : pendingMoves) {
            InventoryComponent from = getPredictedInventoryComponent(request.getFromInventory());
            InventoryComponent to = getPredictedInventoryComponent(request.getToInventory());
            if (request instanceof MoveItemAmountRequest) {
                moveItemAmountCommon(request.getFromInventory(), from, request.getFromSlot(), request.getToInventory(), to, request.getToSlot(), ((MoveItemAmountRequest) request).getAmount());
            } else {
                moveItemCommon(request.getFromInventory(), from, request.getFromSlot(), request.getToInventory(), to, request.getToSlot());
            }
        }
    }

    /**
     * Creates or retrieves a predicted inventory component. This is used when calculated the predicted state of an inventory,
     * and any predicted changes will be made to this component (and not the real inventory)
     * @param inventoryEntity
     * @return
     */
    private InventoryComponent getPredictedInventoryComponent(EntityRef inventoryEntity) {
        InventoryComponent result = predictedState.get(inventoryEntity);
        if (result == null) {
            result = inventoryEntity.getComponent(InventoryComponent.class);
            if (result != null) {
                result = entitySystemLibrary.getComponentLibrary().copy(result);
                predictedState.put(inventoryEntity, result);
            }
        }
        return result;
    }

    /*
     * Common logic used both server and client-side. Actual changes must be done using network aware methods that
     * will predict client-side and enact server-side.
     */

    /**
     *
     * @param fromEntity
     * @param fromInv
     * @param fromSlot
     * @param toEntity
     * @param toInv
     * @param toSlot
     */
    private void moveItemCommon(EntityRef fromEntity, InventoryComponent fromInv, int fromSlot, EntityRef toEntity, InventoryComponent toInv, int toSlot) {
        if (fromSlot < 0 || fromSlot >= fromInv.itemSlots.size() || toSlot < 0 || toSlot >= toInv.itemSlots.size()) {
            return;
        }
        EntityRef fromItem = fromInv.itemSlots.get(fromSlot);
        ItemComponent fromItemComp = fromItem.getComponent(ItemComponent.class);
        int fromItemStackCount = getStackSize(fromItem, fromItemComp);
        EntityRef toItem = toInv.itemSlots.get(toSlot);
        ItemComponent toItemComp = toItem.getComponent(ItemComponent.class);
        int toItemStackCount = getStackSize(toItem, toItemComp);

        if (fromItemComp != null) {
            if (toItemComp == null) {
                putItemInSlot(fromEntity, fromInv, fromSlot, EntityRef.NULL);
                putItemInSlot(toEntity, toInv, toSlot, fromItem);
            } else if (!fromItemComp.stackId.isEmpty() && Objects.equal(fromItemComp.stackId, toItemComp.stackId)) {
                int amountToTransfer = Math.min(MAX_STACK - toItemStackCount, fromItemStackCount);
                if (amountToTransfer == 0) {
                    putItemInSlot(fromEntity, fromInv, fromSlot, EntityRef.NULL);
                    putItemInSlot(toEntity, toInv, toSlot, fromItem);
                } else {
                    setStackSize(toItem, toItemComp, toItemStackCount + amountToTransfer);
                    if (amountToTransfer == fromItemStackCount) {
                        destroyItem(fromItem, fromInv, fromSlot);
                    } else {
                        setStackSize(fromItem, fromItemComp, fromItemStackCount - amountToTransfer);
                    }
                }
            } else {
                putItemInSlot(fromEntity, fromInv, fromSlot, toItem);
                putItemInSlot(toEntity, toInv, toSlot, fromItem);
            }
        }
    }

    /**
     * Common logic for moving item amounts - for use both server and client side.
     * @param fromEntity The inventory being moved fromInv
     * @param fromInv
     * @param fromSlot
     * @param toEntity
     * @param toInv
     * @param toSlot
     * @param amount
     */
    private void moveItemAmountCommon(EntityRef fromEntity, InventoryComponent fromInv, int fromSlot, EntityRef toEntity, InventoryComponent toInv, int toSlot, int amount) {
        if (fromSlot < 0 || fromSlot >= fromInv.itemSlots.size() || toSlot < 0 || toSlot >= toInv.itemSlots.size() || amount == 0) {
            return;
        }
        EntityRef fromItem = fromInv.itemSlots.get(fromSlot);
        ItemComponent fromItemComp = fromItem.getComponent(ItemComponent.class);
        int fromItemStackCount = getStackSize(fromItem, fromItemComp);
        EntityRef toItem = toInv.itemSlots.get(toSlot);
        ItemComponent toItemComp = toItem.getComponent(ItemComponent.class);
        int toItemStackCount = getStackSize(toItem, toItemComp);

        if (fromItemComp != null) {
            int amountToTransfer = Math.min(amount, fromItemStackCount);
            if (toItemComp == null) {
                if (amountToTransfer < fromItemComp.stackCount) {
                    EntityRef newItem = createNewStack(fromItem, amountToTransfer);
                    putItemInSlot(toEntity, toInv, toSlot, newItem);
                    setStackSize(fromItem, fromItemComp, fromItemStackCount - amountToTransfer);
                } else {
                    putItemInSlot(toEntity, toInv, toSlot, fromItem);
                    putItemInSlot(fromEntity, fromInv, fromSlot, EntityRef.NULL);
                }
            } else if (!fromItemComp.stackId.isEmpty() && Objects.equal(fromItemComp.stackId, toItemComp.stackId)) {
                amountToTransfer = Math.min(amountToTransfer, MAX_STACK - toItemComp.stackCount);
                if (amountToTransfer > 0) {
                    setStackSize(toItem, toItemComp, toItemStackCount + amountToTransfer);
                    if (amountToTransfer == fromItemComp.stackCount) {
                        destroyItem(fromItem, fromInv, fromSlot);
                    } else {
                        setStackSize(fromItem, fromItemComp, fromItemStackCount - amountToTransfer);
                    }
                }
            }
        }
    }

    /*
     * Lower-level, network aware methods. These predict client-side and enact server-side.
     */

    /**
     * Puts an item into a slot
     * @param inventoryEntity
     * @param inventory
     * @param slot
     * @param item
     */
    private void putItemInSlot(EntityRef inventoryEntity, InventoryComponent inventory, int slot, EntityRef item) {
        inventory.itemSlots.set(slot, item);

        if (networkSystem.getMode().isAuthority()) {
            inventoryEntity.saveComponent(inventory);

            // Update item ownership
            NetworkComponent networkComponent = item.getComponent(NetworkComponent.class);
            if (networkComponent != null) {
                networkComponent.owner = inventoryEntity;
                item.saveComponent(networkComponent);
            }
            if (item.exists()) {
                inventoryEntity.send(new ReceivedItemEvent(item, slot));
            }
        }
    }

    /**
     * Used to create a new stack when splitting out an amount. Safe for use client-side for prediction.
     * @param original
     * @param withAmount
     * @return The newly created item
     */
    private EntityRef createNewStack(EntityRef original, int withAmount) {
        Map<Class<? extends Component>, Component> componentMap = entityManager.copyComponents(original);
        NetworkComponent netComp = (NetworkComponent) componentMap.get(NetworkComponent.class);
        netComp.networkId = 0;
        ItemComponent itemComp = (ItemComponent) componentMap.get(ItemComponent.class);
        itemComp.stackCount = (byte) withAmount;
        EntityRef result = entityManager.create(componentMap.values());
        if (!networkSystem.getMode().isAuthority()) {
            predictedNewItems.add(result);
        }
        return result;
    }

    /**
     * Requests an item for deletion. Client side this is ignored
     * @param item
     */
    private void destroyItem(EntityRef item, InventoryComponent inventoryComponent, int slot) {
        if (networkSystem.getMode().isAuthority()) {
            item.destroy();
        } else {
            inventoryComponent.itemSlots.set(slot, EntityRef.NULL);
        }
    }

    private int getStackSize(EntityRef item, ItemComponent itemComponent) {
        if (predictedStackCounts.containsKey(item)) {
            return predictedStackCounts.get(item);
        }
        if (itemComponent != null) {
            return itemComponent.stackCount;
        }
        return 0;
    }

    private void setStackSize(EntityRef item, ItemComponent component, int newStackSize) {
        if (!networkSystem.getMode().isAuthority()) {
            predictedStackCounts.put(item, newStackSize);
        } else {
            component.stackCount = (byte) newStackSize;
            item.saveComponent(component);
        }
    }

    private InventoryComponent getInventoryComponent(EntityRef entity) {
        InventoryComponent inventory = predictedState.get(entity);
        if (inventory == null) {
            inventory = entity.getComponent(InventoryComponent.class);
        }
        return inventory;
    }
}
