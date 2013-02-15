/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.potions;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;


/**
 * This Handles Potion Consumption
 * Applies effect and returns an empty vial.
 *
 * @author bi0hax
 */
@RegisterComponentSystem
public class DrinkPotionAction implements EventHandlerSystem {

    @Override
    public void initialise() {
    }


    @Override
    public void shutdown() {
    }

    public EntityRef entity;
    public PotionComponent potion;
    public PoisonedComponent poisoned;
    public EntityRef item;
    public CoreRegistry CoreRegister;

    @In
    private AudioManager audioManager;


    @ReceiveEvent(components = {PotionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        potion = entity.getComponent(PotionComponent.class);
        poisoned = entity.getComponent(PoisonedComponent.class);
        EntityManager entityManager = CoreRegister.get(EntityManager.class);

        HealthComponent health = event.getTarget().getComponent(HealthComponent.class);
        ItemComponent itemComp = entity.getComponent(ItemComponent.class);


        EntityRef item = entityManager.create("potions:emptyVial");

        switch (potion.type) {
            case Red:
                //Max HP
                event.getInstigator().send(new BoostHpEvent());
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }


                break;

            case Green:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Poison time!
                event.getInstigator().send(new PoisonedEvent());
                break;

            case Orange: //Cures the Poison.
                event.getInstigator().send(new CurePoisonEvent());
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                break;

            case Purple:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Speed time!
                event.getInstigator().send(new BoostSpeedEvent());
                break;

            default:
                break;
        }
        audioManager.playSound(Assets.getSound("engine:drink"), 1.0f);


    }
}
