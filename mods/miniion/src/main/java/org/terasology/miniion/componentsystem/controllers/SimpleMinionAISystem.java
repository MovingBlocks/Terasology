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
package org.terasology.miniion.componentsystem.controllers;

import java.util.List;

import javax.vecmath.Vector3f;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.events.MinionMessageEvent;
import org.terasology.miniion.pathfinder.AStarPathing;
import org.terasology.miniion.minionenum.MinionMessagePriority;
import org.terasology.miniion.utilities.MinionMessage;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.utilities.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 7/05/12
 * Time: 18:25
 * first evolution of the minion AI, could probably use a lot of improvements
 */
@RegisterComponentSystem
public class SimpleMinionAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;
    private WorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;
    private FastRandom random = new FastRandom();
    private AStarPathing aStarPathing;
    private Timer timer;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);
        aStarPathing = new AStarPathing(worldProvider);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(SimpleMinionAIComponent.class, CharacterMovementComponent.class, LocationComponent.class, MinionComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            SimpleMinionAIComponent ai = entity.getComponent(SimpleMinionAIComponent.class);
            MinionComponent minioncomp = entity.getComponent(MinionComponent.class);

            Vector3f worldPos = new Vector3f(location.getWorldPosition());
            Vector3f drive = new Vector3f();
            //  shouldn't use local player, need some way to find nearest player
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

            if (localPlayer != null) {
                switch (minioncomp.minionBehaviour) {
                    case Follow: {
                        executeFollowAI(worldPos, localPlayer, ai, entity, drive);
                        break;
                    }
                    case Gather: {
                        executeGatherAI(worldPos, ai, entity, drive);
                        break;
                    }
                    case Move: {
                        executeMoveAI(worldPos, ai, entity, drive);
                        break;
                    }
                    case Patrol: {
                        executePatrolAI(worldPos, ai, entity, drive);
                        break;
                    }
                    case Test: {
                        executeTestAI(worldPos, ai, localPlayer, entity, drive);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }

            float yaw = 0;
            if (drive.x * drive.x + drive.y * drive.y > 0.01f) {
                yaw = (float) Math.atan2(drive.x, drive.z);
            }
            entity.send(new CharacterMoveInputEvent(0, 0, yaw, drive, false, false));
        }
    }

    private void executeFollowAI(Vector3f worldPos, LocalPlayer localPlayer, SimpleMinionAIComponent ai, EntityRef entity, Vector3f drive) {
        Vector3f dist = new Vector3f(worldPos);
        dist.sub(localPlayer.getPosition());
        double distanceToPlayer = dist.lengthSquared();


        if (distanceToPlayer > 8) {
            // Head to player
            Vector3f target = localPlayer.getPosition();
            ai.movementTarget.set(target);
            ai.followingPlayer = true;
            entity.saveComponent(ai);
        }

        setMovement(ai.movementTarget, worldPos, entity, drive);
    }

    private void executeGatherAI(Vector3f worldPos, SimpleMinionAIComponent ai, EntityRef entity, Vector3f drive) {
        List<Vector3f> targets = ai.gatherTargets;
        if ((targets == null) || (targets.size() < 1)) {
            return;
        }
        Vector3f currentTarget = targets.get(0);

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.lengthSquared();

        if (distanceToTarget < 4) {
            // gather the block
            if (timer.getTimeInMs() - ai.lastAttacktime > 500) {
                ai.lastAttacktime = timer.getTimeInMs();
                boolean attacked = attack(entity, currentTarget);
                if (!attacked) {
                    ai.gatherTargets.remove(currentTarget);
                }
            }
        }

        entity.saveComponent(ai);
        setMovement(currentTarget, worldPos, entity, drive);
    }

    private void executeMoveAI(Vector3f worldPos, SimpleMinionAIComponent ai, EntityRef entity, Vector3f drive) {
        //get targets, break if none
        List<Vector3f> targets = ai.movementTargets;
        if ((targets == null) || (targets.size() < 1)) {
            return;
        }
        Vector3f currentTarget = targets.get(0);
        // trying to solve distance calculation with some simple trick of reducing the height to 0.5, might not work for taller entities
        worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;

        //calc distance to current Target
        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.length();

        // used 1.0 here as a check, should be lower to have the minion jump on the last block, TODO need to calc middle of block
        if (distanceToTarget < 0.1d) {
            ai.movementTargets.remove(0);
            entity.saveComponent(ai);
            currentTarget = null;
            return;
        }

        setMovement(currentTarget, worldPos, entity, drive);
    }

    private void executePatrolAI(Vector3f worldPos, SimpleMinionAIComponent ai, EntityRef entity, Vector3f drive) {
        //get targets, break if none
        List<Vector3f> targets = ai.patrolTargets;
        if ((targets == null) || (targets.size() < 1)) {
            return;
        }
        int patrolCounter = ai.patrolCounter;
        Vector3f currentTarget = null;

        //get the patrol point
        if (patrolCounter < targets.size()) {
            currentTarget = targets.get(patrolCounter);
        }

        if (currentTarget == null) {
            return;
        }

        //calc distance to current Target
        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);
        double distanceToTarget = dist.length();

        if (distanceToTarget < 0.1d) {
            patrolCounter++;
            if (!(patrolCounter < targets.size())) patrolCounter = 0;
            ai.patrolCounter = patrolCounter;
            entity.saveComponent(ai);
            return;
        }

        setMovement(currentTarget, worldPos, entity, drive);
    }

    private void executeTestAI(Vector3f worldPos, SimpleMinionAIComponent ai, LocalPlayer localPlayer, EntityRef entity, Vector3f drive) {
        if (!ai.locked) {
            //get targets, break if none
            List<Vector3f> targets = ai.movementTargets;
            List<Vector3f> pathTargets = ai.pathTargets;
            if ((targets == null) || (targets.size() < 1)) {
                return;
            }

            Vector3f currentTarget; // check if currentTarget target is a path or not
            if ((pathTargets != null) && (pathTargets.size() > 0)) {
                currentTarget = pathTargets.get(0);
            } else {
                currentTarget = targets.get(0);
            }
            if (ai.previousTarget != ai.movementTargets.get(0)) {
                ai.locked = true;
                ai.pathTargets = aStarPathing.findPath(worldPos, new Vector3f(currentTarget));
                if (ai.pathTargets == null) {
                    MinionSystem minionSystem = new MinionSystem();
                    MinionMessage messagetosend = new MinionMessage(MinionMessagePriority.Debug, "test", "testdesc", "testcont", entity, localPlayer.getCharacterEntity());
                    entity.send(new MinionMessageEvent(messagetosend));
                    ai.movementTargets.remove(0);
                }
            }
            ai.locked = false;
            if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
                pathTargets = ai.pathTargets;
                ai.previousTarget = targets.get(0); // used to check if the final target changed
                currentTarget = pathTargets.get(0);
            }

            // trying to solve distance calculation with some simple trick of reducing the height to a round int, might not work for taller entities
            worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;
            //calc distance to current Target
            Vector3f dist = new Vector3f(worldPos);
            dist.sub(currentTarget);
            double distanceToTarget = dist.length();

            if (distanceToTarget < 0.1d) {
                if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
                    ai.pathTargets.remove(0);
                    entity.saveComponent(ai);
                } else {
                    if (ai.movementTargets.size() > 0) {
                        ai.movementTargets.remove(0);
                    }
                    ai.previousTarget = null;
                    entity.saveComponent(ai);
                }
                return;
            }

            setMovement(currentTarget, worldPos, entity, drive);
        }
    }

    private void setMovement(Vector3f currentTarget, Vector3f worldPos, EntityRef entity, Vector3f drive) {
        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(currentTarget, worldPos);
        if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f) {
            targetDirection.normalize();
            drive.set(targetDirection);
        } else {
            drive.set(new Vector3f());
        }
    }

    private boolean attack(EntityRef player, Vector3f position) {

        int damage = 1;
        Block block = worldProvider.getBlock(new Vector3f(position.x, position.y - 0.5f, position.z));
        if ((block.isDestructible()) && (block.isTargetable())) {
            EntityRef blockEntity = blockEntityRegistry.getOrCreateEntityAt(new Vector3i(position));
            if (blockEntity == EntityRef.NULL) {
                return false;
            } else {
                blockEntity.send(new DamageEvent(damage, player));
                return true;
            }
        }
        return false;
    }

    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if ((moveComp != null) && (moveComp.grounded)) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
}
