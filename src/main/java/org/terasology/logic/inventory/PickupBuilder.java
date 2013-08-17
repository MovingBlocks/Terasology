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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.inventory.events.ItemDroppedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.NetworkSystem;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
// TODO: This could be event driven (DropItemEvent)
public final class PickupBuilder {

    private EntityManager entityManager;
    private InventoryManager inventoryManager;
    private NetworkSystem networkSystem;


    public PickupBuilder() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        this.inventoryManager = CoreRegistry.get(InventoryManager.class);
        this.networkSystem = CoreRegistry.get(NetworkSystem.class);
    }

    public EntityRef createPickupFor(EntityRef itemEntity, Vector3f pos, int lifespan) {
        ItemComponent itemComp = itemEntity.getComponent(ItemComponent.class);
        if (itemComp == null || !itemComp.pickupPrefab.exists()) {
            return EntityRef.NULL;
        }

        EntityRef owner = itemEntity.getOwner();
        if (owner.hasComponent(InventoryComponent.class)) {
            itemEntity = inventoryManager.removeItem(owner, itemEntity, 1);
        }

        if (!itemEntity.exists()) {
            return EntityRef.NULL;
        }

        //don't perform actual drop on client side
        if (itemEntity.exists()) {
            EntityBuilder builder = entityManager.newBuilder(itemComp.pickupPrefab);
            if (builder.hasComponent(LocationComponent.class)) {
                builder.getComponent(LocationComponent.class).setWorldPosition(pos);
            }
            if (builder.hasComponent(LifespanComponent.class)) {
                builder.getComponent(LifespanComponent.class).lifespan = lifespan;
            }
            boolean destroyItem = false;
            if (builder.hasComponent(PickupComponent.class)) {
                builder.getComponent(PickupComponent.class).itemEntity = itemEntity;
            } else {
                destroyItem = true;
            }


            itemEntity.send(new ItemDroppedEvent(builder));
            if (destroyItem) {
                itemEntity.destroy();
            }
            return builder.build();
        }
        return EntityRef.NULL;
    }
}
