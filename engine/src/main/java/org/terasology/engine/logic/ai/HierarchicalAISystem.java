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

import org.joml.Vector3f;
import org.joml.Vector3fc;
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
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;

/**
 * Hierarchical AI, idea from robotics
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HierarchicalAISystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    private Random random = new FastRandom();
    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    private boolean idling;

    // TODO add way to recognize if attacked

    @Override
    public void update(float delta) {
        Vector3f tmp = new Vector3f();
        for (EntityRef entity : entityManager.getEntitiesWith(
                HierarchicalAIComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            location.getWorldPosition(tmp);

            // Skip this AI if not in a loaded chunk
            if (!worldProvider.isBlockRelevant(tmp)) {
                continue;
            }

            // goto Hierarchical system
            loop(entity, location, tmp);
        }
    }

    /**
     * main loop of hierarchical system
     *
     * @param entity
     * @param location
     * @param worldPos
     */
    private void loop(EntityRef entity, LocationComponent location, Vector3fc worldPos) {
        HierarchicalAIComponent ai = entity.getComponent(HierarchicalAIComponent.class);
        long tempTime = time.getGameTimeInMs();
        //TODO remove next
        long lastAttack = 0;

        // skip update if set to skip them
        if (tempTime - ai.lastProgressedUpdateAt < ai.updateFrequency) {
            ai.lastProgressedUpdateAt = time
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
        if (localPlayer != null) {
            final Vector3f playerPosition = localPlayer.getPosition(new Vector3f());
            double distanceToPlayer = worldPos.distanceSquared(playerPosition);

            ai.inDanger = false;
            if (ai.dieIfPlayerFar && distanceToPlayer > ai.dieDistance) {
                entity.destroy();
            }

            //update
            if (tempTime - ai.lastChangeOfDangerAt > dangerChangeTime) {
                dangerChangeTime = (long) (ai.dangerUpdateTime * random.nextDouble() * ai.hectic);
                if (ai.hunter) {
                    if (distanceToPlayer > ai.playerdistance && distanceToPlayer < ai.playerSense) {
                        // Head to player
                        if (ai.forgiving != 0) {
                            playerPosition.add(
                                    random.nextFloat(-ai.forgiving, ai.forgiving),
                                    random.nextFloat(-ai.forgiving, ai.forgiving),
                                    random.nextFloat(-ai.forgiving, ai.forgiving));
                        }
                        ai.movementTarget.set(playerPosition);
                        ai.inDanger = true;
                        entity.saveComponent(ai);

                        // System.out.print("\nhunting palyer\n");
                    }
                }
                // run opposite direction
                if (ai.wild) {
                    if (distanceToPlayer > ai.panicDistance
                            && distanceToPlayer < ai.runDistance) {
                        runAway(entity, ai);
                    }
                }
                ai.lastChangeOfDangerAt = time.getGameTimeInMs();
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
                    idleChangeTime = (long) (ai.idlingUpdateTime * random.nextDouble() * ai.hectic);
                    idling = false;
                    // mark idling state changed
                    ai.lastChangeOfidlingtAt = time.getGameTimeInMs();
                }
                entity.saveComponent(location);
                ai.lastProgressedUpdateAt = time.getGameTimeInMs();
                return;

            }

            // check if it is time to idle again
            if (tempTime - ai.lastChangeOfMovementAt > moveChangeTime) {
                // update time
                moveChangeTime = (long) (ai.moveUpdateTime * random.nextDouble() * ai.hectic);
                idling = true;
                entity.saveComponent(location);

                // mark start idling
                ai.lastChangeOfMovementAt = time.getGameTimeInMs();
                ai.lastProgressedUpdateAt = time.getGameTimeInMs();
                return;
            }

            // Random walk
            // check if time to change direction
            if (tempTime - ai.lastChangeOfDirectionAt > directionChangeTime) {
                directionChangeTime = (long) (ai.moveUpdateTime * random.nextDouble() * ai.straightLined);
                randomWalk(worldPos, ai);
                entity.saveComponent(ai);
                // System.out.print("direction changed\n");

            }
        }

        Vector3f targetDirection = ai.movementTarget.sub(worldPos, new Vector3f());
        targetDirection.normalize();
        drive.set(targetDirection);

        float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
        entity.send(new CharacterMoveInputEvent(0, 0, yaw, drive, false, false, false, time.getGameDeltaInMs()));
        entity.saveComponent(location);

        ai.lastProgressedUpdateAt = time.getGameTimeInMs();
    }

    private void runAway(EntityRef entity, HierarchicalAIComponent ai) {
        Vector3f tempTarget = localPlayer.getPosition(new Vector3f());
        if (ai.forgiving != 0) {
            ai.movementTarget.set(new Vector3f(
                    -tempTarget.x + random.nextFloat(-ai.forgiving, ai.forgiving),
                    -tempTarget.y + random.nextFloat(-ai.forgiving, ai.forgiving),
                    -tempTarget.z + random.nextFloat(-ai.forgiving, ai.forgiving)
            ));
        } else {
            ai.movementTarget
                .set(new Vector3f(tempTarget.x * -1,
                        tempTarget.y * -1, tempTarget.z
                         * -1));
        }
        entity.saveComponent(ai);
        ai.inDanger = true;
    }

    private void randomWalk(Vector3fc worldPos, HierarchicalAIComponent ai) {
        // if ai flies
        if (ai.flying) {
            float targetY = 0;
            do {
                targetY = worldPos.y() + random.nextFloat(-100.0f, 100.0f);
            } while (targetY > ai.maxAltitude);
            ai.movementTarget.set(
                    worldPos.x() + random.nextFloat(-500.0f, 500.0f),
                    targetY,
                    worldPos.z() + random.nextFloat(-500.0f, 500.0f));
        } else {
            ai.movementTarget.set(
                    worldPos.x() + random.nextFloat(-500.0f, 500.0f),
                    worldPos.y(),
                    worldPos.z() + random.nextFloat(-500.0f, 500.0f));
        }
        ai.lastChangeOfDirectionAt = time.getGameTimeInMs();
    }

    private boolean foodInFront() {
        return false;
        // return true;
    }

    //TODO change eating thingy to use this
    @ReceiveEvent(components = {HierarchicalAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity, CharacterMovementComponent moveComp) {
        if (moveComp != null && moveComp.grounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }

}
