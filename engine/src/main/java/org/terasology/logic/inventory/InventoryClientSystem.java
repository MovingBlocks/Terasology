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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class InventoryClientSystem extends BaseComponentSystem {
    @In
    private LocalPlayer localPlayer;
    @In
    private EntitySystemLibrary entitySystemLibrary;

    private Map<Integer, MoveItemRequest> pendingMoves = new LinkedHashMap<>();

    private int changeId;

    // We support only these two events on the client, as these are the events used by inventory UI

    @ReceiveEvent(components = {InventoryComponent.class})
    public void switchItemRequest(SwitchItemAction event, EntityRef entity) {
        if (!InventoryUtils.moveItem(event.getInstigator(), entity, event.getSlotFrom(), event.getTo(), event.getSlotTo())) {
            return;
        }

        MoveItemRequest request = new MoveItemRequest(event.getInstigator(), entity, event.getSlotFrom(), event.getTo(), event.getSlotTo(), changeId++);
        pendingMoves.put(request.getChangeId(), request);
        localPlayer.getClientEntity().send(request);
    }

    @ReceiveEvent(components = {InventoryComponent.class})
    public void moveItemRequest(MoveItemAction event, EntityRef entity) {
        if (!InventoryUtils.moveItemAmount(event.getInstigator(), entity, event.getSlotFrom(), event.getTo(), event.getSlotTo(), event.getCount())) {
            return;
        }

        MoveItemAmountRequest request = new MoveItemAmountRequest(event.getInstigator(), entity, event.getSlotFrom(), event.getTo(), event.getSlotTo(), event.getCount(), changeId++);
        pendingMoves.put(request.getChangeId(), request);
        localPlayer.getClientEntity().send(request);
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
                InventoryUtils.moveItemAmount(request.getInstigator(), request.getFromInventory(), request.getFromSlot(), request.getToInventory(), request.getToSlot(), amount);
            } else {
                InventoryUtils.moveItem(request.getInstigator(), request.getFromInventory(), request.getFromSlot(), request.getToInventory(), request.getToSlot());
            }
        }
    }
}
