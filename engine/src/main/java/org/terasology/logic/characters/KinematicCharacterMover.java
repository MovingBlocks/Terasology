/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.characters;

import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.events.FootstepEvent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.JumpEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.characters.events.SwimStrokeEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3fUtil;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.engine.CharacterCollider;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.physics.engine.SweepCallback;
import org.terasology.physics.events.MovedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * Calculates character movement using a physics-engine provided CharacterCollider.
 * This collider is swept through the world to detect collisions.
 * The general process for this is:
 * <ol>
 * <li>If moving upwards, sweep up</li>
 * <li>Sweep sideways</li>
 * <li>If an obstacle is hit, step up and resume sideways motion</li>
 * <li>If an slope is hit, slide up it</li>
 * <li>Finally sweep downwards to undo any stepping, and for falling</li>
 * </ol>
 * <br><br>
 * TODO: Refactor to allow additional movement modes.
 * TODO: Detect entry and exit from water while ghosting.
 *
 */
public class KinematicCharacterMover implements CharacterMover {

    public static final float GRAVITY = 28.0f;
    public static final float TERMINAL_VELOCITY = 64.0f;

    /**
     * The amount of horizontal penetration to allow.
     */
    public static final float HORIZONTAL_PENETRATION = 0.03f;

    /**
     * The amount of vertical penetration to allow.
     */
    public static final float VERTICAL_PENETRATION = 0.04f;

    /**
     * The amount of extra distance added to horizontal movement to allow for penetration.
     */
    private static final float HORIZONTAL_PENETRATION_LEEWAY = 0.04f;

    /**
     * The amount of extra distance added to vertical movement to allow for penetration.
     */
    private static final float VERTICAL_PENETRATION_LEEWAY = 0.05f;
    private static final float CHECK_FORWARD_DIST = 0.05f;

    private static final Logger logger = LoggerFactory.getLogger(KinematicCharacterMover.class);
    private boolean stepped;

    // Processing state variables
    private float steppedUpDist;
    private WorldProvider worldProvider;
    private PhysicsEngine physics;

    public KinematicCharacterMover(WorldProvider wp, PhysicsEngine physicsEngine) {
        this.worldProvider = wp;
        physics = physicsEngine;
    }

    @Override
    public CharacterStateEvent step(CharacterStateEvent initial, CharacterMoveInputEvent input, EntityRef entity) {
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        CharacterStateEvent result = new CharacterStateEvent(initial);
        result.setSequenceNumber(input.getSequenceNumber());
        if (worldProvider.isBlockRelevant(initial.getPosition())) {
            updatePosition(characterMovementComponent, result, input, entity);

            if (input.isFirstRun()) {
                checkBlockEntry(entity,
                        new Vector3i(initial.getPosition(), RoundingMode.HALF_UP),
                        new Vector3i(result.getPosition(), RoundingMode.HALF_UP),
                        characterMovementComponent.height);
            }

            if (result.getMode() != MovementMode.GHOSTING && result.getMode() != MovementMode.NONE) {
                checkMode(characterMovementComponent, result, initial, entity, input.isFirstRun());
            }
        }
        result.setTime(initial.getTime() + input.getDeltaMs());
        updateRotation(characterMovementComponent, result, input);
        result.setPitch(input.getPitch());
        result.setYaw(input.getYaw());
        input.runComplete();
        return result;
    }

    private float getMaxSpeed(EntityRef character, CharacterMovementComponent characterMovement) {
        GetMaxSpeedEvent speedEvent = new GetMaxSpeedEvent(characterMovement.mode.maxSpeed, characterMovement.mode);
        character.send(speedEvent);
        return Math.max(0, speedEvent.getResultValue());
    }

