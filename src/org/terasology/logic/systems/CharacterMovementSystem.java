package org.terasology.logic.systems;

import com.google.common.collect.Lists;
import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.FootstepEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.logic.world.WorldUtil;
import org.terasology.math.TeraMath;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.BlockPosition;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class CharacterMovementSystem {

    public static final float UnderwaterGravity = 2.0f;
    public static final float Gravity = 28.0f;
    public static final float TerminalVelocity = 64.0f;
    public static final float UnderwaterInteria = 2.0f;

    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    
    public void update(float delta) {
        float deltaSeconds = (delta / 1000f);

        for (EntityRef entity : entityManager.iteratorEntities(CharacterMovementComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            AABBCollisionComponent collision = entity.getComponent(AABBCollisionComponent.class);
            CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);

            updatePosition(deltaSeconds, entity, location, collision, movementComp);
            //checkPosition(location, collision, movementComp);
            //updateSwimStatus(location, collision, movementComp);
        }
    }

    public void setEntityManager(EntityManager em) {
        this.entityManager = em;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setWorldProvider(IWorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    public IWorldProvider getWorldProvider() {
        return worldProvider;
    }

    protected void updatePosition(float delta, EntityRef entity, LocationComponent location, AABBCollisionComponent collision, CharacterMovementComponent movementComp) {

        // TODO: Better swimming support, flying support
        Vector3f desiredVelocity = new Vector3f(movementComp.drive);
        float maxSpeed = movementComp.isSwimming ? movementComp.maxWaterSpeed : movementComp.maxGroundSpeed;
        if (movementComp.isRunning) {
            maxSpeed *= movementComp.runFactor;
        }
        desiredVelocity.scale(maxSpeed);

        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(movementComp.velocity);
        velocityDiff.y = 0;
        float changeMag = velocityDiff.length();
        if (changeMag > movementComp.groundFriction * delta) {
            velocityDiff.scale(movementComp.groundFriction * delta / changeMag);
        }

        movementComp.velocity.x += velocityDiff.x;
        movementComp.velocity.z += velocityDiff.z;
        
        // Cannot control y if not swimming (or flying I guess)
        if (movementComp.isSwimming) {
            movementComp.velocity.y += Math.signum(desiredVelocity.y - movementComp.velocity.y) * Math.min(UnderwaterInteria * delta, TeraMath.fastAbs(desiredVelocity.y - movementComp.velocity.y));
            movementComp.velocity.y = Math.max(-movementComp.maxWaterSpeed, (movementComp.velocity.y - UnderwaterGravity * delta));
        }
        else {
            movementComp.velocity.y = Math.max(-TerminalVelocity, (float)(movementComp.velocity.y - Gravity * delta));
        }

        // TODO: replace this with swept collision based on JBullet?
        Vector3f worldPos = LocationHelper.localToWorldPos(location);
        Vector3f oldPos = new Vector3f(worldPos);
        worldPos.y += movementComp.velocity.y * delta;
        
        Vector3f extents = new Vector3f(collision.extents);
        extents.scale(LocationHelper.totalScale(location));

        if (verticalHitTest(worldPos, oldPos, extents)) {
            movementComp.velocity.y = 0;

            // Jumping is only possible, if the entity is standing on ground
            if (movementComp.jump) {
                // TODO: Sounds
                //AudioManager.getInstance().playVaryingPositionedSound(calcEntityPositionRelativeToPlayer(),
                //        _footstepSounds[TeraMath.fastAbs(_parent.getWorldProvider().getRandom().randomInt()) % 5]);
                movementComp.jump = false;
                movementComp.isGrounded = false;
                movementComp.velocity.y += movementComp.jumpSpeed;
            } else if (!movementComp.isGrounded) { // Entity reaches the ground
                // TODO: Event on collide (for damage)
                //AudioManager.getInstance().playVaryingPositionedSound(calcEntityPositionRelativeToPlayer(),
                //        _footstepSounds[TeraMath.fastAbs(_parent.getWorldProvider().getRandom().randomInt()) % 5]);
                movementComp.isGrounded = true;
            }
        } else {
            movementComp.isGrounded = false;
        }

        oldPos.set(worldPos);

        /*
         * Update the position of the entity
         * according to the acceleration vector.
         */
        worldPos.x += movementComp.velocity.x * delta;
        worldPos.z += movementComp.velocity.z * delta;

        // TODO: step sound support (while not animation driven anyhow)
        //_stepCounter += java.lang.Math.max(TeraMath.fastAbs(_movementVelocity.x * timePassedInSeconds), TeraMath.fastAbs(_movementVelocity.z * timePassedInSeconds));

        /*
         * Check for horizontal collisions __after__ checking for vertical
         * collisions.
         */
        if (horizontalHitTest(worldPos, oldPos, extents)) {
            entity.send(new HorizontalCollisionEvent());
        }
        movementComp.velocity.x = (worldPos.x - oldPos.x) / delta;
        movementComp.velocity.z = (worldPos.z - oldPos.z) / delta;

        Vector3f dist = new Vector3f(worldPos.x - oldPos.x, 0, worldPos.z - oldPos.z);

        if (movementComp.isGrounded) {
            movementComp.footstepDelta += dist.length();
            if (movementComp.footstepDelta > movementComp.distanceBetweenFootsteps) {
                movementComp.footstepDelta -= movementComp.distanceBetweenFootsteps;
                entity.send(new FootstepEvent());
            }
        }
        location.position.set(LocationHelper.worldToLocalPos(location, worldPos));

        if (movementComp.faceMovementDirection) {
            float yaw = (float)Math.atan2(movementComp.velocity.x, movementComp.velocity.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
            location.rotation.set(axisAngle);
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
            AABB entityAABB = calcAABB(position, extents);

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

                moved = true;
            }
        }

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
        return new AABB(new Vector3d(position), new Vector3d(extents));
    }

}
