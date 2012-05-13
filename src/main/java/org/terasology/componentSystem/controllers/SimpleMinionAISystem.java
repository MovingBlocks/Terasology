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
import org.terasology.logic.pathfinder.AStarPathfinder;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
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
    private AStarPathfinder aStarPathfinder;
    private Timer timer;
    private long attacktime, pathtime;
    private List<Vector3d> paths = null;
    private Vector3f lastaiTarget = null;
    private Vector3f lastTarget = null;
    private Vector3f currentTarget = null;
    private Vector3f tempTarget = null;
    private double lastDistance;
    private int patrolCounter;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);
        attacktime = timer.getTimeInMs();
        aStarPathfinder = new AStarPathfinder(worldProvider);
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
                        if(targets == null || targets.size() < 1) return;
                        currentTarget = targets.get(0);

                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.lengthSquared();

                        if (distanceToTarget < 4) {
                            // gather the block
                            if(timer.getTimeInMs() - attacktime > 500){
                                attacktime = timer.getTimeInMs();
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
                        entity.saveComponent(moveComp);
                        entity.saveComponent(location);
                        break;
                    }
                    case Move:{
                        //get targets, return if none
                        List<Vector3f> targets = ai.movementTargets;
                        if(targets == null || targets.size() < 1) return;

                        //prob useless now, old artifact
                        ai.followingPlayer = false;
                        entity.saveComponent(ai);

                        //init if no targets
                        currentTarget = targets.get(0);
                        if(lastTarget == null) lastTarget = targets.get(0);

                        //calc distance to current Target
                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.lengthSquared();

                        if(distanceToTarget < 1.0d){
                            ai.movementTargets.remove(0);
                            currentTarget = null;
                            lastTarget = null;
                            break;
                            //if(ai.movementTargets.size() > 0)
                            //currentTarget = ai.movementTargets.get(0);
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
                        //get targets, return if none
                        List<Vector3f> targets = ai.patrolTargets;
                        if(targets == null || targets.size() < 1) return;

                        //prob useless now, old artifact
                        ai.followingPlayer = false;
                        entity.saveComponent(ai);

                        //init if no targets
                        if(patrolCounter < targets.size()){
                            currentTarget = targets.get(patrolCounter);
                            if(lastTarget == null) lastTarget = targets.get(0);
                        }

                        //calc distance to current Target
                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(currentTarget);
                        double distanceToTarget = dist.length();

                        if(distanceToTarget < 1.0d){
                            patrolCounter++;
                            if(!(patrolCounter < targets.size())) patrolCounter = 0;
                            currentTarget = null;
                            lastTarget = null;
                            break;
                            //if(ai.movementTargets.size() > 0)
                            //currentTarget = ai.movementTargets.get(0);
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
                        if(targets == null || targets.size() < 1) return;

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
        Block block = BlockManager.getInstance().getBlock(worldProvider.getBlockAtPosition(new Vector3d(position.x, position.y, position.z)));
        if (block.isDestructible() && !block.isSelectionRayThrough()) {
            EntityRef blockEntity = blockEntityRegistry.getOrCreateEntityAt(new Vector3i(position));
            blockEntity.send(new DamageEvent(damage, player));
            return true;
        }
            return false;
    }

    /*private RayBlockIntersection.Intersection calcSelectedBlock(Vector3f position,CharacterMovementComponent moveCompn) {
        //  Proper and centralised ray tracing support though world
        List<RayBlockIntersection.Intersection> inters = new ArrayList<RayBlockIntersection.Intersection>();

        Vector3f pos = new Vector3f(position.x, position.y -1, position.z);

        int blockPosX, blockPosY, blockPosZ;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    // Make sure the correct block positions are calculated relatively to the position of the player
                    blockPosX = (int) (pos.x + (pos.x >= 0 ? 0.5f : -0.5f)) + x;
                    blockPosY = (int) (pos.y + (pos.y >= 0 ? 0.5f : -0.5f)) + y;
                    blockPosZ = (int) (pos.z + (pos.z >= 0 ? 0.5f : -0.5f)) + z;

                    byte blockType = worldProvider.getBlock(blockPosX, blockPosY, blockPosZ);

                    // Ignore special blocks
                    if (BlockManager.getInstance().getBlock(blockType).isSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    List<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(worldProvider, blockPosX, blockPosY, blockPosZ, new Vector3d(position), new Vector3d(moveCompn.getDrive()));

                    if (iss != null) {
                        inters.addAll(iss);
                    }
                }
            }
        }

        /**
         * Calculated the closest intersection.
         */
        /*if (inters.size() > 0) {
            Collections.sort(inters);
            return inters.get(0);
        }

        return null;
    }*/

    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.isGrounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
}
