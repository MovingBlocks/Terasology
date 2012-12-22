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
package org.terasology.functional.componentsystem.controllers;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;
import org.terasology.functional.components.LocomotiveComponent;
import org.terasology.physics.HitResult;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.*;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.Vector3fUtil;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import javax.vecmath.*;

/**
 * @author Pencilcheck <pennsu@gmail.com>
 */
@RegisterComponentSystem
public final class LocomotiveSystem implements UpdateSubscriberSystem, EventHandlerSystem {
    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BulletPhysics physics;

    private static final Logger logger = LoggerFactory.getLogger(LocomotiveSystem.class);


    /**
     * The amount of extra distance added to vertical movement to allow for penetration.
     */
    private static final float VERTICAL_PENETRATION_LEEWAY = 0.05f;
    /**
     * The amount of vertical penetration to allow.
     */
    private static final float VERTICAL_PENETRATION = 0.04f;
    /**
     * The amount of extra distance added to horizontal movement to allow for penentration.
     */
    private static final float HORIZONTAL_PENETRATION_LEEWAY = 0.04f;
    /**
     * The amount of horizontal penetration to allow.
     */
    private static final float HORIZONTAL_PENETRATION = 0.03f;

    public static final float GRAVITY = 28.0f;
    public static final float TERMINAL_VELOCITY = 64.0f;

    public static final float GROUND_FRICTION = 9.0f;

    public static final float UNDERWATER_GRAVITY = 0.25f;
    public static final float UNDERWATER_INERTIA = 2.0f;
    public static final float WATER_TERMINAL_VELOCITY = 4.0f;

    private static final float CHECK_FORWARD_DIST = 0.05f;


    private float steppedUpDist = 0;
    private boolean stepped = false;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {LocomotiveComponent.class, LocationComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        logger.info("Activating Locomotive System");

        LocomotiveComponent loco = entity.getComponent(LocomotiveComponent.class);
        loco.toggle();
        entity.saveComponent(loco);
    }

    @ReceiveEvent(components = {LocomotiveComponent.class, LocationComponent.class})
    public void onDestroy(final RemovedComponentEvent event, final EntityRef entity) {
        LocomotiveComponent comp = entity.getComponent(LocomotiveComponent.class);
        if (comp.collider != null) {
            physics.removeCollider(comp.collider);
        }
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(LocomotiveComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            // Skip this System if not in a loaded chunk
            if (!worldProvider.isBlockActive(worldPos)) {
                continue;
            }

            LocomotiveComponent loco = entity.getComponent(LocomotiveComponent.class);

            if (loco.collider == null) {
                BoxShapeComponent shape  = entity.getComponent(BoxShapeComponent.class);
                BoxShape box = new BoxShape(shape.extents);
                loco.collider = physics.createCollider(location.getWorldPosition(), box, Lists.<CollisionGroup>newArrayList(loco.collisionGroup), loco.collidesWith, CollisionFlags.CHARACTER_OBJECT);
                loco.collider.setUserPointer(entity);
                continue;
            }

            if (!localPlayer.isValid())
                return;

            if (loco.locomotiveType == LocomotiveComponent.LocomotiveType.Boat) {
                goodToFloat(entity, location, loco);
                floating(delta, entity, location, loco);
            } else if (loco.locomotiveType == LocomotiveComponent.LocomotiveType.Train) {
                goodToSlide(entity, location, loco);
                sliding(delta, entity, location, loco);
            }

            entity.saveComponent(loco);
        }
    }

    public boolean standingOn(EntityRef entity) {
        // Only move when someone is standing on it
        HitResult hit = physics.rayTrace(localPlayer.getPosition(), new Vector3f(0, -1, 0), 1.6f * 0.25f);
        boolean standingOn = hit.isHit() && hit.getEntity() == entity;
        //logger.info("Player standing on functional {}", standingOn);
        return standingOn;
    }

    public void updatePositions(Vector3f distanceMoved, EntityRef entity, LocationComponent location, LocationComponent player_location, LocomotiveComponent loco) {
        Vector3f position = location.getWorldPosition();
        position.add(distanceMoved);
        location.setWorldPosition(position);
        entity.saveComponent(location);

        if (loco.shouldMove) {
            distanceMoved.scale(1f); // Player moves slower than the block
            Vector3f player_position = player_location.getWorldPosition();
            player_position.add(distanceMoved);
            player_location.setWorldPosition(player_position);
            localPlayer.getEntity().saveComponent(player_location);
        }
    }

