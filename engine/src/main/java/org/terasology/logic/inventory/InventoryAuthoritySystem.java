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
import org.terasology.registry.In;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class InventoryAuthoritySystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = {InventoryComponent.class})
    public void switchItem(SwitchItemAction event, EntityRef entity) {
        InventoryUtils.moveItem(event.getInstigator(), entity, event.getSlotFrom(), event.getTo(), event.getSlotTo());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void moveItem(MoveItemAction event, EntityRef entity) {
        InventoryUtils.moveItemAmount(event.getInstigator(), entity, event.getSlotFrom(), event.getTo(), event.getSlotTo(), event.getCount());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void removeItem(RemoveItemAction event, EntityRef entity) {
        for (EntityRef item : event.getItems()) {
            ItemComponent itemToRemove = item.getComponent(ItemComponent.class);
            if (itemToRemove == null) {
                event.consume();
                return;
            }
        }

        List<Integer> slotsWithItem = new LinkedList<>();
        for (EntityRef item : event.getItems()) {
            int slotWithItem = InventoryUtils.getSlotWithItem(entity, item);
            if (slotWithItem == -1) {
                return;
            }
            slotsWithItem.add(slotWithItem);
        }

        Integer toRemove = event.getCount();
        if (toRemove == null) {
            toRemove = 0;
            for (EntityRef item : event.getItems()) {
                toRemove += InventoryUtils.getStackCount(item);
            }
        }

        if (removeItemFromSlots(event, entity, slotsWithItem, toRemove)) {
            event.consume();
        }
    }

    private boolean removeItemFromSlots(RemoveItemAction event, EntityRef entity, List<Integer> slotsWithItem, int toRemove) {
        int shrinkSlotNo = -1;
        int shrinkCountResult = 0;

        List<Integer> slotsTotallyConsumed = new LinkedList<>();

        for (int slot : slotsWithItem) {
            EntityRef itemAtEntity = InventoryUtils.getItemAt(entity, slot);
            ItemComponent itemAt = itemAtEntity.getComponent(ItemComponent.class);
            if (itemAt.stackCount <= toRemove) {
                if (canRemoveItemFromSlot(event.getInstigator(), entity, itemAtEntity, slot)) {
                    slotsTotallyConsumed.add(slot);
                    toRemove -= itemAt.stackCount;
                }
            } else {
                shrinkSlotNo = slot;
                shrinkCountResult = itemAt.stackCount - toRemove;
                toRemove = 0;
            }

            if (toRemove == 0) {
                break;
            }
        }

        if (toRemove > 0) {
            return false;
        }

        EntityRef removed = null;
        int removedCount = 0;
        for (int slot : slotsTotallyConsumed) {
            EntityRef itemAt = InventoryUtils.getItemAt(entity, slot);
            removedCount += InventoryUtils.getStackCount(itemAt);

            if (event.isDestroyRemoved()) {
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
            removedCount += InventoryUtils.getSlotCount(itemAt) - shrinkCountResult;
            if (event.isDestroyRemoved()) {
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
            item.stackCount = (byte) removedCount;
            removed.saveComponent(item);
            event.setRemovedItem(removed);
        }
        return true;
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void giveItem(GiveItemAction event, EntityRef entity) {
        EntityRef item = event.getItem();
        ItemComponent itemToGive = item.getComponent(ItemComponent.class);
        if (itemToGive == null) {
            event.consume();
            return;
        }

        List<Integer> slots = event.getSlots();
        if (slots == null) {
            int slotCount = InventoryUtils.getSlotCount(entity);
            slots = new LinkedList<>();
            for (int slot = 0; slot < slotCount; slot++) {
                slots.add(slot);
            }
        }
        if (giveItemToSlots(event.getInstigator(), entity, item, slots)) {
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
            if (!(request instanceof MoveItemAmountRequest)) {
                InventoryUtils.moveItem(request.getInstigator(), request.getFromInventory(), request.getFromSlot(),
                        request.getToInventory(), request.getToSlot());
            }
        } finally {
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }
}
