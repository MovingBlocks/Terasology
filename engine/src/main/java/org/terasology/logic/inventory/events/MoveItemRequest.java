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

/**
 */
@ServerEvent
public class MoveItemRequest extends AbstractMoveItemRequest {
    private int toSlot;

    protected MoveItemRequest() {
    }

    public MoveItemRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory,
            int toSlot, int changeId, Collection<EntityRef> clientSideTempEntities) {
        super(instigator, fromInventory, fromSlot, toInventory, changeId, clientSideTempEntities);
        this.toSlot = toSlot;
    }

    public int getToSlot() {
        return toSlot;
    }

}
