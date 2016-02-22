/*
 * Copyright 2015 MovingBlocks
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
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ServerEvent;

/**
 * A request for a player to drop an item. Is replicated onto the server
 *
 */
@ServerEvent(lagCompensate = true)
public class DropItemRequest implements Event {

    private EntityRef item = EntityRef.NULL;
    private EntityRef inventory = EntityRef.NULL;
    private Vector3f impulse;
    private Vector3f newPosition;

    protected DropItemRequest() {
    }

    public DropItemRequest(EntityRef usedItem, EntityRef inventoryEntity, Vector3f impulse, Vector3f newPosition) {
        this.item = usedItem;
        this.inventory = inventoryEntity;
        this.impulse = impulse;
        this.newPosition = newPosition;
    }

    public EntityRef getItem() {
        return item;
    }

    public EntityRef getInventoryEntity() {
        return inventory;
    }

    public Vector3f getNewPosition() {
        return newPosition;
    }

    public Vector3f getImpulse() {
        return impulse;
    }
}
