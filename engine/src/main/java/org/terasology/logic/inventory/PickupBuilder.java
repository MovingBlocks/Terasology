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
package org.terasology.logic.inventory;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.logic.inventory.events.ItemDroppedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.CoreRegistry;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
// TODO: This could be event driven (DropItemEvent)
public final class PickupBuilder {

    private EntityManager entityManager;
    private InventoryManager inventoryManager;


    public PickupBuilder() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        this.inventoryManager = CoreRegistry.get(InventoryManager.class);
    }

    public EntityRef createPickupFor(EntityRef itemEntity, Vector3f pos, int lifespan) {
        return createPickupFor(itemEntity, pos, lifespan, false);
    }

    public EntityRef createPickupFor(EntityRef itemEntity, Vector3f pos, int lifespan, boolean dropAll) {
        ItemComponent itemComp = itemEntity.getComponent(ItemComponent.class);
        if (itemComp == null || !itemComp.pickupPrefab.exists()) {
            return EntityRef.NULL;
        }

        EntityRef pickupItem = itemEntity;
        EntityRef owner = itemEntity.getOwner();
        if (owner.hasComponent(InventoryComponent.class)) {
            if (dropAll) {
                RemoveItemAction action = new RemoveItemAction(pickupItem, false);
                owner.send(action);
                pickupItem = action.getRemovedItem();
            } else {
                RemoveItemAction action = new RemoveItemAction(pickupItem, false, 1);
                owner.send(action);
                pickupItem = action.getRemovedItem();
            }
        }

        //don't perform actual drop on client side
        if (pickupItem.exists()) {
            EntityBuilder builder = entityManager.newBuilder(itemComp.pickupPrefab);
            if (builder.hasComponent(LocationComponent.class)) {
                builder.getComponent(LocationComponent.class).setWorldPosition(pos);
            }
            if (builder.hasComponent(LifespanComponent.class)) {
                builder.getComponent(LifespanComponent.class).lifespan = lifespan;
            }
            boolean destroyItem = false;
            if (builder.hasComponent(PickupComponent.class)) {
                builder.getComponent(PickupComponent.class).itemEntity = pickupItem;
            } else {
                destroyItem = true;
            }

            pickupItem.send(new ItemDroppedEvent(builder));
            if (destroyItem) {
                pickupItem.destroy();
            }
            return builder.build();
        }
        return EntityRef.NULL;
    }
}
