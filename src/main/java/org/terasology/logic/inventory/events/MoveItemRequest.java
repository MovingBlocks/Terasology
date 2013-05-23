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

package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.event.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
@ServerEvent
public class MoveItemRequest extends AbstractEvent {
    private EntityRef fromInventory = EntityRef.NULL;
    private int fromSlot;
    private EntityRef toInventory = EntityRef.NULL;
    private int toSlot;

    private int changeId;

    protected MoveItemRequest() {
    }

    public MoveItemRequest(EntityRef fromInventory, int fromSlot, EntityRef toInventory, int toSlot, int changeId) {
        this.fromInventory = fromInventory;
        this.fromSlot = fromSlot;
        this.toInventory = toInventory;
        this.toSlot = toSlot;
        this.changeId = changeId;
    }

    public EntityRef getFromInventory() {
        return fromInventory;
    }

    public int getFromSlot() {
        return fromSlot;
    }

    public EntityRef getToInventory() {
        return toInventory;
    }

    public int getToSlot() {
        return toSlot;
    }

    public int getChangeId() {
        return changeId;
    }
}