    public boolean goodToFloat(EntityRef entity, LocationComponent location, LocomotiveComponent loco) {
        Vector3f top_position = location.getWorldPosition();
        Vector3f bottom_position = location.getWorldPosition();
        top_position.y += .12f;
        bottom_position.y -= .12f;
        Block top_block = worldProvider.getBlock(top_position);
        Block bottom_block = worldProvider.getBlock(bottom_position);

        loco.shouldMove = top_block.isPenetrable() && !top_block.isLiquid() && bottom_block.isLiquid() && standingOn(entity);

        if (!loco.shouldMove) {
            loco.currentVelocity.x = 0;
            loco.currentVelocity.y = 0;
            loco.currentVelocity.z = 0;
        }

        if ((!top_block.isLiquid() && !bottom_block.isLiquid()) && top_block.isPenetrable() && bottom_block.isPenetrable()) {
            loco.currentVelocity.x = 0;
            loco.currentVelocity.y -= GRAVITY;
            loco.currentVelocity.z = 0;
            loco.shouldMove = false;
        }

        if ((top_block.isLiquid() && bottom_block.isLiquid()) && top_block.isPenetrable() && bottom_block.isPenetrable()) {
            loco.currentVelocity.x = 0;
            loco.currentVelocity.y += UNDERWATER_GRAVITY;
            loco.currentVelocity.z = 0;
            loco.shouldMove = false;
        }

        //logger.info("Boat shouldMove {}, liquid: top {}, bottom {}", loco.shouldMove, top_block.isLiquid(), bottom_block.isLiquid());
        return loco.shouldMove;
    }

