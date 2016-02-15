/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.inventory.action.MoveItemAction;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.logic.inventory.action.SwitchItemAction;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.BeforeItemRemovedFromInventory;
import org.terasology.logic.inventory.events.InventoryChangeAcknowledgedRequest;
import org.terasology.logic.inventory.events.MoveItemAmountRequest;
import org.terasology.logic.inventory.events.MoveItemRequest;
import org.terasology.logic.inventory.events.MoveItemToSlotsRequest;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = InventoryManager.class)
public class InventoryAuthoritySystem extends BaseComponentSystem implements InventoryManager {
    @In
    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void switchItem(SwitchItemAction event, EntityRef entity) {
        switchItem(entity, event.getInstigator(), event.getSlotFrom(), event.getTo(), event.getSlotTo());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void moveItem(MoveItemAction event, EntityRef entity) {
        moveItem(entity, event.getInstigator(), event.getSlotFrom(), event.getTo(), event.getSlotTo(), event.getCount());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void removeItem(RemoveItemAction event, EntityRef entity) {
        final EntityRef result = removeItemInternal(entity, event.getInstigator(), event.getItems(), event.isDestroyRemoved(), event.getCount());
        if (result != null) {
            if (result != EntityRef.NULL) {
                event.setRemovedItem(result);
            }
            event.consume();
        }
    }

    private EntityRef removeItemFromSlots(EntityRef instigator, boolean destroyRemoved, EntityRef entity, List<Integer> slotsWithItem, int toRemove) {
        int shrinkSlotNo = -1;
        int shrinkCountResult = 0;

        List<Integer> slotsTotallyConsumed = new LinkedList<>();

        int removesRemaining = toRemove;
        for (int slot : slotsWithItem) {
            EntityRef itemAtEntity = InventoryUtils.getItemAt(entity, slot);
            ItemComponent itemAt = itemAtEntity.getComponent(ItemComponent.class);
            if (itemAt.stackCount <= removesRemaining) {
                if (canRemoveItemFromSlot(instigator, entity, itemAtEntity, slot)) {
                    slotsTotallyConsumed.add(slot);
                    removesRemaining -= itemAt.stackCount;
                }
            } else {
                shrinkSlotNo = slot;
                shrinkCountResult = itemAt.stackCount - removesRemaining;
                removesRemaining = 0;
            }

            if (removesRemaining == 0) {
                break;
            }
        }

        if (removesRemaining > 0) {
            return null;
        }

        EntityRef removed = null;
        int removedCount = 0;
        for (int slot : slotsTotallyConsumed) {
            EntityRef itemAt = InventoryUtils.getItemAt(entity, slot);
            removedCount += InventoryUtils.getStackCount(itemAt);

            if (destroyRemoved) {
                InventoryUtils.putItemIntoSlot(entity, EntityRef.NULL, slot);
                itemAt.destroy();
            } else {
                if (removed == null) {
                    InventoryUtils.putItemIntoSlot(entity, EntityRef.NULL, slot);
                    removed = itemAt;
                } else {
                    InventoryUtils.putItemIntoSlot(entity, EntityRef.NULL, slot);
                    itemAt.destroy();
                }
            }
        }

        if (shrinkSlotNo > -1) {
            EntityRef itemAt = InventoryUtils.getItemAt(entity, shrinkSlotNo);
            removedCount += InventoryUtils.getStackCount(itemAt) - shrinkCountResult;
            if (destroyRemoved) {
                InventoryUtils.adjustStackSize(entity, shrinkSlotNo, shrinkCountResult);
            } else {
                if (removed == null) {
                    removed = entityManager.copy(itemAt);
                }
                InventoryUtils.adjustStackSize(entity, shrinkSlotNo, shrinkCountResult);
            }
        }

        if (removed != null) {
            ItemComponent item = removed.getComponent(ItemComponent.class);
            if (item.stackCount != removedCount) {
                item.stackCount = (byte) removedCount;
                removed.saveComponent(item);
            }
            return removed;
        }
        return EntityRef.NULL;
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void giveItem(GiveItemAction event, EntityRef entity) {
        if (giveItem(entity, event.getInstigator(), event.getItem(), event.getSlots())) {
            event.consume();
        }
    }

    private boolean giveItemToSlots(EntityRef instigator, EntityRef entity, EntityRef item, List<Integer> slots) {
        int toConsume = InventoryUtils.getStackCount(item);
        Map<Integer, Integer> consumableCount = new LinkedHashMap<>();

        // First: check which slots we can merge into
        for (int slot : slots) {
            EntityRef itemAtEntity = InventoryUtils.getItemAt(entity, slot);
            ItemComponent itemAt = itemAtEntity.getComponent(ItemComponent.class);
            if (itemAt != null && InventoryUtils.isSameItem(item, itemAtEntity)) {
                int spaceInSlot = itemAt.maxStackSize - itemAt.stackCount;
                int toAdd = Math.min(toConsume, spaceInSlot);
                if (toAdd > 0) {
                    consumableCount.put(slot, toAdd);
                    toConsume -= toAdd;
                    if (toConsume == 0) {
                        break;
                    }
                }
            }
        }

        int emptySlotNo = -1;
        int emptySlotCount = toConsume;
        if (toConsume > 0) {
            // Next: check which slots are empty and figure out where to add
            for (int slot : slots) {
                EntityRef itemAtEntity = InventoryUtils.getItemAt(entity, slot);
                ItemComponent itemAt = itemAtEntity.getComponent(ItemComponent.class);
                if (itemAt == null && canPutItemIntoSlot(instigator, entity, item, slot)) {
                    emptySlotNo = slot;
                    emptySlotCount = toConsume;
                    toConsume = 0;
                    break;
                }
            }
        }

        if (toConsume > 0) {
            return false;
        }

        for (Map.Entry<Integer, Integer> slotCount : consumableCount.entrySet()) {
            int slot = slotCount.getKey();
            int count = slotCount.getValue();
            EntityRef itemAtEntity = InventoryUtils.getItemAt(entity, slot);
            ItemComponent itemAt = itemAtEntity.getComponent(ItemComponent.class);
            InventoryUtils.adjustStackSize(entity, slot, itemAt.stackCount + count);
        }

        if (emptySlotNo > -1) {
            ItemComponent sourceItem = item.getComponent(ItemComponent.class);
            sourceItem.stackCount = (byte) emptySlotCount;
            item.saveComponent(sourceItem);

            InventoryUtils.putItemIntoSlot(entity, item, emptySlotNo);
        } else {
            item.destroy();
        }

        return true;
    }

    private boolean canPutItemIntoSlot(EntityRef instigator, EntityRef entity, EntityRef item, int slot) {
        if (!item.exists()) {
            return true;
        }
        BeforeItemPutInInventory itemPut = new BeforeItemPutInInventory(instigator, item, slot);
        entity.send(itemPut);
        return !itemPut.isConsumed();
    }

    private boolean canRemoveItemFromSlot(EntityRef instigator, EntityRef entity, EntityRef item, int slot) {
        if (!item.exists()) {
            return true;
        }
        BeforeItemRemovedFromInventory itemRemoved = new BeforeItemRemovedFromInventory(instigator, item, slot);
        entity.send(itemRemoved);
        return !itemRemoved.isConsumed();
    }

    @ReceiveEvent
    public void moveItemAmountRequest(MoveItemAmountRequest request, EntityRef entity) {
        try {
            InventoryUtils.moveItemAmount(request.getInstigator(), request.getFromInventory(), request.getFromSlot(),
                    request.getToInventory(), request.getToSlot(), request.getAmount());
        } finally {
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }

    @ReceiveEvent
    public void moveItemRequest(MoveItemRequest request, EntityRef entity) {
        try {
            InventoryUtils.moveItem(request.getInstigator(), request.getFromInventory(), request.getFromSlot(),
                    request.getToInventory(), request.getToSlot());

        } finally {
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }

    @ReceiveEvent
    public void moveItemToSlotsRequest(MoveItemToSlotsRequest request, EntityRef entity) {
        try {
            InventoryUtils.moveItemToSlots(request.getInstigator(), request.getFromInventory(), request.getFromSlot(),
                    request.getToInventory(), request.getToSlots());

        } finally {
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }

    @Override
    public boolean canStackTogether(EntityRef itemA, EntityRef itemB) {
        return InventoryUtils.canStackInto(itemA, itemB);
    }

    @Override
    public int getStackSize(EntityRef item) {
        return InventoryUtils.getStackCount(item);
    }

    @Override
    public EntityRef getItemInSlot(EntityRef inventoryEntity, int slot) {
        return InventoryUtils.getItemAt(inventoryEntity, slot);
    }

    @Override
    public int findSlotWithItem(EntityRef inventoryEntity, EntityRef item) {
        return InventoryUtils.getSlotWithItem(inventoryEntity, item);
    }

    @Override
    public int getNumSlots(EntityRef inventoryEntity) {
        return InventoryUtils.getSlotCount(inventoryEntity);
    }

    @Override
    public boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item) {
        return giveItem(inventory, instigator, item, null);
    }

    @Override
    public boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item, int slot) {
        return giveItem(inventory, instigator, item, Arrays.asList(slot));
    }

    @Override
    public boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item, List<Integer> slots) {
        ItemComponent itemToGive = item.getComponent(ItemComponent.class);
        if (itemToGive == null) {
            return true;
        }

        List<Integer> fillSlots = slots;
        if (fillSlots == null) {
            int slotCount = InventoryUtils.getSlotCount(inventory);
            fillSlots = Lists.newArrayList();
            for (int slot = 0; slot < slotCount; slot++) {
                fillSlots.add(slot);
            }
        }
        return giveItemToSlots(instigator, inventory, item, fillSlots);
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved) {
        return removeItemInternal(inventory, instigator, Arrays.asList(item), destroyRemoved, null);
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved, int count) {
        return removeItemInternal(inventory, instigator, Arrays.asList(item), destroyRemoved, count);
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved) {
        return removeItemInternal(inventory, instigator, items, destroyRemoved, null);
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved, int count) {
        return removeItemInternal(inventory, instigator, items, destroyRemoved, count);
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, int slotNo, boolean destroyRemoved, int count) {
        EntityRef item = InventoryUtils.getItemAt(inventory, slotNo);
        if (InventoryUtils.getStackCount(item) < count) {
            return null;
        }
        return removeItemFromSlots(instigator, destroyRemoved, inventory, Collections.singletonList(slotNo), count);
    }

    private EntityRef removeItemInternal(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved, Integer count) {
        final EntityRef firstItem = items.get(0);
        for (EntityRef item : items) {
            if (item != firstItem && !InventoryUtils.isSameItem(firstItem, item)) {
                return null;
            }
        }

        for (EntityRef item : items) {
            ItemComponent itemToRemove = item.getComponent(ItemComponent.class);
            if (itemToRemove == null) {
                return EntityRef.NULL;
            }
        }

        List<Integer> slotsWithItem = new LinkedList<>();
        for (EntityRef item : items) {
            int slotWithItem = InventoryUtils.getSlotWithItem(inventory, item);
            if (slotWithItem == -1) {
                return null;
            }
            slotsWithItem.add(slotWithItem);
        }

        Integer toRemove = count;
        if (toRemove == null) {
            toRemove = 0;
            for (EntityRef item : items) {
                toRemove += InventoryUtils.getStackCount(item);
            }
        }

        return removeItemFromSlots(instigator, destroyRemoved, inventory, slotsWithItem, toRemove);
    }

    @Override
    public boolean moveItem(EntityRef fromInventory, EntityRef instigator, int slotFrom, EntityRef toInventory, int slotTo, int count) {
        return InventoryUtils.moveItemAmount(instigator, fromInventory, slotFrom, toInventory, slotTo, count);
    }

    @Override
    public boolean moveItemToSlots(EntityRef instigator, EntityRef fromInventory, int slotFrom, EntityRef toInventory, List<Integer> toSlots) {
        return InventoryUtils.moveItemToSlots(instigator, fromInventory, slotFrom, toInventory, toSlots);
    }

    @Override
    public boolean switchItem(EntityRef fromInventory, EntityRef instigator, int slotFrom, EntityRef toInventory, int slotTo) {
        return InventoryUtils.moveItem(instigator, fromInventory, slotFrom, toInventory, slotTo);
    }
}
