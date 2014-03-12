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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.action.MoveItemAction;
import org.terasology.logic.inventory.action.SwitchItemAction;
import org.terasology.logic.inventory.events.InventoryChangeAcknowledgedRequest;
import org.terasology.logic.inventory.events.MoveItemAmountRequest;
import org.terasology.logic.inventory.events.MoveItemRequest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
@Share(value = {InventoryManager.class})
public class InventoryClientSystem extends BaseComponentSystem implements InventoryManager {
    @In
    private LocalPlayer localPlayer;
    @In
    private EntitySystemLibrary entitySystemLibrary;

    private Map<Integer, MoveItemRequest> pendingMoves = new LinkedHashMap<>();

    private int changeId;

    // We support only these two events on the client, as these are the events used by inventory UI

    @ReceiveEvent(components = {InventoryComponent.class})
    public void switchItemRequest(SwitchItemAction event, EntityRef entity) {
        switchItem(entity, event.getInstigator(), event.getSlotFrom(), event.getTo(), event.getSlotTo());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void moveItemRequest(MoveItemAction event, EntityRef entity) {
        moveItem(entity, event.getInstigator(), event.getSlotFrom(), event.getTo(), event.getSlotTo(), event.getCount());
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void inventoryChangeAcknowledge(InventoryChangeAcknowledgedRequest event, EntityRef entity) {
        pendingMoves.remove(event.getChangeId());
        recalculatePredictedState();
    }

    private void recalculatePredictedState() {
        for (MoveItemRequest request : pendingMoves.values()) {
            if (request instanceof MoveItemAmountRequest) {
                int amount = ((MoveItemAmountRequest) request).getAmount();
                InventoryUtils.moveItemAmount(request.getInstigator(), request.getFromInventory(),
                        request.getFromSlot(), request.getToInventory(), request.getToSlot(), amount);
            } else {
                InventoryUtils.moveItem(request.getInstigator(), request.getFromInventory(), request.getFromSlot(), request.getToInventory(), request.getToSlot());
            }
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
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item, int slot) {
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public boolean giveItem(EntityRef inventory, EntityRef instigator, EntityRef item, List<Integer> slots) {
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved) {
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, EntityRef item, boolean destroyRemoved, int count) {
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved) {
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public EntityRef removeItem(EntityRef inventory, EntityRef instigator, List<EntityRef> items, boolean destroyRemoved, int count) {
        throw new UnsupportedOperationException("This operation cannot be invoked on the client");
    }

    @Override
    public void moveItem(EntityRef fromInventory, EntityRef instigator, int slotFrom, EntityRef toInventory, int slotTo, int count) {
        if (!InventoryUtils.moveItemAmount(instigator, fromInventory, slotFrom, toInventory, slotTo, count)) {
            return;
        }

        MoveItemAmountRequest request = new MoveItemAmountRequest(instigator, fromInventory,
                slotFrom, toInventory, slotTo, count, changeId++);
        pendingMoves.put(request.getChangeId(), request);
        localPlayer.getClientEntity().send(request);
    }

    @Override
    public void switchItem(EntityRef fromInventory, EntityRef instigator, int slotFrom, EntityRef toInventory, int slotTo) {
        if (!InventoryUtils.moveItem(instigator, fromInventory, slotFrom, toInventory, slotTo)) {
            return;
        }

        MoveItemRequest request = new MoveItemRequest(instigator, fromInventory, slotFrom, toInventory, slotTo, changeId++);
        pendingMoves.put(request.getChangeId(), request);
        localPlayer.getClientEntity().send(request);
    }
}
