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
package org.terasology.componentSystem.characters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.DrowningComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.BreathMeterUpdateEvent;
import org.terasology.events.DrownTickEvent;
import org.terasology.events.FromLiquidEvent;
import org.terasology.events.IntoLiquidEvent;
import org.terasology.game.Timer;
import org.terasology.game.types.GameTypeManager;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import javax.vecmath.Vector3f;

/**
 * @author Nick "SleekoNiko" Caplinger <sleekoniko@gmail.com>
 */

@RegisterComponentSystem
public class DrowningSystem implements EventHandlerSystem, UpdateSubscriberSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private GameTypeManager gameTypeManager;

    @In
    private EntityManager entityManager;

    @In
    private Timer timer;

    private static final Logger logger = LoggerFactory.getLogger(DrowningSystem.class);

    @Override
    public void initialise(){

    }

    @Override
    public void shutdown(){

    }

    @Override
    public void update(float deltaTime){
        for (EntityRef entity : entityManager.iteratorEntities(DrowningComponent.class)) {
            DrowningComponent drownComp = entity.getComponent(DrowningComponent.class);

            // If the entity is underwater and has run out of breath
            if(drownComp.underWater
                    && (timer.getTimeInMs() - drownComp.timeEnteredLiquid) >= drownComp.timeBeforeDrown){

                // If the entity should take a drown damage tick
                if((timer.getTimeInMs() - drownComp.timeLastDrownTick) >= drownComp.timeBetweenDamageTicks){

                    // Try to damage the entity
                    HealthComponent healthComp = entity.getComponent(HealthComponent.class);
                    if(healthComp != null && healthComp.currentHealth > 0){

                        // Damage the entity
                        int damage = healthComp.maxHealth/10;
                        gameTypeManager.getActiveGameType().onPlayerDamageHook(entity, healthComp, damage, null);

                        // Update timing values
                        drownComp.timeLastDrownTick = timer.getTimeInMs();

                        // Send a DrownTickEvent
                        LocationComponent location = entity.getComponent(LocationComponent.class);
                        if(location != null){
                            Vector3f worldPos = location.getWorldPosition();

                            final Block block = worldProvider.getBlock(worldPos);
                            entity.send(new DrownTickEvent(block, worldPos));
                        }

                        // Save the components
                        entity.saveComponent(drownComp);
                    }

                    sendBreathMeterUpdate(entity);
                }
            }else{
                sendBreathMeterUpdate(entity);
            }
        }
    }

    private void sendBreathMeterUpdate(EntityRef entity){
        // Update the breath meter if it's a player
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if(localPlayerComp != null){
            entity.send(new BreathMeterUpdateEvent());
        }
    }

    @ReceiveEvent(components = {DrowningComponent.class})
    public void onIntoLiquid(IntoLiquidEvent event, EntityRef entity) {

        DrowningComponent drownComp = entity.getComponent(DrowningComponent.class);
        drownComp.timeEnteredLiquid = timer.getTimeInMs();
        drownComp.underWater = true;

        entity.saveComponent(drownComp);
    }

    @ReceiveEvent(components = {DrowningComponent.class})
    public void onFromLiquid(FromLiquidEvent event, EntityRef entity) {

        DrowningComponent drownComp = entity.getComponent(DrowningComponent.class);
        drownComp.underWater = false;

        // Somebody might find this useful for testing purposes
        logger.debug("You were in the liquid for "
                + (timer.getTimeInMs() - drownComp.timeEnteredLiquid)/1000
                + " seconds.");

        entity.saveComponent(drownComp);
    }
}
