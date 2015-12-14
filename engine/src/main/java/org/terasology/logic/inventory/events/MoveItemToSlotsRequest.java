/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.ServerEvent;

import java.util.Collection;
import java.util.List;

/**
 * Represents the request to move the item smarly to to one or more of the specified slots.
 * Stacks will be filled up first before an empty slot will be used.
 *
 * Usually triggered by a shift click on an item.
 *
 */
@ServerEvent
public class MoveItemToSlotsRequest extends AbstractMoveItemRequest {

    private List<Integer> toSlots;

    protected MoveItemToSlotsRequest() {
    }

    public MoveItemToSlotsRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory,
            List<Integer> toSlots, int changeId, Collection<EntityRef> clientSideTempEntities) {
        super(instigator, fromInventory, fromSlot, toInventory, changeId, clientSideTempEntities);
        this.toSlots = toSlots;
    }

    public List<Integer> getToSlots() {
        return toSlots;
    }
}
