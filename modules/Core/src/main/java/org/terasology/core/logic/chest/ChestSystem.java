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
package org.terasology.core.logic.chest;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;

/**
 * @author Immortius
 */
@RegisterSystem
public class ChestSystem implements ComponentSystem {

    @In
    private SlotBasedInventoryManager inventoryManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {InventoryComponent.class, BlockItemComponent.class})
    public void onPlaced(OnBlockItemPlaced event, EntityRef itemEntity) {
        inventoryManager.moveAll(itemEntity, event.getPlacedBlock());
    }
}
