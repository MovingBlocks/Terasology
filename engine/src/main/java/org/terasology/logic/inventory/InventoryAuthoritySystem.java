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

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class InventoryAuthoritySystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = {InventoryComponent.class})
    public void switchItem(SwitchItemAction event, EntityRef entity) {
        InventoryUtils.moveItem(entity, event.getSlotFrom(), event.getTo(), event.getSlotTo());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void moveItem(MoveItemAction event, EntityRef entity) {
        InventoryUtils.moveItemAmount(entity, event.getSlotFrom(), event.getTo(), event.getSlotTo(), event.getCount());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void removeItem(RemoveItemAction event, EntityRef entity) {
        ItemComponent itemToRemove = event.getItem().getComponent(ItemComponent.class);
        if (itemToRemove == null) {
            event.consume();
            return;
        }

        int slotWithItem = InventoryUtils.getSlotWithItem(entity, event.getItem());
        if (slotWithItem == -1) {
            return;
        }

        Integer count = event.getCount();
        if (count != null) {
            if (count > itemToRemove.stackCount) {
                return;
            }
        }

        BeforeItemRemovedFromInventory removeFrom = new BeforeItemRemovedFromInventory(event.getItem(), slotWithItem);
        entity.send(removeFrom);
        if (removeFrom.isConsumed()) {
            return;
        }

        if (count == null || count == itemToRemove.stackCount) {
            InventoryUtils.putItemIntoSlot(entity, EntityRef.NULL, slotWithItem);

            if (event.isDestroyRemoved()) {
                event.getItem().destroy();
            } else {
                event.setRemovedItem(event.getItem());
            }
        } else {
            if (!event.isDestroyRemoved()) {
                EntityRef copy = entityManager.copy(event.getItem());
                ItemComponent copyItem = copy.getComponent(ItemComponent.class);
                copyItem.stackCount = count.byteValue();
                copy.saveComponent(copyItem);

                event.setRemovedItem(copy);
            }

            InventoryUtils.adjustStackSize(entity, slotWithItem, itemToRemove.stackCount - count);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void giveItem(GiveItemAction event, EntityRef entity) {
        ItemComponent itemToGive = event.getItem().getComponent(ItemComponent.class);
        if (itemToGive == null) {
            event.consume();
            return;
        }

        Integer slot = event.getSlot();
        if (slot != null) {
            giveItemToSlot(event, entity, itemToGive, slot);
            return;
        }

        int slotCount = InventoryUtils.getSlotCount(entity);
        for (int i = 0; i < slotCount; i++) {
            if (giveItemToSlot(event, entity, itemToGive, i)) {
                return;
            }
        }
    }

    private boolean giveItemToSlot(GiveItemAction event, EntityRef entity, ItemComponent itemToGive, int slot) {
        EntityRef itemAtEntity = InventoryUtils.getItemAt(entity, slot);
        ItemComponent itemAt = itemAtEntity.getComponent(ItemComponent.class);
        if (itemAt == null || InventoryUtils.isSameItem(itemAt, itemToGive)) {
            if (itemAt == null) {
                if (canPutItemIntoSlot(event.isForce(), entity, event.getItem(), slot)) {
                    InventoryUtils.putItemIntoSlot(entity, event.getItem(), slot);
                    event.consume();
                    return true;
                }
            } else {
                if (itemAt.stackCount + itemToGive.stackCount <= itemToGive.maxStackSize) {
                    InventoryUtils.adjustStackSize(entity, slot, itemAt.stackCount + itemToGive.stackCount);

                    event.getItem().destroy();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canPutItemIntoSlot(boolean force, EntityRef entity, EntityRef item, int slot) {
        if (force || !item.exists()) {
            return true;
        }
        BeforeItemPutInInventory itemPut = new BeforeItemPutInInventory(item, slot);
        entity.send(itemPut);
        return !itemPut.isConsumed();
    }

    @ReceiveEvent
    public void moveItemAmountRequest(MoveItemAmountRequest request, EntityRef entity) {
        try {
            InventoryUtils.moveItemAmount(request.getFromInventory(), request.getFromSlot(),
                    request.getToInventory(), request.getToSlot(), request.getAmount());
        } finally {
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }

    @ReceiveEvent
    public void moveItemRequest(MoveItemRequest request, EntityRef entity) {
        try {
            if (!(request instanceof MoveItemAmountRequest)) {
                InventoryUtils.moveItem(request.getFromInventory(), request.getFromSlot(),
                        request.getToInventory(), request.getToSlot());
            }
        } finally {
            entity.send(new InventoryChangeAcknowledgedRequest(request.getChangeId()));
        }
    }
}
