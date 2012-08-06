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
package org.terasology.mods.miniions.componentsystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.physics.HitResult;
import org.terasology.physics.ImpulseEvent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.DeadEntityFactory;
import org.terasology.entitySystem.*;
import org.terasology.events.DamageEvent;
import org.terasology.events.HitEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.components.SimpleMinionAIComponent;
import org.terasology.mods.miniions.events.MinionMessageEvent;
import org.terasology.mods.miniions.logic.pathfinder.AStarPathing;
import org.terasology.mods.miniions.minionenum.MinionMessagePriority;
import org.terasology.mods.miniions.utilities.MinionMessage;
import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;
import java.util.List;

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
    private DeadEntityFactory deadEntityFactory;
    private FastRandom random = new FastRandom();
    private AStarPathing aStarPathing;
    private Timer timer;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);
        deadEntityFactory = new DeadEntityFactory();
        aStarPathing = new AStarPathing(worldProvider);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(SimpleMinionAIComponent.class, CharacterMovementComponent.class, LocationComponent.class, MinionComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            SimpleMinionAIComponent ai = entity.getComponent(SimpleMinionAIComponent.class);
            CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
            MinionComponent minioncomp = entity.getComponent(MinionComponent.class);

            Vector3f worldPos = new Vector3f(location.getWorldPosition());
            moveComp.getDrive().set(0, 0, 0);
            //  shouldn't use local player, need some way to find nearest player
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

            if (localPlayer != null) {
                switch (minioncomp.minionBehaviour) {
                    case Follow: {
                        executeFollowAI(worldPos, localPlayer, ai, entity, moveComp, location);
                        break;
                    }
                    case Gather: {
                        executeGatherAI(worldPos, ai, entity, moveComp, location);
                        break;
                    }
                    case Move: {
                        executeMoveAI(worldPos, ai, entity, moveComp, location);
                        break;
                    }
                    case Patrol: {
                        executePatrolAI(worldPos, ai, entity, moveComp, location);
                        break;
                    }
                    case Test: {
                        executeTestAI(worldPos, ai, localPlayer, entity, moveComp, location);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
    }

    private void executeFollowAI(Vector3f worldPos, LocalPlayer localPlayer, SimpleMinionAIComponent ai, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location) {
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

        setMovement(ai.movementTarget, worldPos, entity, moveComp, location);
    }

    private void executeGatherAI(Vector3f worldPos, SimpleMinionAIComponent ai, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location) {
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
        setMovement(currentTarget, worldPos, entity, moveComp, location);
    }

    private void executeMoveAI(Vector3f worldPos, SimpleMinionAIComponent ai, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location) {
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

        setMovement(currentTarget, worldPos, entity, moveComp, location);
    }

    private void executePatrolAI(Vector3f worldPos, SimpleMinionAIComponent ai, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location) {
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

        setMovement(currentTarget, worldPos, entity, moveComp, location);
    }

    private void executeTestAI(Vector3f worldPos, SimpleMinionAIComponent ai, LocalPlayer localPlayer, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location) {
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
                    MinionMessage messagetosend = new MinionMessage(MinionMessagePriority.Debug, "test", "testdesc", "testcont", entity, localPlayer.getEntity());
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
            setMovement(currentTarget, worldPos, entity, moveComp, location);
        }
    }

    private void setMovement(Vector3f currentTarget, Vector3f worldPos, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location) {
        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(currentTarget, worldPos);
        if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f) {
            targetDirection.normalize();
            moveComp.setDrive(targetDirection);

            float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            location.getLocalRotation().set(axisAngle);
        } else {
            moveComp.setDrive(new Vector3f());
        }
        entity.saveComponent(moveComp);
        entity.saveComponent(location);
    }

    private boolean attack(EntityRef player, Vector3f position) {

        int damage = 1;
        Block block = worldProvider.getBlock(new Vector3f(position.x, position.y - 0.5f, position.z));
        if ((block.isDestructible()) && (!block.isSelectionRayThrough())) {
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
        if ((moveComp != null) && (moveComp.isGrounded)) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
    
    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
       //Create Dead Body (temporarily)
       EntityRef deadEntity = deadEntityFactory.newInstance(30,entity);
       //TODO Should be dynamic based on the position and the force of of the last strike
       //This is an exaggerated, static example for testing purposes.
       Vector3f impulse = new Vector3f(40, 40, 40);
	   deadEntity.send(new ImpulseEvent(impulse));
	   entity.destroy();
    }
    
    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onHit(HitEvent event, EntityRef entity) {
    	//Push Entity on Hit (needs Rigid Body);
    	HitResult hitResult = event.getHitResult();
    	Vector3f hitNormal = hitResult.getHitNormal();
    	hitNormal.scale(20);//TODO Should scale to the force of the impact
  	    entity.send(new ImpulseEvent(hitNormal));
    }
}
