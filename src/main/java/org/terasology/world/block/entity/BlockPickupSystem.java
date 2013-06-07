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

package org.terasology.world.block.entity;

import org.terasology.asset.Assets;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.physics.CollideEvent;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockPickupSystem implements ComponentSystem {

    @In
    private InventoryManager inventoryManager;

    private BlockItemFactory blockItemFactory;

    @ReceiveEvent(components = BlockPickupComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        BlockPickupComponent blockPickupComponent = entity.getComponent(BlockPickupComponent.class);
        EntityRef blockItem = blockItemFactory.newInstance(blockPickupComponent.blockFamily, blockPickupComponent.placedEntity);
        if (inventoryManager.giveItem(event.getOtherEntity(), blockItem)) {
            event.getOtherEntity().send(new PlaySoundForOwnerEvent(Assets.getSound("engine:Loot"), 1.0f));
            entity.destroy();
        } else {
            blockItem.destroy();
        }
    }


    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
    }

    @Override
    public void shutdown() {
    }
}