    /*
    * Figure out if our position has put us into a new set of blocks and fire the appropriate events.
    */
    private void checkBlockEntry(EntityRef entity, Vector3i oldPosition, Vector3i newPosition, float characterHeight) {
        // TODO: This will only work for tall mobs/players and single block mobs
        // is this a different position than previously
        if (!oldPosition.equals(newPosition)) {
            // get the old position's blocks
            Block[] oldBlocks = new Block[(int) Math.ceil(characterHeight)];
            Vector3i currentPosition = new Vector3i(oldPosition);
            for (int currentHeight = 0; currentHeight < oldBlocks.length; currentHeight++) {
                oldBlocks[currentHeight] = worldProvider.getBlock(currentPosition);
                currentPosition.add(0, 1, 0);
            }

            // get the new position's blocks
            Block[] newBlocks = new Block[(int) Math.ceil(characterHeight)];
            currentPosition = new Vector3i(newPosition);
            for (int currentHeight = 0; currentHeight < characterHeight; currentHeight++) {
                newBlocks[currentHeight] = worldProvider.getBlock(currentPosition);
                currentPosition.add(0, 1, 0);
            }

            for (int i = 0; i < characterHeight; i++) {
                // send a block enter/leave event for this character
                entity.send(new OnEnterBlockEvent(oldBlocks[i], newBlocks[i], new Vector3i(0, i, 0)));
            }
        }
    }

    /**
     * Checks whether a character should change movement mode (from being underwater or in a ladder). A higher and lower point of the
     * character is tested for being in water, only if both points are in water does the character count as swimming.
     * <br><br>
     * Sends the OnEnterLiquidEvent and OnLeaveLiquidEvent events.
     *
     * @param movementComp The movement component of the character.
     * @param state        The current state of the character.
     */
    private void checkMode(final CharacterMovementComponent movementComp, final CharacterStateEvent state,
                           final CharacterStateEvent oldState, EntityRef entity, boolean firstRun) {
        //If we are ghosting or we can't move, the mode cannot be changed.
        if (!state.getMode().respondToEnvironment) {
            return;
        }

        Vector3f worldPos = state.getPosition();
        Vector3f top = new Vector3f(worldPos);
        Vector3f bottom = new Vector3f(worldPos);
        top.y += 0.5f * movementComp.height;
        bottom.y -= 0.5f * movementComp.height;

        final boolean topUnderwater = worldProvider.getBlock(top).isLiquid();
        final boolean bottomUnderwater = worldProvider.getBlock(bottom).isLiquid();

        final boolean newSwimming = !topUnderwater && bottomUnderwater;
        final boolean newDiving = topUnderwater && bottomUnderwater;
        boolean newClimbing = false;

        if (isClimbingAllowed(newSwimming, newDiving)) {
            Vector3i finalDir;
            finalDir = findClimbable(movementComp, worldPos, newSwimming, newDiving);
            if (finalDir != null) {
                newClimbing = true;
                state.setClimbDirection(finalDir);
            }
        }

        updateMode(state, newSwimming, newDiving, newClimbing);
    }

    /**
     * Updates a character's movement mode and changes his vertical velocity accordingly.
     * @param state The current state of the character.
     * @param newSwimming True if the top of the character's body isn't in a liquid block but his bottom is.
     * @param newDiving True if the character's body is fully inside liquid blocks.
     * @param newClimbing True if the character has a climbable block near him and is in conditions to climb it (not swimming or diving).
     */
    static void updateMode(CharacterStateEvent state, boolean newSwimming, boolean newDiving, boolean newClimbing) {
        if (newDiving) {
            if (state.getMode() != MovementMode.DIVING) {
                state.setMode(MovementMode.DIVING);
            }
        } else if (newSwimming) {
            if (state.getMode() != MovementMode.SWIMMING) {
                state.setMode(MovementMode.SWIMMING);
            }
            state.getVelocity().y += 0.02;
        } else if (state.getMode() == MovementMode.SWIMMING || state.getMode() == MovementMode.DIVING) {
            if (newClimbing) {
                state.setMode(MovementMode.CLIMBING);
                state.getVelocity().y = 0;
            } else {
                if (state.getVelocity().y > 0) {
                    state.getVelocity().y += 4;
                }
                state.setMode(MovementMode.WALKING);
            }
        } else if (newClimbing != (state.getMode() == MovementMode.CLIMBING)) {
            //We need to toggle the climbing mode
            state.getVelocity().y = 0;
            state.setMode((newClimbing) ? MovementMode.CLIMBING : MovementMode.WALKING);
        }
    }

