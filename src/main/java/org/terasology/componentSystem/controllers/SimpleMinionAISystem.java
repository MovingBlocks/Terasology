package org.terasology.componentSystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.block.BlockEntityRegistry;
import org.terasology.components.*;
import org.terasology.entitySystem.*;
import org.terasology.events.DamageEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
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
@RegisterComponentSystem(authorativeOnly = true)
public class SimpleMinionAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;
    private FastRandom random = new FastRandom();
    private Timer timer;
    private long time;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        timer = CoreRegistry.get(Timer.class);
        time = timer.getTimeInMs();
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

                        Vector3f dist = new Vector3f(worldPos);
                        dist.sub(ai.movementTarget);
                        double distanceToTarget = dist.lengthSquared();

                        if (distanceToTarget < 4) {
                            // gather the block
                            if(timer.getTimeInMs() - time > 500){
                                time = timer.getTimeInMs();
                                attack(entity,ai.movementTarget);
                            }
                        }

                        Vector3f targetDirection = new Vector3f();
                        targetDirection.sub(ai.movementTarget, worldPos);
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
                        ai.followingPlayer = false;
                        entity.saveComponent(ai);
                        Vector3f targetDirection = new Vector3f();
                        targetDirection.sub(ai.movementTarget, worldPos);
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
                }

            }
        }
    }

    private void attack(EntityRef player, Vector3f position) {
        //RayBlockIntersection.Intersection selectedBlock = calcSelectedBlock(position, moveCompn);
        //ItemComponent item = withItem.getComponent(ItemComponent.class);

        //if (selectedBlock != null) {
            //BlockPosition blockPos = selectedBlock.getBlockPosition();

            int damage = 1;
            /*if (item != null) {
                damage = item.baseDamage;
                if (item.getPerBlockDamageBonus().containsKey(block.getBlockFamily().getTitle())) {
                    damage += item.getPerBlockDamageBonus().get(block.getBlockFamily().getTitle());
                }
            }*/
            Block block = BlockManager.getInstance().getBlock(worldProvider.getBlockAtPosition(new Vector3d(position.x, position.y, position.z)));
            if (block.isDestructible() && !block.isSelectionRayThrough()) {
                EntityRef blockEntity = blockEntityRegistry.getOrCreateEntityAt(new Vector3i(position));
                blockEntity.send(new DamageEvent(damage, player));
            }
        //}


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
