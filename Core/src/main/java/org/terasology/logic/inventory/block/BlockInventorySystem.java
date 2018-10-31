/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.inventory.block;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

/**
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockInventorySystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;

    @ReceiveEvent(components = {InventoryComponent.class, RetainBlockInventoryComponent.class})
    public void copyBlockInventory(OnBlockToItem event, EntityRef blockEntity) {
        EntityRef inventoryItem = event.getItem();
        int slotCount = InventoryUtils.getSlotCount(blockEntity);
        inventoryItem.addComponent(new InventoryComponent(slotCount));
        for (int i = 0; i < slotCount; i++) {
            inventoryManager.switchItem(blockEntity, blockEntity, i, inventoryItem, i);
        }
        ItemComponent itemComponent = inventoryItem.getComponent(ItemComponent.class);
        if (itemComponent != null && !itemComponent.stackId.isEmpty()) {
            itemComponent.stackId = "";
            inventoryItem.saveComponent(itemComponent);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class, BlockItemComponent.class})
    public void onPlaced(OnBlockItemPlaced event, EntityRef itemEntity) {
        int slotCount = InventoryUtils.getSlotCount(itemEntity);
        for (int i = 0; i < slotCount; i++) {
            inventoryManager.switchItem(event.getPlacedBlock(), itemEntity, i, itemEntity, i);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class, DropBlockInventoryComponent.class, LocationComponent.class})
    public void dropContentsOfInventory(DoDestroyEvent event, EntityRef entity) {
        Vector3f position = entity.getComponent(LocationComponent.class).getWorldPosition();

        FastRandom random = new FastRandom();
        int slotCount = InventoryUtils.getSlotCount(entity);
        for (int i = 0; i < slotCount; i++) {
            EntityRef itemInSlot = InventoryUtils.getItemAt(entity, i);
            if (itemInSlot.exists()) {
                inventoryManager.removeItem(entity, entity, itemInSlot, false);
                itemInSlot.send(new DropItemEvent(position));
                itemInSlot.send(new ImpulseEvent(random.nextVector3f(30.0f)));
            }
        }
    }
}
