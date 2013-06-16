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

package org.terasology.logic.inventory;

import org.terasology.asset.Assets;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.physics.CollideEvent;


@RegisterSystem(RegisterMode.AUTHORITY)
public class ItemPickupSystem implements ComponentSystem {

    @In
    private InventoryManager inventoryManager;

    @Override
    public void initialise() {
    }

    @ReceiveEvent(components = PickupComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        PickupComponent pickupComponent = entity.getComponent(PickupComponent.class);
        if (inventoryManager.giveItem(event.getOtherEntity(), pickupComponent.itemEntity)) {
            pickupComponent.itemEntity = EntityRef.NULL;
            entity.saveComponent(pickupComponent);
            entity.destroy();
            event.getOtherEntity().send(new PlaySoundForOwnerEvent(Assets.getSound("engine:Loot"), 1.0f));
        }
    }

    @Override
    public void shutdown() {
    }
}
