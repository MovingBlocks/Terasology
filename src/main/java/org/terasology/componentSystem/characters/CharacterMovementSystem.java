package org.terasology.componentSystem.characters;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.FootstepEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.JumpEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.logic.world.WorldUtil;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.BlockPosition;
import org.terasology.performanceMonitor.PerformanceMonitor;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class CharacterMovementSystem implements UpdateSubscriberSystem {

    public static final float UnderwaterGravity = 0.25f;
    public static final float Gravity = 28.0f;
    public static final float TerminalVelocity = 64.0f;
    public static final float UnderwaterInertia = 2.0f;
    public static final float WaterTerminalVelocity = 4.0f;
    public static final float GhostInertia = 4f;

    private EntityManager entityManager;
    private IWorldProvider worldProvider;

    // For reuse to save memory churn
    private AABB entityAABB = new AABB(new Vector3d(), new Vector3d());
    
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
    }
    
    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(CharacterMovementComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (!worldProvider.isChunkAvailableAt(location.getWorldPosition())) continue;

            AABBCollisionComponent collision = entity.getComponent(AABBCollisionComponent.class);
            CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);

            updatePosition(delta, entity, location, collision, movementComp);
            updateSwimStatus(location, collision, movementComp);
            
            entity.saveComponent(location);
            entity.saveComponent(movementComp);
        }
    }

    /**
     * Updates whether a character is underwater. A higher and lower point of the character is tested for being in water,
     * only if both points are in water does the character count as swimming.
     * @param location
     * @param aabb
     * @param movementComp
     */
    private void updateSwimStatus(LocationComponent location, AABBCollisionComponent aabb, CharacterMovementComponent movementComp) {
        Vector3f worldPos = location.getWorldPosition();
        List<BlockPosition> blockPositions = WorldUtil.gatherAdjacentBlockPositions(worldPos);
        boolean topUnderwater = false;
        boolean bottomUnderwater = false;
        Vector3f top = new Vector3f(worldPos);
        Vector3f bottom = new Vector3f(worldPos);
        top.y += aabb.getExtents().y / 2;
        bottom.y -= aabb.getExtents().y / 2;

        for (int i = 0; i < blockPositions.size(); i++) {
            BlockPosition p = blockPositions.get(i);
            byte blockType = worldProvider.getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
            Block block = BlockManager.getInstance().getBlock(blockType);

            if (block.isLiquid()) {
                for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                    if (blockAABB.contains(top)) {
                        topUnderwater = true;
                    }
                    if (blockAABB.contains(bottom)) {
                        bottomUnderwater = true;
                    }
                }
            }
        }
        boolean newSwimming = topUnderwater && bottomUnderwater;

        // Boost when leaving water
        if (!newSwimming && movementComp.isSwimming && movementComp.getVelocity().y > 0) {
            float len = movementComp.getVelocity().length();
            movementComp.getVelocity().scale((len + 8)/len);
        }
        movementComp.isSwimming = newSwimming;
    }

    private void updatePosition(float delta, EntityRef entity, LocationComponent location, AABBCollisionComponent collision, CharacterMovementComponent movementComp) {

        if (movementComp.isGhosting) {
            ghost(delta, entity, location, collision, movementComp);
        } else if (movementComp.isSwimming) {
            swim(delta, entity, location, collision, movementComp);
        } else {
            walk(delta, entity, location, collision, movementComp);
        }
    }

    private void ghost(float delta, EntityRef entity, LocationComponent location, AABBCollisionComponent collision, CharacterMovementComponent movementComp) {
        Vector3f desiredVelocity = new Vector3f(movementComp.getDrive());
        float maxSpeed = movementComp.maxGhostSpeed;
        if (movementComp.isRunning) {
            maxSpeed *= movementComp.runFactor;
        }

        desiredVelocity.scale(maxSpeed);

        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(movementComp.getVelocity());

        velocityDiff.scale(Math.min(GhostInertia * delta, 1.0f));

        movementComp.getVelocity().add(velocityDiff);
        
        // No collision, so just do the move
        Vector3f worldPos = location.getWorldPosition();
        Vector3f deltaPos = new Vector3f(movementComp.getVelocity());
        deltaPos.scale(delta);
        worldPos.add(deltaPos);
        location.setWorldPosition(worldPos);

        if (movementComp.faceMovementDirection && movementComp.getVelocity().lengthSquared() > 0.01f) {
            float yaw = (float)Math.atan2(movementComp.getVelocity().x, movementComp.getVelocity().z);
            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
            location.getLocalRotation().set(axisAngle);
        }
    }

    private void walk(float delta, EntityRef entity, LocationComponent location, AABBCollisionComponent collision, CharacterMovementComponent movementComp) {
        Vector3f desiredVelocity = new Vector3f(movementComp.getDrive());
        float maxSpeed = movementComp.maxGroundSpeed;
        if (movementComp.isRunning) {
            maxSpeed *= movementComp.runFactor;
        }

        // As we can't use it, remove the y component of desired movement while maintaining speed
        if (desiredVelocity.y != 0) {
            float speed = desiredVelocity.length();
            desiredVelocity.y = 0;
            if (desiredVelocity.x != 0 || desiredVelocity.z != 0) {
                desiredVelocity.normalize();
                desiredVelocity.scale(speed);
            }
        }
        desiredVelocity.scale(maxSpeed);

        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(movementComp.getVelocity());
        velocityDiff.y = 0;

        velocityDiff.scale(Math.min(movementComp.groundFriction * delta, 1.0f));

        movementComp.getVelocity().x += velocityDiff.x;
        movementComp.getVelocity().z += velocityDiff.z;
        movementComp.getVelocity().y = Math.max(-TerminalVelocity, (float)(movementComp.getVelocity().y - Gravity * delta));

        // TODO: replace this with swept collision based on JBullet?
        Vector3f worldPos = location.getWorldPosition();
        Vector3f oldPos = new Vector3f(worldPos);
        worldPos.y += movementComp.getVelocity().y * delta;

        Vector3f extents = new Vector3f(collision.getExtents());
        extents.scale(location.getWorldScale());

        if (verticalHitTest(worldPos, oldPos, extents)) {
            if (!movementComp.isGrounded) {
                entity.send(new VerticalCollisionEvent(movementComp.getVelocity(), worldPos));
                movementComp.isGrounded = true;
            }
            movementComp.getVelocity().y = 0;
            // Jumping is only possible, if the entity is standing on ground
            if (movementComp.jump) {
                entity.send(new JumpEvent());
                movementComp.jump = false;
                movementComp.isGrounded = false;
                movementComp.getVelocity().y += movementComp.jumpSpeed;
            }

        } else {
            movementComp.isGrounded = false;
            movementComp.jump = false;
        }

        oldPos.set(worldPos);
        /*
         * Update the position of the entity
         * according to the acceleration vector.
         */
        worldPos.x += movementComp.getVelocity().x * delta;
        worldPos.z += movementComp.getVelocity().z * delta;

        /*
         * Check for horizontal collisions __after__ checking for vertical
         * collisions.
         */
        if (horizontalHitTest(worldPos, oldPos, extents)) {
            entity.send(new HorizontalCollisionEvent());
            movementComp.getVelocity().x = (worldPos.x - oldPos.x) / delta;
            movementComp.getVelocity().z = (worldPos.z - oldPos.z) / delta;
        }

        Vector3f dist = new Vector3f(worldPos.x - oldPos.x, 0, worldPos.z - oldPos.z);

        if (movementComp.isGrounded) {
            movementComp.footstepDelta += dist.length();
            if (movementComp.footstepDelta > movementComp.distanceBetweenFootsteps) {
                movementComp.footstepDelta -= movementComp.distanceBetweenFootsteps;
                entity.send(new FootstepEvent());
            }
        }
        location.setWorldPosition(worldPos);

        if (movementComp.faceMovementDirection && movementComp.getVelocity().lengthSquared() > 0.01f) {
            float yaw = (float)Math.atan2(movementComp.getVelocity().x, movementComp.getVelocity().z);
            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
            location.getLocalRotation().set(axisAngle);
        }
    }

    private void swim(float delta, EntityRef entity, LocationComponent location, AABBCollisionComponent collision, CharacterMovementComponent movementComp) {
        Vector3f desiredVelocity = new Vector3f(movementComp.getDrive());
        float maxSpeed = movementComp.maxWaterSpeed;
        if (movementComp.isRunning) {
            maxSpeed *= movementComp.runFactor;
        }
        desiredVelocity.scale(maxSpeed);

        desiredVelocity.y -= UnderwaterGravity;

        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(movementComp.getVelocity());
        velocityDiff.scale(Math.min(UnderwaterInertia * delta,1.0f));

        movementComp.getVelocity().x += velocityDiff.x;
        movementComp.getVelocity().y += velocityDiff.y;
        movementComp.getVelocity().z += velocityDiff.z;

        // Slow down due to friction
        float speed = movementComp.getVelocity().length();
        if (speed > movementComp.maxWaterSpeed) {
            movementComp.getVelocity().scale((speed - 4 * (speed - movementComp.maxWaterSpeed) * delta) / speed);
        }

        // TODO: replace this with swept collision based on JBullet?
        Vector3f worldPos = location.getWorldPosition();
        Vector3f oldPos = new Vector3f(worldPos);
        worldPos.y += movementComp.getVelocity().y * delta;
        
        Vector3f extents = new Vector3f(collision.getExtents());
        extents.scale(location.getWorldScale());

        if (verticalHitTest(worldPos, oldPos, extents)) {
            movementComp.getVelocity().y = 0;
        }

        oldPos.set(worldPos);
        /*
         * Update the position of the entity
         * according to the acceleration vector.
         */
        worldPos.x += movementComp.getVelocity().x * delta;
        worldPos.z += movementComp.getVelocity().z * delta;

        /*
         * Check for horizontal collisions __after__ checking for vertical
         * collisions.
         */
        if (horizontalHitTest(worldPos, oldPos, extents)) {
            entity.send(new HorizontalCollisionEvent());
            movementComp.getVelocity().x = (worldPos.x - oldPos.x) / delta;
            movementComp.getVelocity().z = (worldPos.z - oldPos.z) / delta;
        }


        location.setWorldPosition(worldPos);

        if (movementComp.faceMovementDirection && movementComp.getVelocity().lengthSquared() > 0.01f) {
            float yaw = (float)Math.atan2(movementComp.getVelocity().x, movementComp.getVelocity().z);
            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
            location.getLocalRotation().set(axisAngle);
        }
    }

    /**
     * Checks for blocks below and above the entity.
     *
     * @param origin The origin position of the entity
     * @return True if a vertical collision was detected
     */
    private boolean verticalHitTest(Vector3f position, Vector3f origin, Vector3f extents) {

        List<BlockPosition> blockPositions = WorldUtil.gatherAdjacentBlockPositions(origin);

        boolean moved = false;

        for (int i = 0; i < blockPositions.size(); i++) {
            BlockPosition p = blockPositions.get(i);
            byte blockType1 = worldProvider.getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
            calcAABB(position, extents);

            Block block = BlockManager.getInstance().getBlock(blockType1);
            if (block == null || block.isPenetrable())
                continue;
            for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                if (!entityAABB.overlaps(blockAABB))
                    continue;

                double direction = origin.y -position.y;

                if (direction >= 0) {
                    position.y = (float)(blockAABB.getPosition().y + blockAABB.getDimensions().y + entityAABB.getDimensions().y);
                    position.y += java.lang.Math.ulp(position.y);
                } else {
                    position.y = (float)(blockAABB.getPosition().y - blockAABB.getDimensions().y - entityAABB.getDimensions().y);
                    position.y -= java.lang.Math.ulp(position.y);
                }


               return true;
            }
        }

        PerformanceMonitor.endActivity();
        return moved;
    }

    private boolean horizontalHitTest(Vector3f position, Vector3f origin, Vector3f extents) {
        boolean result = false;
        List<BlockPosition> blockPositions = WorldUtil.gatherAdjacentBlockPositions(origin);

        // Check each block position for collision
        for (int i = 0; i < blockPositions.size(); i++) {
            BlockPosition p = blockPositions.get(i);
            byte blockType = worldProvider.getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
            Block block = BlockManager.getInstance().getBlock(blockType);

            if (!block.isPenetrable()) {
                for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                    if (calcAABB(position, extents).overlaps(blockAABB)) {
                        result = true;
                        Vector3d direction = new Vector3d(position.x, 0f, position.z);
                        direction.x -= origin.x;
                        direction.z -= origin.z;

                        // Calculate the point of intersection on the block's AABB
                        Vector3d blockPoi = blockAABB.closestPointOnAABBToPoint(new Vector3d(origin));
                        Vector3d entityPoi = calcAABB(origin, extents).closestPointOnAABBToPoint(blockPoi);

                        Vector3d planeNormal = blockAABB.getFirstHitPlane(direction, new Vector3d(origin), new Vector3d(extents), true, false, true);

                        // Find a vector parallel to the surface normal
                        Vector3d slideVector = new Vector3d(planeNormal.z, 0, -planeNormal.x);
                        Vector3d pushBack = new Vector3d();

                        pushBack.sub(blockPoi, entityPoi);

                        // Calculate the intensity of the diversion alongside the block
                        double length = slideVector.dot(direction);

                        Vector3d newPosition = new Vector3d();
                        newPosition.z = origin.z + pushBack.z * 0.2 + length * slideVector.z;
                        newPosition.x = origin.x + pushBack.x * 0.2 + length * slideVector.x;
                        newPosition.y = origin.y;

                        // Update the position
                        position.set(newPosition);
                    }
                }
            }
        }

        return result;
    }

    private AABB calcAABB(Vector3f position, Vector3f extents)
    {
        entityAABB.getPosition().set(position);
        entityAABB.getDimensions().set(extents);
        return entityAABB;
    }

}
