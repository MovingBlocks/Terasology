package org.terasology.componentSystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.pathfinder.AStarPathing;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 7/05/12
 * Time: 18:25
 * first evolution of the minion AI, could probably use a lot of improvements
 */
public class SimpleMinionAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;
    private FastRandom random = new FastRandom();
    private AStarPathing aStarPathing;
    private Timer timer;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);
        aStarPathing = new AStarPathing(worldProvider);
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(SimpleMinionAIComponent.class, CharacterMovementComponent.class, LocationComponent.class, MinionComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            SimpleMinionAIComponent ai = entity.getComponent(SimpleMinionAIComponent.class);
            CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
            MinionComponent minioncomp = entity.getComponent(MinionComponent.class);

            Vector3f worldPos = new Vector3f(location.getWorldPosition());
            moveComp.getDrive().set(0,0,0);
            //  shouldn't use local player, need some way to find nearest player
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

            if (localPlayer != null)
            {
                switch(minioncomp.minionBehaviour){
                    case Follow: {
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
                        break;
                    }
                    case Gather:{

                        List<Vector3f> targets = ai.gatherTargets;
                        if(targets == null || targets.size() < 1) break;
                        Vector3f currentTarget = targets.get(0);

                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.lengthSquared();

                        if (distanceToTarget < 4) {
                            // gather the block
                            if(timer.getTimeInMs() - ai.lastAttacktime > 500){
                                ai.lastAttacktime = timer.getTimeInMs();
                                boolean attacked = attack(entity,currentTarget);
                                if(!attacked) {
                                    ai.gatherTargets.remove(currentTarget);
                                }
                            }
                        }

                        entity.saveComponent(ai);
                        setMovement(currentTarget, worldPos, entity, moveComp, location);
                        break;
                    }
                    case Move:{
                        //get targets, break if none
                        List<Vector3f> targets = ai.movementTargets;
                        if(targets == null || targets.size() < 1) break;
                        Vector3f currentTarget = targets.get(0);
                        // trying to solve distance calculation with some simple trick of reducing the height to 0.5, might not work for taller entities
                        worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;

                        //calc distance to current Target
                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.length();

                        // used 1.0 here as a check, should be lower to have the minion jump on the last block, TODO need to calc middle of block
                        if(distanceToTarget < 0.1d){
                            ai.movementTargets.remove(0);
                            entity.saveComponent(ai);
                            currentTarget = null;
                            break;
                        }

                        setMovement(currentTarget, worldPos, entity, moveComp, location);
                        break;

                    }
                    case Patrol:{
                        //get targets, break if none
                        List<Vector3f> targets = ai.patrolTargets;
                        if(targets == null || targets.size() < 1) break;
                        int patrolCounter = ai.patrolCounter;
                        Vector3f currentTarget = null;

                        //get the patrol point
                        if(patrolCounter < targets.size()){
                            currentTarget = targets.get(patrolCounter);
                        }

                        if(currentTarget == null){
                            break;
                        }

                        //calc distance to current Target
                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.length();

                        if(distanceToTarget < 0.1d){
                            patrolCounter++;
                            if(!(patrolCounter < targets.size())) patrolCounter = 0;
                            ai.patrolCounter = patrolCounter;
                            entity.saveComponent(ai);
                            break;
                        }

                        setMovement(currentTarget,worldPos,entity,moveComp,location);

                        break;
                    }
                    case Test:{
                        if(!ai.locked){
                            //get targets, break if none
                            List<Vector3f> targets = ai.movementTargets;
                            List<Vector3f> pathTargets = ai.pathTargets;
                            if(targets == null || targets.size() < 1) break;

                            Vector3f currentTarget; // check if currentTarget target is a path or not
                            if(pathTargets != null && pathTargets.size() > 0)
                            {
                                currentTarget = pathTargets.get(0);
                            }
                            else
                            {
                                currentTarget = targets.get(0);
                            }
                            if(ai.previousTarget != ai.movementTargets.get(0)){
                                ai.locked = true;
                                ai.pathTargets = aStarPathing.findPath(worldPos, new Vector3f(currentTarget));
                            }
                            ai.locked = false;
                            if(ai.pathTargets != null && ai.pathTargets.size() > 0){
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

                            // TODO need a good way to deal with distance calculation
                            if(distanceToTarget < 0.1d){
                                if(ai.pathTargets != null && ai.pathTargets.size() > 0){
                                    ai.pathTargets.remove(0);
                                    entity.saveComponent(ai);
                                }
                                else{
                                    ai.movementTargets.remove(0);
                                    ai.previousTarget = null;
                                    entity.saveComponent(ai);
                                }
                                break;
                            }

                            setMovement(currentTarget, worldPos, entity, moveComp, location);
                            break;
                        }
                    }
                    default: {
                        break;
                    }
                }

            }
        }
    }

    private void setMovement(Vector3f currentTarget, Vector3f worldPos, EntityRef entity, CharacterMovementComponent moveComp, LocationComponent location){
        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(currentTarget, worldPos);
        if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f) {
            targetDirection.normalize();
            moveComp.setDrive(targetDirection);

            float yaw = (float)Math.atan2(targetDirection.x, targetDirection.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
            location.getLocalRotation().set(axisAngle);
        } else {
            moveComp.setDrive(new Vector3f());
        }
        entity.saveComponent(moveComp);
        entity.saveComponent(location);
    }

    private boolean attack(EntityRef player, Vector3f position) {

        int damage = 1;
        Block block = BlockManager.getInstance().getBlock(worldProvider.getBlockAtPosition(new Vector3d(position.x, position.y, position.z)));
        if (block.isDestructible() && !block.isSelectionRayThrough()) {
            EntityRef blockEntity = blockEntityRegistry.getOrCreateEntityAt(new Vector3i(position));
            blockEntity.send(new DamageEvent(damage, player));
            return true;
        }
            return false;
    }

    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.isGrounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
}
