/*
 * Copyright 2012
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

package org.terasology.world.block;

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.components.ItemComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.physics.CollideEvent;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class BlockPickupSystem implements EventHandlerSystem {

    private BlockItemFactory blockItemFactory;
    @In
    private AudioManager audioManager;

    @ReceiveEvent(components = BlockPickupComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        BlockPickupComponent blockPickupComponent = entity.getComponent(BlockPickupComponent.class);
        EntityRef blockItem = blockItemFactory.newInstance(blockPickupComponent.blockFamily, blockPickupComponent.placedEntity);
        ReceiveItemEvent giveItemEvent = new ReceiveItemEvent(blockItem);
        event.getOtherEntity().send(giveItemEvent);

        ItemComponent itemComp = blockItem.getComponent(ItemComponent.class);
        if (itemComp != null && !itemComp.container.exists()) {
            blockItem.destroy();
        } else {
            audioManager.playSound(Assets.getSound("engine:Loot"));
            entity.destroy();
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
