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

package org.terasology.logic.characters.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * A request for a player to drop an item. Is replicated onto the server
 *
 * @author Sdab
 */
@ServerEvent(lagCompensate = true)
public class DropItemRequest extends AbstractEvent {

    private EntityRef item = EntityRef.NULL, inventory = EntityRef.NULL;
    private Vector3f impulse, newPosition;

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