    private Vector3i findClimbable(CharacterMovementComponent movementComp, Vector3f worldPos, boolean swimming, boolean diving) {
        Vector3i finalDir = null;
        Vector3f[] sides = {new Vector3f(worldPos), new Vector3f(worldPos), new Vector3f(worldPos), new Vector3f(
                worldPos), new Vector3f(worldPos)};
        float factor = 1.0f;
        sides[0].x += factor * movementComp.radius;
        sides[1].x -= factor * movementComp.radius;
        sides[2].z += factor * movementComp.radius;
        sides[3].z -= factor * movementComp.radius;
        sides[4].y -= movementComp.height;

        float distance = 100f;

        for (Vector3f side : sides) {
            Block block = worldProvider.getBlock(side);
            if (block.isClimbable()) {
                //If any of our sides are near a climbable block, check if we are near to the side
                Vector3i myPos = new Vector3i(worldPos, RoundingMode.HALF_UP);
                Vector3i climbBlockPos = new Vector3i(side, RoundingMode.HALF_UP);
                Vector3i dir = new Vector3i(block.getDirection().getVector3i());
                float currentDistance = 10f;

                if (dir.x != 0 && Math.abs(worldPos.x - climbBlockPos.x + dir.x * .5f) < movementComp.radius + 0.1f) {
                    if (myPos.x < climbBlockPos.x) {
                        dir.x = -dir.x;
                    }
                    currentDistance = Math.abs(climbBlockPos.z - worldPos.z);

                } else if (dir.z != 0 && Math.abs(worldPos.z - climbBlockPos.z + dir.z * .5f) < movementComp.radius + 0.1f) {
                    if (myPos.z < climbBlockPos.z) {
                        dir.z = -dir.z;
                    }
                    currentDistance = Math.abs(climbBlockPos.z - worldPos.z);
                }

                // if there are multiple climb blocks, choose the nearest one. This can happen when there are two
                // adjacent ledges around a corner.
                if (currentDistance < distance) {
                    distance = currentDistance;
                    finalDir = dir;
                }
            }
        }
        return finalDir;
    }

    private boolean isClimbingAllowed(boolean swimming, boolean diving) {
        return !swimming && !diving;
    }

    /**
     * Checks of the player will step up to an object. In a single movement step the player can only step up a single item.
     *
     * @param collider
     * @param position
     * @param direction
     * @param callback
     * @param slopeFactor
     * @param stepHeight
     * @return
     */
    private boolean checkStep(CharacterCollider collider, Vector3f position, Vector3f direction, SweepCallback callback,
                              float slopeFactor, float stepHeight) {
        if (!stepped) {
            stepped = true;

            boolean moveUpStep = callback.checkForStep(direction, stepHeight, slopeFactor, CHECK_FORWARD_DIST);

            if (moveUpStep) {
                steppedUpDist = moveUp(stepHeight, collider, position);
                return true;
            }
        }
        return false;
    }

