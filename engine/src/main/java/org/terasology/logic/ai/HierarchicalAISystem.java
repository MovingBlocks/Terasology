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

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
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
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * Hierarchical AI, idea from robotics
 *
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HierarchicalAISystem implements ComponentSystem,
        UpdateSubscriberSystem {

    private WorldProvider worldProvider;
    private EntityManager entityManager;
    private FastRandom random = new FastRandom();
    private Time time;
    private boolean idling;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        time = CoreRegistry.get(Time.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        idling = false;
    }

    @Override
    public void shutdown() {
    }

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
        if (tempTime - ai.lastProgressedUpdateAt < ai.updateFrequency) {
            ai.lastProgressedUpdateAt = CoreRegistry.get(Time.class)
                    .getGameTimeInMs();
            return;
        }

        long directionChangeTime = ai.moveUpdateTime;
        long moveChangeTime = ai.moveUpdateTime;
        long idleChangeTime = ai.idlingUpdateTime;
        long dangerChangeTime = ai.dangerUpdateTime;

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

            ai.inDanger = false;
            if (ai.dieIfPlayerFar && distanceToPlayer > ai.dieDistance) {
                entity.destroy();
            }

            //----------------danger behavior----------

            // if our AI is aggressive or hunter go and hunt player else run away
            // if wild
            if (ai.aggressive) {
                // TODO fix this to proper attacking
                if (distanceToPlayer <= ai.attackDistance) {
                    if (tempTime - lastAttack > ai.damageFrequency) {
                        localPlayer.getCharacterEntity().send(
                                new DoDamageEvent(ai.damage, EngineDamageTypes.PHYSICAL.get(), entity));
                        lastAttack = CoreRegistry.get(Time.class).getGameTimeInMs();
                    }
                }
            }

            //update
            if (tempTime - ai.lastChangeOfDangerAt > dangerChangeTime) {
                dangerChangeTime = (long) (TeraMath.fastAbs(ai.dangerUpdateTime
                        * random.randomDouble() * ai.hectic));
                if (ai.hunter) {
                    if (distanceToPlayer > ai.playerdistance
                            && distanceToPlayer < ai.playerSense) {
                        // Head to player
                        Vector3f tempTarget = localPlayer.getPosition();
                        if (ai.forgiving != 0) {
                            ai.movementTarget.set(new Vector3f(
                                    (tempTarget.x + random.randomFloat()
                                            * ai.forgiving),
                                    (tempTarget.y + random.randomFloat()
                                            * ai.forgiving),
                                    (tempTarget.z + random.randomFloat()
                                            * ai.forgiving)));
                        } else {
                            ai.movementTarget.set(tempTarget);
                        }
                        ai.inDanger = true;
                        entity.saveComponent(ai);

                        // System.out.print("\nhunting palyer\n");
                    }
                }
                // run opposite direction
                if (ai.wild) {
                    if (distanceToPlayer > ai.panicDistance
                            && distanceToPlayer < ai.runDistance) {
                        Vector3f tempTarget = localPlayer.getPosition();
                        if (ai.forgiving != 0) {
                            ai.movementTarget.set(new Vector3f(
                                    (tempTarget.x * -1 + random.randomFloat()
                                            * ai.forgiving),
                                    (tempTarget.y * -1 + random.randomFloat()
                                            * ai.forgiving),
                                    (tempTarget.z * -1 + random.randomFloat()
                                            * ai.forgiving)));
                        } else {
                            ai.movementTarget
                                    .set(new Vector3f(tempTarget.x * -1,
                                            tempTarget.y * -1, tempTarget.z
                                            * -1));
                        }
                        entity.saveComponent(ai);
                        ai.inDanger = true;
                    }
                }
                ai.lastChangeOfDangerAt = CoreRegistry.get(Time.class)
                        .getGameTimeInMs();
            }
        }

        if (!ai.inDanger) {

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
                if (tempTime - ai.lastChangeOfidlingtAt > idleChangeTime) {
                    idleChangeTime = (long) (TeraMath
                            .fastAbs(ai.idlingUpdateTime
                                    * random.randomDouble() * ai.hectic));
                    idling = false;
                    // mark idling state changed
                    ai.lastChangeOfidlingtAt = CoreRegistry.get(Time.class)
                            .getGameTimeInMs();
                }
                entity.saveComponent(location);
                ai.lastProgressedUpdateAt = CoreRegistry.get(Time.class)
                        .getGameTimeInMs();
                return;

            }

            // check if it is time to idle again
            if (tempTime - ai.lastChangeOfMovementAt > moveChangeTime) {
                // update time
                moveChangeTime = (long) (TeraMath.fastAbs(ai.moveUpdateTime
                        * random.randomDouble() * ai.hectic));
                idling = true;
                entity.saveComponent(location);

                // mark start idling
                ai.lastChangeOfMovementAt = CoreRegistry.get(Time.class)
                        .getGameTimeInMs();
                ai.lastProgressedUpdateAt = CoreRegistry.get(Time.class)
                        .getGameTimeInMs();
                return;
            }

            // Random walk
            // check if time to change direction
            if (tempTime - ai.lastChangeOfDirectionAt > directionChangeTime) {
                directionChangeTime = (long) (TeraMath
                        .fastAbs(ai.moveUpdateTime * random.randomDouble()
                                * ai.straightLined));
                // if ai flies
                if (ai.flying) {
                    float targetY = 0;
                    do {
                        targetY = worldPos.y + random.randomFloat() * 100;
                    } while (targetY > ai.maxAltitude);
                    ai.movementTarget.set(worldPos.x + random.randomFloat()
                            * 500, targetY, worldPos.z + random.randomFloat()
                            * 500);
                } else {
                    ai.movementTarget.set(worldPos.x + random.randomFloat()
                            * 500, worldPos.y,
                            worldPos.z + random.randomFloat() * 500);
                }
                ai.lastChangeOfDirectionAt = time.getGameTimeInMs();
                entity.saveComponent(ai);
                // System.out.print("direction changed\n");

            }
        }

        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(ai.movementTarget, worldPos);
        targetDirection.normalize();
        drive.set(targetDirection);

        float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
        entity.send(new CharacterMoveInputEvent(0, 0, yaw, drive, false, false));
        entity.saveComponent(location);
        // System.out.print("\Destination set: " + targetDirection.x + ":" +targetDirection.z + "\n");
        // System.out.print("\nI am: " + worldPos.x + ":" + worldPos.z + "\n");

        ai.lastProgressedUpdateAt = CoreRegistry.get(Time.class).getGameTimeInMs();
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
