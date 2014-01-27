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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.registry.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;
import org.terasology.world.block.items.OnBlockToItem;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockInventorySystem extends BaseComponentSystem {

    @In
    private SlotBasedInventoryManager inventoryManager;

    @ReceiveEvent(components = {InventoryComponent.class, RetainBlockInventoryComponent.class})
    public void copyBlockInventory(OnBlockToItem event, EntityRef blockEntity) {
        EntityRef inventoryItem = event.getItem();
        inventoryItem.addComponent(new InventoryComponent(inventoryManager.getNumSlots(blockEntity)));
        inventoryManager.moveAll(blockEntity, inventoryItem);
        ItemComponent itemComponent = inventoryItem.getComponent(ItemComponent.class);
        if (itemComponent != null && !itemComponent.stackId.isEmpty()) {
            itemComponent.stackId = "";
            inventoryItem.saveComponent(itemComponent);
        }
    }

    @ReceiveEvent(components = {InventoryComponent.class, BlockItemComponent.class})
    public void onPlaced(OnBlockItemPlaced event, EntityRef itemEntity) {
        inventoryManager.moveAll(itemEntity, event.getPlacedBlock());
    }
}
