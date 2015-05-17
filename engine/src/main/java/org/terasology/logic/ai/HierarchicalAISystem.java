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
package org.terasology.logic.ai;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;

/**
 * Hierarchical AI, idea from robotics
 *
 * @author Esa-Petri Tirkkonen
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HierarchicalAISystem extends BaseComponentSystem implements
        UpdateSubscriberSystem {

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    private Random random = new FastRandom();
    @In
    private Time time;
    private boolean idling;

    // TODO add way to recognize if attacked

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(
                HierarchicalAIComponent.class, CharacterMovementComponent.class,
                LocationComponent.class)) {
            LocationComponent location = entity
                    .getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            // Skip this AI if not in a loaded chunk
            if (!worldProvider.isBlockRelevant(worldPos)) {
                continue;
            }

            // goto Hierarchical system
            loop(entity, location, worldPos);
        }
    }

    /**
     * main loop of hierarchical system
     *
     * @param entity
     * @param location
     * @param worldPos
     */
    private void loop(EntityRef entity, LocationComponent location,
                      Vector3f worldPos) {
        HierarchicalAIComponent ai = entity
                .getComponent(HierarchicalAIComponent.class);
        long tempTime = CoreRegistry.get(Time.class).getGameTimeInMs();
        //TODO remove next
        long lastAttack = 0;

        // skip update if set to skip them
        if (!ai.needsUpdate(tempTime)) {
        	ai.setLastProgressedUpdated(CoreRegistry.get(Time.class)
                    .getGameTimeInMs());
            return;
        }

        long directionChangeTime = ai.getMoveUpdateTime();
        long moveChangeTime = ai.getMoveUpdateTime();
        long idleChangeTime = ai.getIdlingUpdateTime();
        long dangerChangeTime = ai.getDangerUpdateTime();

        // get movement
        Vector3f drive = new Vector3f();

        // find player position
        // TODO: shouldn't use local player, need some way to find nearest
        // player
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if (localPlayer != null) {
            Vector3f dist = new Vector3f(worldPos);
            dist.sub(localPlayer.getPosition());
            double distanceToPlayer = dist.lengthSquared();

            ai.setSafe();
            if (ai.shouldDieForDistance(distanceToPlayer)) {
                entity.destroy();
            }

            //----------------danger behavior----------

            // if our AI is aggressive or hunter go and hunt player else run away
            // if wild
            if (ai.willAtack(distanceToPlayer, tempTime, lastAttack)) {
                localPlayer.getCharacterEntity().send(
                		new DoDamageEvent(ai.getDamage(), EngineDamageTypes.PHYSICAL.get(), entity));
                lastAttack = CoreRegistry.get(Time.class).getGameTimeInMs();
            }

            //update
            if (ai.shouldHurt(tempTime, dangerChangeTime)) {
                dangerChangeTime = ai.getNewDangerChangeTime(random); 
                float forgiving = ai.forgiving();
                if (ai.sensePlayer(distanceToPlayer)) {
            		// Head to player
                    Vector3f tempTarget = localPlayer.getPosition();
                    if (forgiving != 0) {
                        ai.getMovementTarget().set(new Vector3f(
                                tempTarget.x + random.nextFloat(-forgiving, forgiving),
                                tempTarget.y + random.nextFloat(-forgiving, forgiving),
                                tempTarget.z + random.nextFloat(-forgiving, forgiving)
                        ));
                    } else {
                        ai.getMovementTarget().set(tempTarget);
                    }
                    ai.setInDanger();
                    entity.saveComponent(ai);

                    // System.out.print("\nhunting palyer\n");
                }
                
                // run opposite direction
                if (ai.isInPanic(distanceToPlayer)) {
                        Vector3f tempTarget = localPlayer.getPosition();
                        if (forgiving != 0) {
                            ai.getMovementTarget().set(new Vector3f(
                                    -tempTarget.x + random.nextFloat(-forgiving, forgiving),
                                    -tempTarget.y + random.nextFloat(-forgiving, forgiving),
                                    -tempTarget.z + random.nextFloat(-forgiving, forgiving)
                            ));
                        } else {
                            ai.getMovementTarget()
                                    .set(new Vector3f(tempTarget.x * -1,
                                            tempTarget.y * -1, tempTarget.z
                                            * -1));
                        }
                        entity.saveComponent(ai);
                        ai.setInDanger();
                    
                }
                ai.setLastChangeOfDanger(CoreRegistry.get(Time.class).getGameTimeInMs());
            }
        }

        if (!ai.isInDanger()) {

            //----------------eat----------
            // if anything edible is in front
            if (foodInFront()) {
                return;
            }

            //----------------idle----------
            // Idling part
            // what AI does when nothing better to do
            if (idling) {
                // time to stop idling
                if (ai.shouldIdle(tempTime, idleChangeTime)) {
                    idleChangeTime = ai.getNewIdilingChangeTime(random);
                    idling = false;
                    // mark idling state changed
                    ai.setLastChangeOfIdiling(CoreRegistry.get(Time.class)
                            .getGameTimeInMs());
                }
                entity.saveComponent(location);
                ai.setLastProgressedUpdated(CoreRegistry.get(Time.class)
                        .getGameTimeInMs());
                return;

            }

            // check if it is time to idle again
            if (ai.shouldMove(tempTime, moveChangeTime)) {
                // update time
                moveChangeTime = ai.getNewMoveChangeTime(random);
                idling = true;
                entity.saveComponent(location);

                // mark start idling
                ai.setLastChangeOfMovement(CoreRegistry.get(Time.class)
                        .getGameTimeInMs());
                ai.setLastProgressedUpdated(CoreRegistry.get(Time.class)
                        .getGameTimeInMs());
                return;
            }

            // Random walk
            // check if time to change direction
            if (ai.shouldChangeDirection(tempTime, directionChangeTime)) {
                directionChangeTime = ai.getNewDirectionChangeTime(random);
                // if ai flies
                if (ai.isFlying()) {
                    float targetY = 0;
                    do {
                        targetY = worldPos.y + random.nextFloat(-100.0f, 100.0f);
                    } while (targetY > ai.getMaxAltitude());
                    ai.getMovementTarget().set(
                            worldPos.x + random.nextFloat(-500.0f, 500.0f),
                            targetY,
                            worldPos.z + random.nextFloat(-500.0f, 500.0f));
                } else {
                    ai.getMovementTarget().set(
                            worldPos.x + random.nextFloat(-500.0f, 500.0f),
                            worldPos.y,
                            worldPos.z + random.nextFloat(-500.0f, 500.0f));
                }
                ai.setLastChangeOfDirection(time.getGameTimeInMs());
                entity.saveComponent(ai);
                // System.out.print("direction changed\n");

            }
        }

        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(ai.getMovementTarget(), worldPos);
        targetDirection.normalize();
        drive.set(targetDirection);

        float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
        entity.send(new CharacterMoveInputEvent(0, 0, yaw, drive, false, false));
        entity.saveComponent(location);
        // System.out.print("\Destination set: " + targetDirection.x + ":" +targetDirection.z + "\n");
        // System.out.print("\nI am: " + worldPos.x + ":" + worldPos.z + "\n");

        ai.setLastProgressedUpdated(CoreRegistry.get(Time.class)
                .getGameTimeInMs());
    }

    private boolean foodInFront() {
        return false;
        // return true;
    }

    //TODO change eating thingy to use this
    @ReceiveEvent(components = {HierarchicalAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity
                .getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.grounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }

}
