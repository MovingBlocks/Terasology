package org.terasology.physics.character;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Lists;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.FootstepEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.JumpEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3fUtil;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;

import javax.vecmath.*;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class BulletCharacterMovementSystem implements UpdateSubscriberSystem, EventHandlerSystem {

    private static final float VERTICAL_PENETRATION_LEEWAY = 0.05f;

    public static final float Gravity = 28.0f;
    public static final float TerminalVelocity = 64.0f;

    public static final float UnderwaterGravity = 0.25f;
    public static final float UnderwaterInertia = 2.0f;
    public static final float WaterTerminalVelocity = 4.0f;

    public static final float GhostInertia = 4f;

    private Logger logger = Logger.getLogger(getClass().getName());

    private EntityManager entityManager;
    private WorldProvider worldProvider;
    private BulletPhysics physics;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        physics = CoreRegistry.get(BulletPhysics.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onDestroy(RemovedComponentEvent event, EntityRef entity) {
        CharacterMovementComponent comp = entity.getComponent(CharacterMovementComponent.class);
        if (comp.collider != null) {
            physics.removeCollider(comp.collider);
        }
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (!worldProvider.isBlockActive(location.getWorldPosition())) continue;

            CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);

            if (movementComp.collider == null) {
                float height = (movementComp.height - 2 * movementComp.radius) * location.getWorldScale();
                float width = movementComp.radius * location.getWorldScale();
                ConvexShape capsule = new CapsuleShape(width, height);
                movementComp.collider = physics.createCollider(location.getWorldPosition(), capsule, Lists.<CollisionGroup>newArrayList(movementComp.collisionGroup), movementComp.collidesWith, CollisionFlags.CHARACTER_OBJECT);
                movementComp.collider.setUserPointer(entity);
                capsule.setMargin(0.05f);
            }

            updatePosition(delta, entity, location, movementComp);
            updateSwimStatus(location, movementComp);

            entity.saveComponent(location);
            entity.saveComponent(movementComp);
        }
    }

    /**
     * Updates whether a character is underwater. A higher and lower point of the character is tested for being in water,
     * only if both points are in water does the character count as swimming.
     *
     * @param location
     * @param movementComp
     */
    private void updateSwimStatus(LocationComponent location, CharacterMovementComponent movementComp) {
        Vector3f worldPos = location.getWorldPosition();
        boolean topUnderwater = false;
        boolean bottomUnderwater = false;
        Vector3f top = new Vector3f(worldPos);
        Vector3f bottom = new Vector3f(worldPos);
        top.y += 0.25f * movementComp.height;
        bottom.y -= 0.25f * movementComp.height;

        topUnderwater = worldProvider.getBlock(top).isLiquid();
        bottomUnderwater = worldProvider.getBlock(bottom).isLiquid();
        boolean newSwimming = topUnderwater && bottomUnderwater;

        // Boost when leaving water
        if (!newSwimming && movementComp.isSwimming && movementComp.getVelocity().y > 0) {
            float len = movementComp.getVelocity().length();
            movementComp.getVelocity().scale((len + 8) / len);
        }
        movementComp.isSwimming = newSwimming;
    }

    private void updatePosition(float delta, EntityRef entity, LocationComponent location, CharacterMovementComponent movementComp) {
        if (movementComp.isGhosting) {
            ghost(delta, location, movementComp);
        } else if (movementComp.isSwimming) {
            swim(delta, entity, location, movementComp);
        } else {
            walk(delta, entity, location, movementComp);
        }
    }

    private void swim(float delta, EntityRef entity, LocationComponent location, CharacterMovementComponent movementComp) {
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
        velocityDiff.scale(Math.min(UnderwaterInertia * delta, 1.0f));

        movementComp.getVelocity().x += velocityDiff.x;
        movementComp.getVelocity().y += velocityDiff.y;
        movementComp.getVelocity().z += velocityDiff.z;

        // Slow down due to friction
        float speed = movementComp.getVelocity().length();
        if (speed > movementComp.maxWaterSpeed) {
            movementComp.getVelocity().scale((speed - 4 * (speed - movementComp.maxWaterSpeed) * delta) / speed);
        }

        Vector3f moveDelta = new Vector3f(movementComp.getVelocity());
        moveDelta.scale(delta);

        // Note: No stepping underwater
        MoveResult moveResult = move(location.getWorldPosition(), moveDelta, 0, movementComp.collider);
        Vector3f distanceMoved = new Vector3f(moveResult.finalPosition);
        distanceMoved.sub(location.getWorldPosition());

        location.setWorldPosition(moveResult.finalPosition);
        movementComp.collider.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), moveResult.finalPosition, 1.0f)));

        if (movementComp.faceMovementDirection && distanceMoved.lengthSquared() > 0.01f) {
            float yaw = (float) Math.atan2(distanceMoved.x, distanceMoved.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            location.getLocalRotation().set(axisAngle);
        }
    }

    private void ghost(float delta, LocationComponent location, CharacterMovementComponent movementComp) {
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
        movementComp.collider.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), worldPos, 1.0f)));

        if (movementComp.faceMovementDirection && movementComp.getVelocity().lengthSquared() > 0.01f) {
            float yaw = (float) Math.atan2(movementComp.getVelocity().x, movementComp.getVelocity().z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            location.getLocalRotation().set(axisAngle);
        }
    }

    private void walk(float delta, EntityRef entity, LocationComponent location, CharacterMovementComponent movementComp) {
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

        velocityDiff.scale(Math.min(movementComp.groundFriction * delta, 1.0f));

        movementComp.getVelocity().x += velocityDiff.x;
        movementComp.getVelocity().z += velocityDiff.z;
        movementComp.getVelocity().y = Math.max(-TerminalVelocity, (movementComp.getVelocity().y - Gravity * delta));

        Vector3f moveDelta = new Vector3f(movementComp.getVelocity());
        moveDelta.scale(delta);

        MoveResult moveResult = move(location.getWorldPosition(), moveDelta, movementComp.stepHeight, movementComp.collider);
        Vector3f distanceMoved = new Vector3f(moveResult.finalPosition);
        distanceMoved.sub(location.getWorldPosition());

        location.setWorldPosition(moveResult.finalPosition);
        movementComp.collider.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), moveResult.finalPosition, 1.0f)));

        if (moveResult.hitBottom) {
            if (!movementComp.isGrounded) {
                entity.send(new VerticalCollisionEvent(movementComp.getVelocity(), moveResult.finalPosition));
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
            if (moveResult.hitTop && movementComp.getVelocity().y > 0) {
                movementComp.getVelocity().y = -0.5f * movementComp.getVelocity().y;
            }
            movementComp.isGrounded = false;
            movementComp.jump = false;
        }

        if (moveResult.hitHoriz) {
            entity.send(new HorizontalCollisionEvent());
        }

         if (movementComp.isGrounded) {
             movementComp.footstepDelta += distanceMoved.length();
             if (movementComp.footstepDelta > movementComp.distanceBetweenFootsteps) {
                 movementComp.footstepDelta -= movementComp.distanceBetweenFootsteps;
                 entity.send(new FootstepEvent());
             }
         }

         if (movementComp.faceMovementDirection && distanceMoved.lengthSquared() > 0.01f) {
             float yaw = (float) Math.atan2(distanceMoved.x, distanceMoved.z);
             AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
             location.getLocalRotation().set(axisAngle);
         }
    }

    private static class MoveResult {
        public Vector3f finalPosition;
        public boolean hitHoriz = false;
        public boolean hitBottom = false;
        public boolean hitTop = false;
    }

    private MoveResult move(Vector3f startPosition, Vector3f moveDelta, float stepHeight, GhostObject collider) {
        MoveResult result = new MoveResult();
        Vector3f position = new Vector3f(startPosition);
        result.finalPosition = position;

        float steppedUpDist = moveUp((moveDelta.y > 0) ? moveDelta.y : 0, stepHeight, collider, result, position);
        result.hitHoriz = moveHorizontal(new Vector3f(moveDelta.x, 0, moveDelta.z), collider, position);
        moveDown((moveDelta.y < 0) ? moveDelta.y : 0, steppedUpDist, collider, result, position);
        return result;
    }

    private boolean moveHorizontal(Vector3f horizMove, GhostObject collider, Vector3f position) {
        float remainingFraction = 1.0f;
        boolean horizontalHit = false;
        Vector3f normalizedDir = Vector3fUtil.safeNormalize(horizMove, new Vector3f());
        Vector3f targetPos = new Vector3f(position);
        targetPos.add(horizMove);
        int iteration = 0;
        while (remainingFraction >= 0.01f && iteration < 10) {
            SweepCallback callback = sweep(position, targetPos, collider, iteration++ * 0.005f);

            /* Note: this isn't quite correct (after the first iteration the closestHitFraction is only for part of the moment)
               but probably close enough */
            remainingFraction -= callback.closestHitFraction;
            if (callback.hasHit()) {
                Vector3f actualMove = new Vector3f();
                actualMove.sub(targetPos, position);
                actualMove.scale(Math.max(0, callback.closestHitFraction));
                position.add(actualMove);
                if (callback.hitNormalWorld.dot(new Vector3f(0,1,0)) < 0.5f)  {
                    horizontalHit = true;
                }

                extractResidualMovement(callback.hitNormalWorld, targetPos, position);

                Vector3f currentDir = new Vector3f();
                currentDir.sub(targetPos, position);
                float sqrDist = currentDir.lengthSquared();
                if (sqrDist > BulletGlobals.SIMD_EPSILON) {
                    currentDir.normalize();
                    if (currentDir.dot(normalizedDir) <= 0.0f) {
                        break;
                    }
                } else {
                    break;
                }

            } else {
                position.set(targetPos);
            }
        }
        return horizontalHit;
    }

    private void moveDown(float fallAmount, float stepDownAmount, GhostObject collider, MoveResult result, Vector3f position) {
        float stepDist = -stepDownAmount + fallAmount;
        {
            SweepCallback callback = sweep(position, new Vector3f(position.x, position.y + stepDist - VERTICAL_PENETRATION_LEEWAY, position.z), collider, VERTICAL_PENETRATION_LEEWAY);
            if (callback.hasHit()) {
                float actualDist = Math.min(0, (stepDist - VERTICAL_PENETRATION_LEEWAY) * callback.closestHitFraction + VERTICAL_PENETRATION_LEEWAY);
                position.y += actualDist;
                if (fallAmount < 0) {
                    result.hitBottom = true;
                }
            } else {
                position.y += stepDist;
            }
        }
    }

    private float moveUp(float riseAmount, float stepHeight, GhostObject collider, MoveResult result, Vector3f position) {
        float vertDist = riseAmount + stepHeight;
        float steppedUpDist = 0;

        SweepCallback callback = sweep(position, new Vector3f(position.x, position.y + vertDist + VERTICAL_PENETRATION_LEEWAY, position.z), collider, VERTICAL_PENETRATION_LEEWAY);

        if (callback.hasHit()) {
            float actualDist = Math.max(0, ((vertDist + VERTICAL_PENETRATION_LEEWAY) * callback.closestHitFraction) - VERTICAL_PENETRATION_LEEWAY);
            position.y += actualDist;
            if (actualDist <= riseAmount) {
                result.hitTop = true;
            } else {
                steppedUpDist = actualDist - riseAmount;
            }
        } else {
            position.y += vertDist;
            steppedUpDist = stepHeight;
        }
        return steppedUpDist;
    }

    private SweepCallback sweep(Vector3f from, Vector3f to, GhostObject collider, float allowedPenetration) {
        Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), from, 1.0f));
        Transform endTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), to, 1.0f));
        SweepCallback callback = new SweepCallback(collider, new Vector3f(0, 1, 0), -1.0f);
        callback.collisionFilterGroup = collider.getBroadphaseHandle().collisionFilterGroup;
        callback.collisionFilterMask = collider.getBroadphaseHandle().collisionFilterMask;

        collider.convexSweepTest((ConvexShape) (collider.getCollisionShape()), startTransform, endTransform, callback, allowedPenetration);
        return callback;
    }

    private static class SweepCallback extends CollisionWorld.ClosestConvexResultCallback {
        protected CollisionObject me;
        protected final Vector3f up;
        protected float minSlopeDot;

        public SweepCallback(CollisionObject me, final Vector3f up, float minSlopeDot) {
            super(new Vector3f(), new Vector3f());
            this.me = me;
            this.up = up;
            this.minSlopeDot = minSlopeDot;
        }

        @Override
        public float addSingleResult(CollisionWorld.LocalConvexResult convexResult, boolean normalInWorldSpace) {
            if (convexResult.hitCollisionObject == me) {
                return 1.0f;
            }

            Vector3f hitNormalWorld;
            if (normalInWorldSpace) {
                hitNormalWorld = convexResult.hitNormalLocal;
            } else {
                //need to transform normal into worldspace
                hitNormalWorld = new Vector3f();
                hitCollisionObject.getWorldTransform(new Transform()).basis.transform(convexResult.hitNormalLocal, hitNormalWorld);
            }

            float dotUp = up.dot(hitNormalWorld);
            if (dotUp < minSlopeDot) {
                return 1.0f;
            }

            return super.addSingleResult(convexResult, normalInWorldSpace);
        }
    }

    private Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f targetPos, Vector3f currentPos) {
        return extractResidualMovement(hitNormal, targetPos, currentPos, 1f);
    }

    private Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f targetPos, Vector3f currentPos, float normalMag) {
        Vector3f movementDirection = new Vector3f();
        movementDirection.sub(targetPos, currentPos);
        float movementLength = movementDirection.length();
        if (movementLength > BulletGlobals.SIMD_EPSILON) {
            movementDirection.normalize();

            Vector3f reflectDir = Vector3fUtil.reflect(movementDirection, hitNormal, new Vector3f());
            reflectDir.normalize();

            Vector3f perpindicularDir = Vector3fUtil.getPerpendicularComponent(reflectDir, hitNormal, new Vector3f());

            targetPos.set(currentPos);

            if (normalMag != 0.0f) {
                Vector3f perpComponent = new Vector3f();
                perpComponent.scale(normalMag * movementLength, perpindicularDir);
                targetPos.add(perpComponent);
            }
        }
        return targetPos;
    }





}