    public void floating(float delta, EntityRef entity, LocationComponent location, LocomotiveComponent loco) {
        if (loco.shouldMove) {
            // Move forward
            Vector3f movementDirection = localPlayer.getViewDirection();
            float speed = movementDirection.length();
            movementDirection = new Vector3f(movementDirection.x, 0, movementDirection.z);
            movementDirection.normalize();
            movementDirection.scale(speed);

            Vector3f desiredVelocity = new Vector3f(movementDirection);
            desiredVelocity.scale(loco.getMaximumSpeed());

            // TODO: Should be floating, oscillating
            // Modify velocity towards desired, up to the maximum rate determined by friction
            Vector3f velocityDiff = new Vector3f(desiredVelocity);
            velocityDiff.sub(loco.currentVelocity);
            velocityDiff.scale(Math.min(UNDERWATER_INERTIA * delta, 1.0f));


            loco.currentVelocity.x += velocityDiff.x;
            loco.currentVelocity.y += velocityDiff.y;
            loco.currentVelocity.z += velocityDiff.z;

            // Slow down due to friction
            speed = loco.currentVelocity.length();
            if (speed > loco.getMaximumSpeed()) {
                loco.currentVelocity.scale((speed - 4 * (speed - loco.getMaximumSpeed()) * delta) / speed);
            }
        }

        Vector3f moveDelta = new Vector3f(loco.currentVelocity);
        moveDelta.scale(delta);

        // Note: No stepping underwater, no issue with slopes
        MoveResult moveResult = move(location.getWorldPosition(), moveDelta, 0, -1, loco.collider);
        Vector3f distanceMoved = new Vector3f(moveResult.finalPosition);
        distanceMoved.sub(location.getWorldPosition());

        if (!loco.shouldMove) {
            distanceMoved = new Vector3f(loco.currentVelocity);
        }

        loco.collider.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), moveResult.finalPosition, 1.0f)));
        /*
        if (movementComp.faceMovementDirection && distanceMoved.lengthSquared() > 0.01f) {
            float yaw = (float) Math.atan2(distanceMoved.x, distanceMoved.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            location.getLocalRotation().set(axisAngle);
        }
        */


        updatePositions(distanceMoved, entity, location, localPlayer.getEntity().getComponent(LocationComponent.class), loco);
    }

    public boolean goodToSlide(EntityRef entity, LocationComponent location, LocomotiveComponent loco) {
        Vector3f top_position = location.getWorldPosition();
        Vector3f middle_position = location.getWorldPosition();
        Vector3f bottom_position = location.getWorldPosition();
        top_position.y += .12f;
        middle_position.y -= .08f;
        bottom_position.y -= .12f;
        Block top_block = worldProvider.getBlock(top_position);
        Block middle_block = worldProvider.getBlock(middle_position);
        Block bottom_block = worldProvider.getBlock(bottom_position);

        loco.shouldMove = top_block.isPenetrable() && middle_block.isPenetrable() && standingOn(entity);
        logger.info("Train shouldMove {}, penetrable: top {}, middle {}, bottom {}", loco.shouldMove, top_block.isPenetrable(), middle_block.isPenetrable(), bottom_block.isPenetrable());

        if (!loco.shouldMove) {
            loco.currentVelocity.x = 0;
            loco.currentVelocity.y = 0;
            loco.currentVelocity.z = 0;
            logger.info("Train resetting");
        }

        // Try to fix it
        if (!middle_block.isPenetrable() || !bottom_block.isPenetrable()) {
            loco.currentVelocity.x = 0;
            loco.currentVelocity.y = 1f;
            loco.currentVelocity.z = 0;
            loco.shouldMove = false;
            logger.info("Train fixing");
        }

        return loco.shouldMove;


        /*
        HitResult ground_hit = physics.rayTrace(location.getWorldPosition(), new Vector3f(0, -1, 0), 32);
        loco.shouldMove = ground_hit.isHit();
        if (ground_hit.isHit()) {
            Block block = worldProvider.getBlock(ground_hit.getBlockPosition());
            loco.shouldMove = !block.isLiquid() && standingOn(entity);
            logger.info("Train shouldMove {}, when block below is not liquid {}", loco.shouldMove, !block.isLiquid());
        }
        return loco.shouldMove;
        */
    }

    public void sliding(float delta, EntityRef entity, LocationComponent location, LocomotiveComponent loco) {

        if (loco.shouldMove) {
            // Move forward
            Vector3f movementDirection = localPlayer.getViewDirection();
            float speed = movementDirection.length();
            movementDirection = new Vector3f(movementDirection.x, 0, movementDirection.z);
            movementDirection.normalize();
            movementDirection.scale(speed);

            Vector3f desiredVelocity = new Vector3f(movementDirection);
            desiredVelocity.scale(loco.getMaximumSpeed());

            // Modify velocity towards desired, up to the maximum rate determined by friction
            Vector3f velocityDiff = new Vector3f(desiredVelocity);
            velocityDiff.sub(loco.currentVelocity);
            velocityDiff.scale(Math.min(GROUND_FRICTION * delta, 1.0f));

            //logger.info("Sliding with {}", loco.getMaximumSpeed());

            loco.currentVelocity.x += velocityDiff.x;
            loco.currentVelocity.z += velocityDiff.z;
            loco.currentVelocity.y = Math.max(-TERMINAL_VELOCITY, (loco.currentVelocity.y - GRAVITY * delta));
        }

        Vector3f moveDelta = new Vector3f(loco.currentVelocity);
        moveDelta.scale(delta);

        MoveResult moveResult = move(location.getWorldPosition(), moveDelta, loco.stepHeight, loco.slopeFactor, loco.collider);
        Vector3f distanceMoved = new Vector3f(moveResult.finalPosition);
        distanceMoved.sub(location.getWorldPosition());

        if (!loco.shouldMove) {
            distanceMoved = new Vector3f(loco.currentVelocity);
        }
        logger.info("Distance moved {}", distanceMoved);

        loco.collider.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), moveResult.finalPosition, 1.0f)));

        /*
        if (movementComp.faceMovementDirection && distanceMoved.lengthSquared() > 0.01f) {
            float yaw = (float) Math.atan2(distanceMoved.x, distanceMoved.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            location.getLocalRotation().set(axisAngle);
        }
        */

        updatePositions(distanceMoved, entity, location, localPlayer.getEntity().getComponent(LocationComponent.class), loco);
    }


    private static class MoveResult {
        public Vector3f finalPosition;
        public boolean hitHoriz = false;
        public boolean hitBottom = false;
        public boolean hitTop = false;
    }

    private MoveResult move(Vector3f startPosition, Vector3f moveDelta, float stepHeight, float slopeFactor, PairCachingGhostObject collider) {
        steppedUpDist = 0;
        stepped = false;

        MoveResult result = new MoveResult();
        Vector3f position = new Vector3f(startPosition);
        result.finalPosition = position;

        // Actual upwards movement
        if (moveDelta.y > 0) {
            result.hitTop = moveDelta.y - moveUp(moveDelta.y, collider, position) > BulletGlobals.SIMD_EPSILON;
        }
        result.hitHoriz = moveHorizontal(new Vector3f(moveDelta.x, 0, moveDelta.z), collider, position, slopeFactor, stepHeight);
        if (moveDelta.y < 0 || steppedUpDist > 0) {
            float dist = (moveDelta.y < 0) ? moveDelta.y : 0;
            dist -= steppedUpDist;
            result.hitBottom = moveDown(dist, slopeFactor, collider, position);
        }
        if (!result.hitBottom && stepHeight > 0) {
            Vector3f tempPos = new Vector3f(position);
            result.hitBottom = moveDown(-stepHeight, slopeFactor, collider, tempPos);
            // Don't apply step down if nothing to step onto
            if (result.hitBottom) {
                position.set(tempPos);
            }
        }
        return result;
    }

    private boolean moveHorizontal(Vector3f horizMove, PairCachingGhostObject collider, Vector3f position, float slopeFactor, float stepHeight) {
        float remainingFraction = 1.0f;
        float dist = horizMove.length();
        if (dist < BulletGlobals.SIMD_EPSILON) {
            return false;
        }

        boolean horizontalHit = false;
        Vector3f normalizedDir = Vector3fUtil.safeNormalize(horizMove, new Vector3f());
        Vector3f targetPos = new Vector3f(normalizedDir);
        targetPos.scale(dist + HORIZONTAL_PENETRATION_LEEWAY);
        targetPos.add(position);
        int iteration = 0;
        Vector3f lastHitNormal = new Vector3f(0, 1, 0);
        while (remainingFraction >= 0.01f && iteration++ < 10) {
            SweepCallback callback = sweep(position, targetPos, collider, slopeFactor, HORIZONTAL_PENETRATION);

            /* Note: this isn't quite correct (after the first iteration the closestHitFraction is only for part of the moment)
               but probably close enough */
            float actualDist = Math.max(0, (dist + HORIZONTAL_PENETRATION_LEEWAY) * callback.closestHitFraction - HORIZONTAL_PENETRATION_LEEWAY);
            if (actualDist != 0) {
                remainingFraction -= actualDist / dist;
            }
            if (callback.hasHit()) {
                if (actualDist > BulletGlobals.SIMD_EPSILON) {
                    Vector3f actualMove = new Vector3f(normalizedDir);
                    actualMove.scale(actualDist);
                    position.add(actualMove);
                }

                dist -= actualDist;
                Vector3f newDir = new Vector3f(normalizedDir);
                newDir.scale(dist);

                float slope = callback.hitNormalWorld.dot(new Vector3f(0, 1, 0));
                // We step up if we're hitting a big slope, or if we're grazing the ground)
                if (slope < slopeFactor || 1 - slope < BulletGlobals.SIMD_EPSILON) {
                    boolean stepping = checkStep(collider, position, newDir, callback, slopeFactor, stepHeight);
                    if (!stepping) {
                        horizontalHit = true;

                        Vector3f newHorizDir = new Vector3f(newDir.x, 0, newDir.z);
                        Vector3f horizNormal = new Vector3f(callback.hitNormalWorld.x, 0, callback.hitNormalWorld.z);
                        if (horizNormal.lengthSquared() > BulletGlobals.SIMD_EPSILON) {
                            horizNormal.normalize();
                            if (lastHitNormal.dot(horizNormal) > BulletGlobals.SIMD_EPSILON) {
                                break;
                            }
                            lastHitNormal.set(horizNormal);
                            extractResidualMovement(horizNormal, newHorizDir);
                        }

                        newDir.set(newHorizDir);
                    }
                } else {
                    // Hitting a shallow slope, move up it
                    Vector3f newHorizDir = new Vector3f(newDir.x, 0, newDir.z);
                    extractResidualMovement(callback.hitNormalWorld, newDir);
                    Vector3f modHorizDir = new Vector3f(newDir);
                    modHorizDir.y = 0;
                    newDir.scale(newHorizDir.length() / modHorizDir.length());
                }

                float sqrDist = newDir.lengthSquared();
                if (sqrDist > BulletGlobals.SIMD_EPSILON) {
                    newDir.normalize();
                    if (newDir.dot(normalizedDir) <= 0.0f) {
                        break;
                    }
                } else {
                    break;
                }
                dist = (float) Math.sqrt(sqrDist);
                normalizedDir.set(newDir);
                targetPos.set(normalizedDir);
                targetPos.scale(dist + HORIZONTAL_PENETRATION_LEEWAY);
                targetPos.add(position);
            } else {
                normalizedDir.scale(dist);
                position.add(normalizedDir);
                break;
            }
        }
        return horizontalHit;
    }

    private boolean checkStep(PairCachingGhostObject collider, Vector3f position, Vector3f direction, SweepCallback callback, float slopeFactor, float stepHeight) {
        if (!stepped) {
            stepped = true;

            Vector3f lookAheadOffset = new Vector3f(direction);
            lookAheadOffset.y = 0;
            lookAheadOffset.normalize();
            lookAheadOffset.scale(CHECK_FORWARD_DIST);
            boolean hitStep = false;
            float stepSlope = 1f;

            Vector3f fromWorld = new Vector3f(callback.hitPointWorld);
            fromWorld.y += stepHeight + 0.05f;
            fromWorld.add(lookAheadOffset);
            Vector3f toWorld = new Vector3f(callback.hitPointWorld);
            toWorld.y -= 0.05f;
            toWorld.add(lookAheadOffset);
            CollisionWorld.ClosestRayResultCallback rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
            CollisionWorld.rayTestSingle(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f)), new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f)), callback.hitCollisionObject, callback.hitCollisionObject.getCollisionShape(), callback.hitCollisionObject.getWorldTransform(new Transform()), rayResult);
            if (rayResult.hasHit()) {
                hitStep = true;
                stepSlope = rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0));
            }
            fromWorld.add(lookAheadOffset);
            toWorld.add(lookAheadOffset);
            rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
            CollisionWorld.rayTestSingle(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f)), new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f)), callback.hitCollisionObject, callback.hitCollisionObject.getCollisionShape(), callback.hitCollisionObject.getWorldTransform(new Transform()), rayResult);
            if (rayResult.hasHit()) {
                hitStep = true;
                stepSlope = Math.min(stepSlope, rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0)));
            }

            if (hitStep && stepSlope >= slopeFactor) {
                steppedUpDist = moveUp(stepHeight, collider, position);
                return true;
            }
        }
        return false;
    }

    private boolean moveDown(float dist, float slopeFactor, PairCachingGhostObject collider, Vector3f position) {

        float remainingDist = -dist;

        Vector3f targetPos = new Vector3f(position);
        targetPos.y -= remainingDist + VERTICAL_PENETRATION_LEEWAY;
        Vector3f normalizedDir = new Vector3f(0, -1, 0);
        boolean hit = false;

        int iteration = 0;
        while (remainingDist > BulletGlobals.SIMD_EPSILON && iteration++ < 10) {
            SweepCallback callback = sweep(position, targetPos, collider, -1.0f, VERTICAL_PENETRATION);

            float actualDist = Math.max(0, (remainingDist + VERTICAL_PENETRATION_LEEWAY) * callback.closestHitFraction - VERTICAL_PENETRATION_LEEWAY);
            Vector3f expectedMove = new Vector3f(targetPos);
            expectedMove.sub(position);
            if (expectedMove.lengthSquared() > BulletGlobals.SIMD_EPSILON) {
                expectedMove.normalize();
                expectedMove.scale(actualDist);
                position.add(expectedMove);
            }

            remainingDist -= actualDist;
            if (remainingDist < BulletGlobals.SIMD_EPSILON) {
                break;
            }

            if (callback.hasHit()) {
                Vector3f contactPoint = callback.hitPointWorld;
                float originalSlope = callback.hitNormalWorld.dot(new Vector3f(0, 1, 0));
                if (originalSlope < slopeFactor) {
                    float slope = 1;
                    boolean foundSlope = false;

                    // We do two ray traces, and use the steepest, to avoid incongruities with the slopes
                    Vector3f fromWorld = new Vector3f(contactPoint);
                    fromWorld.y += 0.2f;
                    Vector3f toWorld = new Vector3f(contactPoint);
                    toWorld.y -= 0.2f;
                    CollisionWorld.ClosestRayResultCallback rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
                    CollisionWorld.rayTestSingle(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f)), new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f)), callback.hitCollisionObject, callback.hitCollisionObject.getCollisionShape(), callback.hitCollisionObject.getWorldTransform(new Transform()), rayResult);

                    if (rayResult.hasHit()) {
                        foundSlope = true;
                        slope = Math.min(slope, (rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0))));
                    }

                    Vector3f secondTraceOffset = new Vector3f(callback.hitNormalWorld);
                    secondTraceOffset.y = 0;
                    secondTraceOffset.normalize();
                    secondTraceOffset.scale(CHECK_FORWARD_DIST);
                    fromWorld.add(secondTraceOffset);
                    toWorld.add(secondTraceOffset);

                    rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
                    CollisionWorld.rayTestSingle(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f)), new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f)), callback.hitCollisionObject, callback.hitCollisionObject.getCollisionShape(), callback.hitCollisionObject.getWorldTransform(new Transform()), rayResult);

                    if (rayResult.hasHit()) {
                        foundSlope = true;
                        slope = Math.min(slope, (rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0))));
                    }

                    if (!foundSlope) {
                        slope = originalSlope;
                    }

                    if (slope < slopeFactor) {
                        remainingDist -= actualDist;
                        expectedMove.set(targetPos);
                        expectedMove.sub(position);

                        extractResidualMovement(callback.hitNormalWorld, expectedMove);
                        float sqrDist = expectedMove.lengthSquared();
                        if (sqrDist > BulletGlobals.SIMD_EPSILON) {
                            expectedMove.normalize();
                            if (expectedMove.dot(normalizedDir) <= 0.0f) {
                                hit = true;
                                break;
                            }
                        } else {
                            hit = true;
                            break;
                        }
                        if (expectedMove.y > -BulletGlobals.SIMD_EPSILON) {
                            hit = true;
                            break;
                        }
                        normalizedDir.set(expectedMove);

                        expectedMove.scale(-remainingDist / expectedMove.y + HORIZONTAL_PENETRATION_LEEWAY);
                        targetPos.add(position, expectedMove);
                    } else {
                        hit = true;
                        break;
                    }
                } else {
                    hit = true;
                    break;
                }
            } else {
                break;
            }
        }

        if (iteration >= 10) {
            hit = true;
        }

        return hit;
    }

    private float moveUp(float riseAmount, GhostObject collider, Vector3f position) {
        SweepCallback callback = sweep(position, new Vector3f(position.x, position.y + riseAmount + VERTICAL_PENETRATION_LEEWAY, position.z), collider, -1.0f, VERTICAL_PENETRATION_LEEWAY);

        if (callback.hasHit()) {
            float actualDist = Math.max(0, ((riseAmount + VERTICAL_PENETRATION_LEEWAY) * callback.closestHitFraction) - VERTICAL_PENETRATION_LEEWAY);
            position.y += actualDist;
            return actualDist;
        }
        position.y += riseAmount;
        return riseAmount;
    }

    private SweepCallback sweep(Vector3f from, Vector3f to, GhostObject collider, float slopeFactor, float allowedPenetration) {
        Transform startTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), from, 1.0f));
        Transform endTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), to, 1.0f));
        SweepCallback callback = new SweepCallback(collider, new Vector3f(0, 1, 0), slopeFactor);
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

            return super.addSingleResult(convexResult, normalInWorldSpace);
        }
    }

    private Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f direction) {
        return extractResidualMovement(hitNormal, direction, 1f);
    }

    private Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f direction, float normalMag) {
        float movementLength = direction.length();
        if (movementLength > BulletGlobals.SIMD_EPSILON) {
            direction.normalize();

            Vector3f reflectDir = Vector3fUtil.reflect(direction, hitNormal, new Vector3f());
            reflectDir.normalize();

            Vector3f perpindicularDir = Vector3fUtil.getPerpendicularComponent(reflectDir, hitNormal, new Vector3f());


            if (normalMag != 0.0f) {
                Vector3f perpComponent = new Vector3f();
                perpComponent.scale(normalMag * movementLength, perpindicularDir);
                direction.set(perpComponent);
            }
        }
        return direction;
    }

}
