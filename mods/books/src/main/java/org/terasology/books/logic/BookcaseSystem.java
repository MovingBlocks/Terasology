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

package org.terasology.books.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entityFactory.DroppedItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.NoHealthEvent;
import org.terasology.world.block.BlockItemComponent;

import javax.vecmath.Vector3f;

@RegisterComponentSystem
public class BookcaseSystem implements EventHandlerSystem {

    private final Logger logger = LoggerFactory.getLogger(BookcaseSystem.class);

    private DroppedBlockFactory droppedBlockFactory;
    private DroppedItemFactory droppedItemFactory;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
        droppedBlockFactory = new DroppedBlockFactory(entityManager);
        droppedItemFactory = new DroppedItemFactory(entityManager);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = BookcaseComponent.class)
    public void onOutOfHealth(NoHealthEvent event, EntityRef entity) {
        logger.info("Bookcase is out of health");

        InventoryComponent inventoryComponent = entity.getComponent(InventoryComponent.class);
        if (inventoryComponent != null) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            Vector3f origin = null;
            if (locationComponent != null) {
                origin = locationComponent.getWorldPosition();
            }
            if (origin == null) {
                return;
            }
            for (EntityRef e : inventoryComponent.itemSlots) {
                if (!e.equals(EntityRef.NULL)) {
                    BlockItemComponent block = e.getComponent(BlockItemComponent.class);
                    ItemComponent item = e.getComponent(ItemComponent.class);

                    if (!e.hasComponent(BlockItemComponent.class)) {
                        for (int i = 0; i < item.stackCount; i++) {
                            droppedItemFactory.newInstance(origin, item.icon, 200, e);
                        }
                    } else {
                        for (int i = 0; i < item.stackCount; i++) {
                            droppedBlockFactory.newInstance(origin, block.blockFamily, 20,
                                e.getComponent(BlockItemComponent.class).placedEntity);
                        }
                    }
                    e.destroy();
                }
            }
        }
    }
}
