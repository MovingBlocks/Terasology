package org.terasology.componentSystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.*;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.DamageEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

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
@RegisterComponentSystem(authorativeOnly = true)
public class SimpleMinionAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;
    private WorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;
    private FastRandom random = new FastRandom();
    //private AStarPathfinder aStarPathfinder;
    private Timer timer;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);
        //aStarPathfinder = new AStarPathfinder(worldProvider);
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

            Vector3f worldPos = location.getWorldPosition();
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

                        Vector3f targetDirection = new Vector3f();
                        targetDirection.sub(ai.movementTarget, worldPos);
                        if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f) {
                            targetDirection.normalize();
                            moveComp.setDrive(targetDirection);

                            float yaw = (float)Math.atan2(targetDirection.x, targetDirection.z);
                            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
                            location.getLocalRotation().set(axisAngle);
                        } else {
                            moveComp.getDrive().set(0,0,0);
                        }
                        entity.saveComponent(moveComp);
                        entity.saveComponent(location);
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

                        Vector3f targetDirection = new Vector3f();
                        targetDirection.sub(currentTarget, worldPos);
                        if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f)  {
                            targetDirection.normalize();
                            moveComp.setDrive(targetDirection);

                            float yaw = (float)Math.atan2(targetDirection.x, targetDirection.z);
                            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
                            location.getLocalRotation().set(axisAngle);
                        }
                        else {
                            moveComp.setDrive(new Vector3f());
                        }
                        entity.saveComponent(ai);
                        entity.saveComponent(moveComp);
                        entity.saveComponent(location);
                        break;
                    }
                    case Move:{
                        //get targets, break if none
                        List<Vector3f> targets = ai.movementTargets;
                        if(targets == null || targets.size() < 1) break;
                        Vector3f currentTarget = targets.get(0);

                        //calc distance to current Target
                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.length();


                        // used 1.0 here as a check, should be lower to have the minion jump on the last block, TODO need to calc middle of block
                        if(distanceToTarget < 1.0d){
                            ai.movementTargets.remove(0);
                            entity.saveComponent(ai);
                            currentTarget = null;
                            break;
                        }

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

                        if(distanceToTarget < 1.0d){
                            patrolCounter++;
                            if(!(patrolCounter < targets.size())) patrolCounter = 0;
                            ai.patrolCounter = patrolCounter;
                            entity.saveComponent(ai);
                            break;
                        }

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
                        break;
                    }
                    case Test:{
                        /*List<Vector3f> targets = ai.movementTargets;
                        if(targets == null || targets.size() < 1) break;

                        if(timer.getTimeInMs() - pathtime > 3000){
                            aStarPathfinder.setVis(false);
                            ai.followingPlayer = false;
                            entity.saveComponent(ai);

                            if(currentTarget == null) currentTarget = targets.get(0);
                            if(lastTarget == null) lastTarget = targets.get(0);
                            if(lastaiTarget == null) lastaiTarget = targets.get(0);
                            Vector3f dist = new Vector3f(worldPos);
                            dist.sub(lastTarget);
                            double distanceToTarget = dist.lengthSquared();

                            if(lastTarget == currentTarget && distanceToTarget - lastDistance < 0.1){
                                //if(paths.size() > 0)
                                currentTarget = new Vector3f(paths.remove(0));
                            }
                            else{
                                lastDistance = distanceToTarget;
                                Vector3f tmpvec = currentTarget;
                                tmpvec.sub(ai.movementTargets.get(0));
                                double distrem = tmpvec.length();
                                if(distrem < 0.1) ai.movementTargets.remove(0);
                            }

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
                            break;
                        }*/
                    }
                    default: {
                        break;
                    }
                }

            }
        }
    }

    private boolean attack(EntityRef player, Vector3f position) {

        int damage = 1;
        Block block = worldProvider.getBlock(position);
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
