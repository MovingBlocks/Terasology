// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.events.FootstepEvent;
import org.terasology.engine.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.engine.logic.characters.events.JumpEvent;
import org.terasology.engine.logic.characters.events.OnEnterBlockEvent;
import org.terasology.engine.logic.characters.events.SwimStrokeEvent;
import org.terasology.engine.logic.characters.events.VerticalCollisionEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.engine.CharacterCollider;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.engine.physics.engine.SweepCallback;
import org.terasology.engine.physics.events.MovedEvent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.math.TeraMath;

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
    public static final float HORIZONTAL_PENETRATION_LEEWAY = 0.04f;

    /**
     * The amount of extra distance added to vertical movement to allow for penetration.
     */
    public static final float VERTICAL_PENETRATION_LEEWAY = 0.05f;
    private static final float CHECK_FORWARD_DIST = 0.05f;

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
                //TODO: Only the character height is considered here, but not other properties denoting the extent.
                //      The CharacterMovementComponent also has a 'radius' which may be used here.
                //      Question: Is this connected with _shapes_ or bounding boxes in some way?
                checkBlockEntry(entity,
                        new Vector3i(initial.getPosition(), RoundingMode.HALF_UP),
                        new Vector3i(result.getPosition(), RoundingMode.HALF_UP),
                        characterMovementComponent.height);
            }
            if (result.getMode() != MovementMode.GHOSTING && result.getMode() != MovementMode.NONE) {
                checkMode(characterMovementComponent, result, initial, entity, input.isFirstRun(), input.isCrouching());
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
        // TODO: This will only work for mobs/players with a ground area of a single block as horizontal extents are
        //       not considered.
        //       Maybe the simple `characterHeight` value should be replaced by a vector of extents, or rather provide
        //       a method overload to allow for both.
        //          private void checkBlockEntry(EntityRef entity, Vector3i oldPosition, Vector3i newPosition, Vector3f characterExtents) {

        if (!oldPosition.equals(newPosition)) {
            int characterHeightInBlocks = (int) Math.ceil(characterHeight);

            // get the old position's blocks
            Block[] oldBlocks = new Block[characterHeightInBlocks];
            for (int y = 0; y < characterHeightInBlocks; y++) {
                oldBlocks[y] = worldProvider.getBlock(oldPosition.x, oldPosition.y + y, oldPosition.z);
            }

            // get the new position's blocks
            Block[] newBlocks = new Block[characterHeightInBlocks];
            for (int y = 0; y < characterHeightInBlocks; y++) {
                newBlocks[y] = worldProvider.getBlock(newPosition.x, newPosition.y + y, newPosition.z);
            }

            for (int y = 0; y < characterHeightInBlocks; y++) {
                // send a block enter/leave event for this character
                entity.send(new OnEnterBlockEvent(oldBlocks[y], newBlocks[y], new Vector3i(0, y, 0)));
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
                           final CharacterStateEvent oldState, EntityRef entity, boolean firstRun, boolean isCrouching) {
        //If we are ghosting or we can't move, the mode cannot be changed.
        if (!state.getMode().respondToEnvironment) {
            return;
        }
        Vector3f worldPos = state.getPosition();
        Vector3f top = new Vector3f(worldPos);
        Vector3f bottom = new Vector3f(worldPos);
        top.y += 0.5f * movementComp.height;
        bottom.y -= 0.25f * movementComp.height;

        final boolean topUnderwater = worldProvider.getBlock(top).isLiquid();
        final boolean bottomUnderwater = worldProvider.getBlock(bottom).isLiquid();

        //We check if either a single point is in water (SWIMMING) or if both points are in water (DIVING).
        final boolean newSwimming = !topUnderwater && bottomUnderwater || topUnderwater && !bottomUnderwater;
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

        updateMode(state, newSwimming, newDiving, newClimbing, isCrouching);
    }

    /**
     * Updates a character's movement mode and changes his vertical velocity accordingly.
     *
     * @param state       The current state of the character.
     * @param newSwimming True if the top of the character's body isn't in a liquid block but his bottom is.
     * @param newDiving   True if the character's body is fully inside liquid blocks.
     * @param newClimbing True if the character has a climbable block near him and is in conditions to climb it (not swimming or diving).
     */
    static void updateMode(CharacterStateEvent state, boolean newSwimming, boolean newDiving, boolean newClimbing, boolean isCrouching) {
        if (newDiving) {
            if (state.getMode() != MovementMode.DIVING) {
                state.setMode(MovementMode.DIVING);
            }
        } else if (newSwimming) {
            if (state.getMode() != MovementMode.SWIMMING) {
                state.setMode(MovementMode.SWIMMING);
            }
            state.getVelocity().y += 0.02f;
        } else if (state.getMode() == MovementMode.SWIMMING || state.getMode() == MovementMode.DIVING) {
            if (newClimbing) {
                state.setMode(MovementMode.CLIMBING);
                state.getVelocity().y = 0;
            } else {
                if (state.getVelocity().y > 0) {
                    state.getVelocity().y += 4;
                }
                state.setMode(isCrouching ? MovementMode.CROUCHING : MovementMode.WALKING);
            }
        } else if (newClimbing != (state.getMode() == MovementMode.CLIMBING)) {
            //We need to toggle the climbing mode
            state.getVelocity().y = 0;
            state.setMode((newClimbing) ? MovementMode.CLIMBING : isCrouching ? MovementMode.CROUCHING : MovementMode.WALKING);
        }
        if (state.getMode() == MovementMode.WALKING || state.getMode() == MovementMode.CROUCHING) {
            state.setMode(isCrouching ? MovementMode.CROUCHING : MovementMode.WALKING);
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
                Vector3i myPos = new Vector3i(worldPos, org.joml.RoundingMode.HALF_UP);
                Vector3i climbBlockPos = new Vector3i(side, org.joml.RoundingMode.HALF_UP);
                Vector3i dir = new Vector3i(block.getDirection().direction());
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
            Vector3f reflectDir = direction.reflect(hitNormal, new Vector3f());
            reflectDir.normalize();

            Vector3f perpendicularDir = hitNormal.mul(reflectDir.dot(hitNormal), new Vector3f()).mul(-1).add(reflectDir);
            if (normalMag != 0.0f) {
                Vector3f perpComponent = new Vector3f(perpendicularDir);
                perpComponent.mul(normalMag * movementLength);
                direction.set(perpComponent);
            }
        }
        return direction;
    }

    private void followToParent(final CharacterStateEvent state, EntityRef entity) {
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        if (!locationComponent.getParent().equals(EntityRef.NULL)) {
            Vector3f position = locationComponent.getWorldPosition(new Vector3f());
            Vector3f velocity = new Vector3f(position);
            velocity.sub(state.getPosition());
            state.getVelocity().set(velocity);
            state.getPosition().set(position);
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
                expectedMove.mul(actualDist);
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
                        expectedMove.mul(-remainingDist / expectedMove.y + HORIZONTAL_PENETRATION_LEEWAY);
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
        Vector3f normalizedDir = horizMove.normalize();
        if (!normalizedDir.isFinite()) {
            normalizedDir.set(0);
        }

        if (collider == null) {
            // ignore collision
            normalizedDir.mul(dist);
            position.add(normalizedDir);
            return false;
        }

        Vector3f targetPos = new Vector3f(normalizedDir);
        targetPos.mul(dist + HORIZONTAL_PENETRATION_LEEWAY);
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
                    actualMove.mul(actualDist);
                    position.add(actualMove);
                }
                dist -= actualDist;
                Vector3f newDir = new Vector3f(normalizedDir);
                newDir.mul(dist);
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
                    newDir.mul(newHorizDir.length() / modHorizDir.length());
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
                targetPos.mul(dist + HORIZONTAL_PENETRATION_LEEWAY);
                targetPos.add(position);
            } else {
                normalizedDir.mul(dist);
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
            result.getRotation().setAngleAxis(yaw, 0, 1, 0);
        } else {
            result.getRotation().set(new Quaternionf().rotationYXZ(Math.toRadians(input.getYaw()), 0, 0));
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
        desiredVelocity.mul(movementComp.speedMultiplier);

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
                desiredVelocity.mul(speed);
            }
        }
        desiredVelocity.mul(maxSpeed);

        if (movementComp.mode == MovementMode.CLIMBING) {
            climb(state, input, desiredVelocity);
        }

        // If swimming or diving, cancel double jump to avoid jumping underwater and on the surface
        if (movementComp.mode == MovementMode.SWIMMING || movementComp.mode == MovementMode.DIVING) {
            movementComp.numberOfJumpsLeft = 0;
        }

        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(state.getVelocity());
        velocityDiff.mul(Math.min(movementComp.mode.scaleInertia * input.getDelta(), 1.0f));
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
        moveDelta.mul(input.getDelta());
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

        // Upon hitting solid ground, reset the number of jumps back to the maximum value.
        if (state.isGrounded()) {
            movementComp.numberOfJumpsLeft = movementComp.numberOfJumpsMax;
        }

        if (moveResult.isBottomHit()) {
            if (!state.isGrounded() && movementComp.mode.canBeGrounded) {
                if (input.isFirstRun()) {
                    Vector3f landVelocity = new Vector3f(state.getVelocity());
                    landVelocity.y += (distanceMoved.y / moveDelta.y) * (endVelocity.y - state.getVelocity().y);
                    entity.send(new VerticalCollisionEvent(state.getPosition(), landVelocity));
                }
                state.setGrounded(true);
                movementComp.numberOfJumpsLeft = movementComp.numberOfJumpsMax;
            }
            endVelocity.y = 0;

            // Jumping is only possible, if the entity is standing on ground
            if (input.isJumping() && state.isGrounded()) {

                state.setGrounded(false);

                // Send event to allow for other systems to modify the jump force.
                AffectJumpForceEvent affectJumpForceEvent = new AffectJumpForceEvent(movementComp.jumpSpeed);
                entity.send(affectJumpForceEvent);
                endVelocity.y += affectJumpForceEvent.getResultValue();
                if (input.isFirstRun()) {
                    entity.send(new JumpEvent());
                }

                // Send event to allow for other systems to modify the max number of jumps.
                AffectMultiJumpEvent affectMultiJumpEvent = new AffectMultiJumpEvent(movementComp.baseNumberOfJumpsMax);
                entity.send(affectMultiJumpEvent);
                movementComp.numberOfJumpsMax = (int) affectMultiJumpEvent.getResultValue();

                movementComp.numberOfJumpsLeft--;
            }
        } else {
            if (moveResult.isTopHit() && endVelocity.y > 0) {
                if (input.isFirstRun()) {
                    Vector3f hitVelocity = new Vector3f(state.getVelocity());
                    hitVelocity.y += (distanceMoved.y / moveDelta.y) * (endVelocity.y - state.getVelocity().y);
                    entity.send(new VerticalCollisionEvent(state.getPosition(), hitVelocity));
                }
                endVelocity.y = -0.0f * endVelocity.y;
            }

            // Jump again in mid-air only if a jump was requested and there are jumps remaining.
            if (input.isJumping() && movementComp.numberOfJumpsLeft > 0) {
                state.setGrounded(false);

                // Send event to allow for other systems to modify the jump force.
                AffectJumpForceEvent affectJumpForceEvent = new AffectJumpForceEvent(movementComp.jumpSpeed);
                entity.send(affectJumpForceEvent);
                endVelocity.y += affectJumpForceEvent.getResultValue();
                if (input.isFirstRun()) {
                    entity.send(new JumpEvent());
                }

                // Send event to allow for other systems to modify the max number of jumps.
                AffectMultiJumpEvent affectMultiJumpEvent = new AffectMultiJumpEvent(movementComp.baseNumberOfJumpsMax);
                entity.send(affectMultiJumpEvent);
                movementComp.numberOfJumpsMax = (int) affectMultiJumpEvent.getResultValue();

                movementComp.numberOfJumpsLeft--;
            }

            if (state.isGrounded()) {
                movementComp.numberOfJumpsLeft--;
                state.setGrounded(false);
            }
        }
        if (input.isFirstRun() && moveResult.isHorizontalHit()) {
            Vector3f hitVelocity = new Vector3f(state.getVelocity());
            hitVelocity.x += (distanceMoved.x / moveDelta.x) * (endVelocity.x - state.getVelocity().x);
            hitVelocity.z += (distanceMoved.z / moveDelta.z) * (endVelocity.z - state.getVelocity().z);
            entity.send(new HorizontalCollisionEvent(state.getPosition(), hitVelocity));
        }
        state.getVelocity().set(endVelocity);
        if (state.isGrounded() || movementComp.mode == MovementMode.SWIMMING || movementComp.mode == MovementMode.DIVING) {
            state.setFootstepDelta(
                    state.getFootstepDelta() + distanceMoved.length() / movementComp.distanceBetweenFootsteps);
            if (state.getFootstepDelta() > 1) {
                state.setFootstepDelta(state.getFootstepDelta() - 1);
                if (input.isFirstRun()) {
                    switch (movementComp.mode) {
                        case CROUCHING:
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
        Vector3f climbDir3f = new Vector3f(climbDir3i);

        Quaternionf rotation = new Quaternionf().rotationYXZ(TeraMath.DEG_TO_RAD * state.getYaw(), 0, 0);
        tmp = new Vector3f(0.0f, 0.0f, -1.0f);
        tmp.rotate(rotation);
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
                rotation.rotationYXZ(TeraMath.DEG_TO_RAD * state.getYaw(), TeraMath.DEG_TO_RAD * pitch, 0);
                desiredVelocity.rotate(rotation);
            }

            // looking sidewards from ladder
        } else if (angleToClimbDirection < Math.PI * 3.0 / 4.0) {
            float rollAmount = state.isGrounded() ? 45f : 90f;
            tmp = new Vector3f();
            climbDir3f.rotate(rotation, tmp);
            float leftOrRight = tmp.x;
            float plusOrMinus = (leftOrRight < 0f ? -1.0f : 1.0f) * (climbDir3i.x != 0 ? -1.0f : 1.0f);
            if (jumpOrCrouchActive) {
                rotation.rotationY(TeraMath.DEG_TO_RAD * state.getYaw());
            } else {
                rotation.rotationYXZ(TeraMath.DEG_TO_RAD * input.getYaw(), 0f,
                        TeraMath.DEG_TO_RAD * rollAmount * plusOrMinus);
            }
            desiredVelocity.rotate(rotation);
            // facing away from ladder
        } else {
            rotation.rotationYXZ(TeraMath.DEG_TO_RAD * state.getYaw(), 0, 0);
            desiredVelocity.rotate(rotation);
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