    private Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f direction) {
        return extractResidualMovement(hitNormal, direction, 1f);
    }

    private Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f direction, float normalMag) {
        float movementLength = direction.length();
        if (movementLength > physics.getEpsilon()) {
            direction.normalize();
            Vector3f reflectDir = Vector3fUtil.reflect(direction, hitNormal, new Vector3f());
            reflectDir.normalize();
            Vector3f perpendicularDir = Vector3fUtil.getPerpendicularComponent(reflectDir, hitNormal, new Vector3f());
            if (normalMag != 0.0f) {
                Vector3f perpComponent = new Vector3f(perpendicularDir);
                perpComponent.scale(normalMag * movementLength);
                direction.set(perpComponent);
            }
        }
        return direction;
    }

    private void followToParent(final CharacterStateEvent state, EntityRef entity) {
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        if (!locationComponent.getParent().equals(EntityRef.NULL)) {
            Vector3f velocity = new Vector3f(locationComponent.getWorldPosition());
            velocity.sub(state.getPosition());
            state.getVelocity().set(velocity);
            state.getPosition().set(locationComponent.getWorldPosition());
        }
    }

    private MoveResult move(final Vector3f startPosition, final Vector3f moveDelta, final float stepHeight,
                            final float slopeFactor, final CharacterCollider collider) {
        steppedUpDist = 0;
        stepped = false;
        Vector3f position = new Vector3f(startPosition);
        boolean hitTop = false;
        boolean hitBottom = false;
        boolean hitSide;

        // Actual upwards movement
        if (moveDelta.y > 0) {
            hitTop = moveDelta.y - moveUp(moveDelta.y, collider, position) > physics.getEpsilon();
        }
        hitSide = moveHorizontal(new Vector3f(moveDelta.x, 0, moveDelta.z), collider, position, slopeFactor, stepHeight);
        if (moveDelta.y < 0 || steppedUpDist > 0) {
            float dist = (moveDelta.y < 0) ? moveDelta.y : 0;
            dist -= steppedUpDist;
            hitBottom = moveDown(dist, slopeFactor, collider, position);
        }
        if (!hitBottom && stepHeight > 0) {
            Vector3f tempPos = new Vector3f(position);
            hitBottom = moveDown(-stepHeight, slopeFactor, collider, tempPos);
            // Don't apply step down if nothing to step onto
            if (hitBottom) {
                position.set(tempPos);
            }
        }
        return new MoveResult(position, hitSide, hitBottom, hitTop);
    }

    private boolean moveDown(float dist, float slopeFactor, CharacterCollider collider, Vector3f position) {
        if (collider == null) {
            position.y += dist;
            return false;
        }

        float remainingDist = -dist;
        Vector3f targetPos = new Vector3f(position);
        targetPos.y -= remainingDist + VERTICAL_PENETRATION_LEEWAY;
        Vector3f normalizedDir = new Vector3f(0, -1, 0);
        boolean hit = false;
        int iteration = 0;
        while (remainingDist > physics.getEpsilon() && iteration++ < 10) {
            SweepCallback callback = collider.sweep(position, targetPos, VERTICAL_PENETRATION, -1.0f);
            float actualDist = Math.max(0,
                    (remainingDist + VERTICAL_PENETRATION_LEEWAY) * callback.getClosestHitFraction() - VERTICAL_PENETRATION_LEEWAY);
            Vector3f expectedMove = new Vector3f(targetPos);
            expectedMove.sub(position);
            if (expectedMove.lengthSquared() > physics.getEpsilon()) {
                expectedMove.normalize();
                expectedMove.scale(actualDist);
                position.add(expectedMove);
            }
            remainingDist -= actualDist;
            if (remainingDist < physics.getEpsilon()) {
                break;
            }
            if (callback.hasHit()) {
                float originalSlope = callback.getHitNormalWorld().dot(new Vector3f(0, 1, 0));
                if (originalSlope < slopeFactor) {
                    float slope = callback.calculateAverageSlope(originalSlope, CHECK_FORWARD_DIST);
                    if (slope < slopeFactor) {
                        remainingDist -= actualDist;
                        expectedMove.set(targetPos);
                        expectedMove.sub(position);
                        extractResidualMovement(callback.getHitNormalWorld(), expectedMove);
                        float sqrDist = expectedMove.lengthSquared();
                        if (sqrDist > physics.getEpsilon()) {
                            expectedMove.normalize();
                            if (expectedMove.dot(normalizedDir) <= 0.0f) {
                                hit = true;
                                break;
                            }
                        } else {
                            hit = true;
                            break;
                        }
                        if (expectedMove.y > -physics.getEpsilon()) {
                            hit = true;
                            break;
                        }
                        normalizedDir.set(expectedMove);
                        expectedMove.scale(-remainingDist / expectedMove.y + HORIZONTAL_PENETRATION_LEEWAY);
                        targetPos.set(position);
                        targetPos.add(expectedMove);
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

    private boolean moveHorizontal(Vector3f horizMove, CharacterCollider collider, Vector3f position, float slopeFactor,
                                   float stepHeight) {
        float remainingFraction = 1.0f;
        float dist = horizMove.length();
        if (dist < physics.getEpsilon()) {
            return false;
        }
        boolean horizontalHit = false;
        Vector3f normalizedDir = Vector3fUtil.safeNormalize(horizMove, new Vector3f());

        if (collider == null) {
            // ignore collision
            normalizedDir.scale(dist);
            position.add(normalizedDir);
            return false;
        }

        Vector3f targetPos = new Vector3f(normalizedDir);
        targetPos.scale(dist + HORIZONTAL_PENETRATION_LEEWAY);
        targetPos.add(position);
        int iteration = 0;
        Vector3f lastHitNormal = new Vector3f(0, 1, 0);
        while (remainingFraction >= 0.01f && iteration++ < 10) {
            SweepCallback callback = collider.sweep(position, targetPos, HORIZONTAL_PENETRATION, slopeFactor);

            /* Note: this isn't quite correct (after the first iteration the closestHitFraction is only for part of the moment)
             but probably close enough */
            float actualDist = Math.max(0,
                    (dist + HORIZONTAL_PENETRATION_LEEWAY) * callback.getClosestHitFraction() - HORIZONTAL_PENETRATION_LEEWAY);
            if (actualDist != 0) {
                remainingFraction -= actualDist / dist;
            }
            if (callback.hasHit()) {
                if (actualDist > physics.getEpsilon()) {
                    Vector3f actualMove = new Vector3f(normalizedDir);
                    actualMove.scale(actualDist);
                    position.add(actualMove);
                }
                dist -= actualDist;
                Vector3f newDir = new Vector3f(normalizedDir);
                newDir.scale(dist);
                float slope = callback.getHitNormalWorld().dot(new Vector3f(0, 1, 0));

                // We step up if we're hitting a big slope, or if we're grazing
                // the ground, otherwise we move up a shallow slope.
                if (slope < slopeFactor || 1 - slope < physics.getEpsilon()) {
                    boolean stepping = checkStep(collider, position, newDir, callback, slopeFactor, stepHeight);
                    if (!stepping) {
                        horizontalHit = true;
                        Vector3f newHorizDir = new Vector3f(newDir.x, 0, newDir.z);
                        Vector3f horizNormal = new Vector3f(callback.getHitNormalWorld().x, 0,
                                callback.getHitNormalWorld().z);
                        if (horizNormal.lengthSquared() > physics.getEpsilon()) {
                            horizNormal.normalize();
                            if (lastHitNormal.dot(horizNormal) > physics.getEpsilon()) {
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
                    extractResidualMovement(callback.getHitNormalWorld(), newDir);
                    Vector3f modHorizDir = new Vector3f(newDir);
                    modHorizDir.y = 0;
                    newDir.scale(newHorizDir.length() / modHorizDir.length());
                }
                float sqrDist = newDir.lengthSquared();
                if (sqrDist > physics.getEpsilon()) {
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

    private float moveUp(float riseAmount, CharacterCollider collider, Vector3f position) {
        Vector3f to = new Vector3f(position.x, position.y + riseAmount + VERTICAL_PENETRATION_LEEWAY, position.z);
        if (collider != null) {
            SweepCallback callback = collider.sweep(position, to, VERTICAL_PENETRATION_LEEWAY, -1f);
            if (callback.hasHit()) {
                float actualDist = Math.max(0,
                        ((riseAmount + VERTICAL_PENETRATION_LEEWAY) * callback.getClosestHitFraction()) - VERTICAL_PENETRATION_LEEWAY);
                position.y += actualDist;
                return actualDist;
            }
        }
        position.y += riseAmount;
        return riseAmount;
    }

    private void updatePosition(final CharacterMovementComponent movementComp, final CharacterStateEvent state,
                                CharacterMoveInputEvent input, EntityRef entity) {
        switch (state.getMode()) {
            case NONE:
                followToParent(state, entity);
                break;
            default:
                walk(movementComp, state, input, entity);
                break;
        }
    }

    @SuppressWarnings(value = "SuspiciousNameCombination")
    private void updateRotation(CharacterMovementComponent movementComp, CharacterStateEvent result,
                                CharacterMoveInputEvent input) {
        if (movementComp.faceMovementDirection && result.getVelocity().lengthSquared() > 0.01f) {
            float yaw = (float) Math.atan2(result.getVelocity().x, result.getVelocity().z);
            result.getRotation().set(new Vector3f(0, 1, 0), yaw);
        } else {
            result.getRotation().set(new Quat4f(TeraMath.DEG_TO_RAD * input.getYaw(), 0, 0));
        }
    }

    private void walk(final CharacterMovementComponent movementComp, final CharacterStateEvent state,
                      CharacterMoveInputEvent input, EntityRef entity) {
        Vector3f desiredVelocity = new Vector3f(input.getMovementDirection());

        float lengthSquared = desiredVelocity.lengthSquared();

        // If the length of desired movement is > 1, normalise it to prevent movement being faster than allowed.
        // (Desired velocity < 1 is allowed, as the character may wish to walk/crawl/otherwise move slowly)
        if (lengthSquared > 1) {
            desiredVelocity.normalize();
        }
        desiredVelocity.scale(movementComp.speedMultiplier);

        float maxSpeed = getMaxSpeed(entity, movementComp);
        if (input.isRunning()) {
            maxSpeed *= movementComp.runFactor;
        }

        // As we can't use it, remove the y component of desired movement while maintaining speed.
        if (movementComp.grounded && desiredVelocity.y != 0) {
            float speed = desiredVelocity.length();
            desiredVelocity.y = 0;
            if (desiredVelocity.x != 0 || desiredVelocity.z != 0) {
                desiredVelocity.normalize();
                desiredVelocity.scale(speed);
            }
        }
        desiredVelocity.scale(maxSpeed);

        if (movementComp.mode == MovementMode.CLIMBING) {
            climb(state, input, desiredVelocity);
        }

        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(state.getVelocity());
        velocityDiff.scale(Math.min(movementComp.mode.scaleInertia * input.getDelta(), 1.0f));
        Vector3f endVelocity = new Vector3f(state.getVelocity());
        endVelocity.x += velocityDiff.x;
        endVelocity.z += velocityDiff.z;
        if (movementComp.mode.scaleGravity == 0) {
            // apply the velocity without gravity
            endVelocity.y += velocityDiff.y;
        } else if (movementComp.mode.applyInertiaToVertical) {
            endVelocity.y += Math.max(-TERMINAL_VELOCITY, velocityDiff.y - (GRAVITY * movementComp.mode.scaleGravity) * input.getDelta());
        } else {
            endVelocity.y = Math.max(-TERMINAL_VELOCITY, state.getVelocity().y - (GRAVITY * movementComp.mode.scaleGravity) * input.getDelta());
        }
        Vector3f moveDelta = new Vector3f(endVelocity);
        moveDelta.scale(input.getDelta());
        CharacterCollider collider = movementComp.mode.useCollision ? physics.getCharacterCollider(entity) : null;
        MoveResult moveResult = move(state.getPosition(), moveDelta,
                (state.getMode() != MovementMode.CLIMBING && state.isGrounded() && movementComp.mode.canBeGrounded) ? movementComp.stepHeight : 0,
                movementComp.slopeFactor, collider);
        Vector3f distanceMoved = new Vector3f(moveResult.getFinalPosition());
        distanceMoved.sub(state.getPosition());
        state.getPosition().set(moveResult.getFinalPosition());
        if (input.isFirstRun() && distanceMoved.length() > 0) {
            entity.send(new MovedEvent(distanceMoved, state.getPosition()));
        }

        if (moveResult.isBottomHit()) {
            if (!state.isGrounded() && movementComp.mode.canBeGrounded) {
                if (input.isFirstRun()) {
                    Vector3f landVelocity = new Vector3f(state.getVelocity());
                    landVelocity.y += (distanceMoved.y / moveDelta.y) * (endVelocity.y - state.getVelocity().y);
                    logger.debug("Landed at " + landVelocity);
                    entity.send(new VerticalCollisionEvent(state.getPosition(), landVelocity));
                }
                state.setGrounded(true);
            }
            endVelocity.y = 0;

            // Jumping is only possible, if the entity is standing on ground
            if (input.isJumpRequested()) {
                state.setGrounded(false);
                endVelocity.y += movementComp.jumpSpeed;
                if (input.isFirstRun()) {
                    entity.send(new JumpEvent());
                }
            }
        } else {
            if (moveResult.isTopHit() && endVelocity.y > 0) {
                endVelocity.y = -0.5f * endVelocity.y;
            }
            state.setGrounded(false);
        }
        state.getVelocity().set(endVelocity);
        if (input.isFirstRun() && moveResult.isHorizontalHit()) {
            entity.send(new HorizontalCollisionEvent(state.getPosition(), state.getVelocity()));
        }
        if (state.isGrounded() || movementComp.mode == MovementMode.SWIMMING || movementComp.mode == MovementMode.DIVING) {
            state.setFootstepDelta(
                    state.getFootstepDelta() + distanceMoved.length() / movementComp.distanceBetweenFootsteps);
            if (state.getFootstepDelta() > 1) {
                state.setFootstepDelta(state.getFootstepDelta() - 1);
                if (input.isFirstRun()) {
                    switch (movementComp.mode) {
                        case WALKING:
                            entity.send(new FootstepEvent());
                            break;
                        case DIVING:
                        case SWIMMING:
                            entity.send(new SwimStrokeEvent(worldProvider.getBlock(state.getPosition())));
                            break;
                        case CLIMBING:
                        case FLYING:
                        case GHOSTING:
                        case NONE:
                            break;
                    }
                }
            }
        }
    }

    private void climb(final CharacterStateEvent state, CharacterMoveInputEvent input, Vector3f desiredVelocity) {
        if (state.getClimbDirection() == null) {
            return;
        }
        Vector3f tmp;

        Vector3i climbDir3i = state.getClimbDirection();
        Vector3f climbDir3f = climbDir3i.toVector3f();

        Quat4f rotation = new Quat4f(TeraMath.DEG_TO_RAD * state.getYaw(), 0, 0);
        tmp = new Vector3f(0.0f, 0.0f, -1.0f);
        rotation.rotate(tmp, tmp);
        float angleToClimbDirection = tmp.angle(climbDir3f);

        boolean clearMovementToDirection = !state.isGrounded();
        boolean jumpOrCrouchActive = desiredVelocity.y != 0;

        // facing the ladder or looking down or up
        if (angleToClimbDirection < Math.PI / 4.0 || Math.abs(input.getPitch()) > 60f) {
            if (jumpOrCrouchActive) {
                desiredVelocity.x = 0;
                desiredVelocity.z = 0;
                clearMovementToDirection = false;
            } else {
                float pitchAmount = state.isGrounded() ? 45f : 90f;
                float pitch = input.getPitch() > 30f ? pitchAmount : -pitchAmount;
                rotation = new Quat4f(TeraMath.DEG_TO_RAD * state.getYaw(), TeraMath.DEG_TO_RAD * pitch, 0);
                rotation.rotate(desiredVelocity, desiredVelocity);
            }

            // looking sidewards from ladder
        } else if (angleToClimbDirection < Math.PI * 3.0 / 4.0) {
            float rollAmount = state.isGrounded() ? 45f : 90f;
            tmp = new Vector3f();
            rotation.rotate(climbDir3f, tmp);
            float leftOrRight = tmp.x;
            float plusOrMinus = (leftOrRight < 0f ? -1.0f : 1.0f) * (climbDir3i.x != 0 ? -1.0f : 1.0f);
            if (jumpOrCrouchActive) {
                rotation = new Quat4f(TeraMath.DEG_TO_RAD * state.getYaw(), 0, 0);
            } else {
                rotation = new Quat4f(TeraMath.DEG_TO_RAD * input.getYaw(), 0f,
                        TeraMath.DEG_TO_RAD * rollAmount * plusOrMinus
                );
            }
            rotation.rotate(desiredVelocity, desiredVelocity);

            // facing away from ladder
        } else {
            rotation = new Quat4f(TeraMath.DEG_TO_RAD * state.getYaw(), 0, 0);
            rotation.rotate(desiredVelocity, desiredVelocity);
            clearMovementToDirection = false;
        }

        // clear out movement towards or away from the ladder
        if (clearMovementToDirection) {
            if (climbDir3i.x != 0) {
                desiredVelocity.x = 0f;
            }
            if (climbDir3i.z != 0) {
                desiredVelocity.z = 0f;
            }
        }
    }

    /**
     * Holds the result of movement.
     */
    public static class MoveResult {

        private Vector3f finalPosition;
        private boolean horizontalHit;
        private boolean bottomHit;
        private boolean topHit;

        public MoveResult(Vector3f finalPosition, boolean hitHorizontal, boolean hitBottom, boolean hitTop) {
            this.finalPosition = finalPosition;
            this.horizontalHit = hitHorizontal;
            this.bottomHit = hitBottom;
            this.topHit = hitTop;
        }

        public Vector3f getFinalPosition() {
            return finalPosition;
        }

        public boolean isHorizontalHit() {
            return horizontalHit;
        }

        public boolean isBottomHit() {
            return bottomHit;
        }

        public boolean isTopHit() {
            return topHit;
        }
    }
}
