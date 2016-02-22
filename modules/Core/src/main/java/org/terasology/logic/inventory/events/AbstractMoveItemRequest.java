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
import org.terasology.entitySystem.event.Event;
import org.terasology.network.NoReplicate;
import org.terasology.network.ServerEvent;

import java.util.Collection;

/**
 */
@ServerEvent
public abstract class AbstractMoveItemRequest implements Event {
    private EntityRef instigator;

    private EntityRef fromInventory = EntityRef.NULL;
    private int fromSlot;
    private EntityRef toInventory = EntityRef.NULL;

    private int changeId;

    @NoReplicate
    private Collection<EntityRef> clientSideTempEntities;

    protected AbstractMoveItemRequest() {
    }

    public AbstractMoveItemRequest(EntityRef instigator, EntityRef fromInventory, int fromSlot, EntityRef toInventory, int changeId,
                                   Collection<EntityRef> clientSideTempEntities) {
        this.instigator = instigator;
        this.fromInventory = fromInventory;
        this.fromSlot = fromSlot;
        this.toInventory = toInventory;
        this.changeId = changeId;
        this.clientSideTempEntities = clientSideTempEntities;
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

    public int getChangeId() {
        return changeId;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public Collection<EntityRef> getClientSideTempEntities() {
        return clientSideTempEntities;
    }

    public void setClientSideTempEntities(Collection<EntityRef> clientSideTempEntities) {
        this.clientSideTempEntities = clientSideTempEntities;
    }
}
