/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terasology.logic.characters.bullet;

import org.terasology.physics.SweepCallback;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.linearmath.QuaternionUtil;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterMover;
import org.terasology.logic.characters.CharacterStateEvent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.FootstepEvent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.JumpEvent;
import org.terasology.logic.characters.events.OnEnterLiquidEvent;
import org.terasology.logic.characters.events.OnLeaveLiquidEvent;
import org.terasology.logic.characters.events.SwimStrokeEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3fUtil;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.physics.CharacterMoverCollider;
import org.terasology.physics.MovedEvent;
import org.terasology.world.WorldProvider;

/**
 * The AbstractCharacterMover generalises the character movement to a physics
 * engine independent class. The physics engine will then only have to fill in
 * several smaller method calls.
 *
 * @author Rednax
 */
public class KinematicCharacterMover implements CharacterMover {

    protected static final float CHECK_FORWARD_DIST = 0.05f;
    public static final float CLIMB_GRAVITY = 0f;
    public static final float GHOST_INERTIA = 4f;
    public static final float GRAVITY = 28.0f;
    /**
     * The amount of horizontal penetration to allow.
     */
    protected static final float HORIZONTAL_PENETRATION = 0.03f;
    /**
     * The amount of extra distance added to horizontal movement to allow for
     * penentration.
     */
    protected static final float HORIZONTAL_PENETRATION_LEEWAY = 0.04f;
    public static final float TERMINAL_VELOCITY = 64.0f;
    public static final float UNDERWATER_GRAVITY = 0.25f;
    public static final float UNDERWATER_INERTIA = 2.0f;
    /**
     * The amount of vertical penetration to allow.
     */
    protected static final float VERTICAL_PENETRATION = 0.04f;
    /**
     * The amount of extra distance added to vertical movement to allow for
     * penetration.
     */
    protected static final float VERTICAL_PENETRATION_LEEWAY = 0.05f;
    //Logger is now based on implementing class:
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected boolean stepped;
    // Processing state variables
    protected float steppedUpDist;
    protected WorldProvider worldProvider;
    private BulletPhysics physics;
    
    public KinematicCharacterMover(WorldProvider wp, BulletPhysics physicsEngine) {
        this.worldProvider = wp;
        physics = physicsEngine;
    }

    /**
     * Updates whether a character should change movement mode (from being
     * underwater or in a ladder). A higher and lower point of the character is
     * tested for being in water, only if both points are in water does the
     * character count as swimming. <br> <br>
     *
     * Sends the OnEnterLiquidEvent and OnLeaveLiquidEvent events.
     *
     * @param movementComp The movement component of the character.
     * @param state The current state of the character.
     */
    protected void checkMode(final CharacterMovementComponent movementComp, final CharacterStateEvent state, final CharacterStateEvent oldState, EntityRef entity, boolean firstRun) {
        //If we are ghosting, the mode cannot be changed!
        if (state.getMode() == MovementMode.GHOSTING) {
            return;
        }

        //Determine top and bottom positions:
        Vector3f worldPos = state.getPosition();
        Vector3f top = new Vector3f(worldPos);
        Vector3f bottom = new Vector3f(worldPos);
        top.y += 0.25f * movementComp.height;
        bottom.y -= 0.25f * movementComp.height;

        //Check if they are under water:
        boolean topUnderwater = worldProvider.getBlock(top).isLiquid();
        boolean bottomUnderwater = worldProvider.getBlock(bottom).isLiquid();

        boolean newSwimming = topUnderwater && bottomUnderwater;
        boolean newClimbing = false;

        //In the new state, we are nor swimming, so we need to check if we will
        //be climbing
        if (!newSwimming) {
            Vector3f[] sides = {new Vector3f(worldPos), new Vector3f(worldPos), new Vector3f(worldPos), new Vector3f(worldPos), new Vector3f(worldPos)};
            float factor = 0.18f;
            sides[0].x += factor * movementComp.radius;
            sides[1].x -= factor * movementComp.radius;
            sides[2].z += factor * movementComp.radius;
            sides[3].z -= factor * movementComp.radius;
            sides[4].y -= movementComp.height;
            for (Vector3f side : sides) {
                if (worldProvider.getBlock(side).isClimbable()) {
                    //If any of our sides are near a climbable block, climb!
                    newClimbing = true;
                    break;
                }
            }
        }

        //Now lets do someting with our newfound info about hte states:
        if (newSwimming) {
            //Note that you cannot climb under water!
            if (state.getMode() != MovementMode.SWIMMING) {
                //Only enter liquid if we were not swimming already!
                if (firstRun) {
                    entity.send(new OnEnterLiquidEvent(worldProvider.getBlock(state.getPosition())));
                }
                state.setMode(MovementMode.SWIMMING);
            }
        } else if (state.getMode() == MovementMode.SWIMMING) {
            //Note that newSwimming is false here! So we are nolonger swimming:
            if (firstRun) {
                entity.send(new OnLeaveLiquidEvent(worldProvider.getBlock(oldState.getPosition())));
            }
            //Leaving liquid and instantly climbing:
            if (newClimbing) {
                state.setMode(MovementMode.CLIMBING);
                state.getVelocity().y = 0;
            } else {
                //Not swimming or climbing? So we must be walking!
                if (state.getVelocity().y > 0) {
                    state.getVelocity().y += 8;
                }
                state.setMode(MovementMode.WALKING);
            }
        } else if (newClimbing != (state.getMode() == MovementMode.CLIMBING)) {
            //We need to toggle the climbing mode
            state.getVelocity().y = 0;
            state.setMode((newClimbing) ? MovementMode.CLIMBING : MovementMode.WALKING);
        } else {
            /*
             * We were walking and continue to do so. No changes, no code!
             */
        }
    }

    /**
     * Checks of the player will step up to an object. In a single movement step
     * the player can only step up a single item.
     *
     * @param collider
     * @param position
     * @param direction
     * @param callback
     * @param slopeFactor
     * @param stepHeight
     * @return
     */
    protected boolean checkStep(CharacterMoverCollider collider, Vector3f position, Vector3f direction, SweepCallback callback, float slopeFactor, float stepHeight) {
        if (!stepped) {
            stepped = true;
            
            boolean moveUpStep = false;
            moveUpStep = callback.checkForStep(direction, stepHeight, slopeFactor, CHECK_FORWARD_DIST);
            
            if (moveUpStep) {
                steppedUpDist = moveUp(stepHeight, collider, position);
                return true;
            }
        }
        return false;
    }

    protected void climb(final CharacterMovementComponent movementComp, final CharacterStateEvent state, CharacterMoveInputEvent input, EntityRef entity) {
        Vector3f desiredVelocity = new Vector3f(input.getMovementDirection());
        float lengthSquared = desiredVelocity.lengthSquared();
        if (lengthSquared > 1) {
            desiredVelocity.normalize();
        }
        float maxSpeed = movementComp.maxClimbSpeed;
        if (input.isRunning()) {
            maxSpeed *= movementComp.runFactor;
        }
        desiredVelocity.scale(maxSpeed);
        desiredVelocity.y -= CLIMB_GRAVITY;
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(state.getVelocity());
        velocityDiff.scale(Math.min(movementComp.groundFriction * input.getDelta(), 1.0f));
        Vector3f endVelocity = new Vector3f(state.getVelocity());
        endVelocity.x += velocityDiff.x;
        endVelocity.y += velocityDiff.y;
        endVelocity.z += velocityDiff.z;
        Vector3f moveDelta = new Vector3f(endVelocity);
        moveDelta.scale(input.getDelta());
        BulletPhysics.BulletCharacterMoverCollider collider = (BulletPhysics.BulletCharacterMoverCollider) physics.getCollider(entity);
        MoveResult moveResult = move(state.getPosition(), moveDelta, 0, movementComp.slopeFactor, collider);
        Vector3f distanceMoved = new Vector3f(moveResult.getFinalPosition());
        distanceMoved.sub(state.getPosition());
        state.getPosition().set(moveResult.getFinalPosition());
        if (input.isFirstRun() && distanceMoved.length() > 0) {
            entity.send(new MovedEvent(distanceMoved, state.getPosition()));
        }
        collider.setLocation(moveResult.getFinalPosition());
        if (moveResult.isBottomHit()) {
            if (!state.isGrounded()) {
                if (input.isFirstRun()) {
                    Vector3f landVelocity = new Vector3f(state.getVelocity());
                    landVelocity.y += (distanceMoved.y / moveDelta.y) * (endVelocity.y - state.getVelocity().y);
                    entity.send(new VerticalCollisionEvent(state.getPosition(), landVelocity));
                }
                state.setGrounded(true);
            }
            endVelocity.y = 0;
        } else {
            if (moveResult.isTopHit() && endVelocity.y > 0) {
                endVelocity.y = 0;
            }
            state.setGrounded(false);
        }
        state.getVelocity().set(endVelocity);
        if (input.isFirstRun() && moveResult.isHorizontalHit()) {
            entity.send(new HorizontalCollisionEvent(state.getPosition(), state.getVelocity()));
        }
        if (state.isGrounded()) {
            state.setFootstepDelta(state.getFootstepDelta() + distanceMoved.length() / movementComp.distanceBetweenFootsteps);
            if (state.getFootstepDelta() > 1) {
                state.setFootstepDelta(state.getFootstepDelta() - 1);
                if (input.isFirstRun()) {
                    entity.send(new FootstepEvent());
                }
            }
        }
    }

    protected Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f direction) {
        return extractResidualMovement(hitNormal, direction, 1f);
    }

    protected Vector3f extractResidualMovement(Vector3f hitNormal, Vector3f direction, float normalMag) {
        float movementLength = direction.length();
        if (movementLength > physics.getEpsilon()) {
            direction.normalize();
            Vector3f reflectDir = Vector3fUtil.reflect(direction, hitNormal, new Vector3f());
            reflectDir.normalize();
            Vector3f perpendicularDir = Vector3fUtil.getPerpendicularComponent(reflectDir, hitNormal, new Vector3f());
            if (normalMag != 0.0f) {
                Vector3f perpComponent = new Vector3f();
                perpComponent.scale(normalMag * movementLength, perpendicularDir);
                direction.set(perpComponent);
            }
        }
        return direction;
    }

    protected void ghost(final CharacterMovementComponent movementComp, final CharacterStateEvent state, CharacterMoveInputEvent input, EntityRef entity) {
        Vector3f desiredVelocity = new Vector3f(input.getMovementDirection());
        float lengthSquared = desiredVelocity.lengthSquared();
        if (lengthSquared > 1) {
            desiredVelocity.normalize();
        }
        float maxSpeed = movementComp.maxGhostSpeed;
        if (input.isRunning()) {
            maxSpeed *= movementComp.runFactor;
        }
        desiredVelocity.scale(maxSpeed);
        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(state.getVelocity());
        velocityDiff.scale(Math.min(GHOST_INERTIA * input.getDelta(), 1.0f));
        state.getVelocity().add(velocityDiff);
        // No collision, so just do the move
        Vector3f deltaPos = new Vector3f(state.getVelocity());
        deltaPos.scale(input.getDelta());
        state.getPosition().add(deltaPos);
        if (input.isFirstRun() && deltaPos.length() > 0) {
            entity.send(new MovedEvent(deltaPos, state.getPosition()));
        }
    }

    protected MoveResult move(final Vector3f startPosition, final Vector3f moveDelta, final float stepHeight, final float slopeFactor, final CharacterMoverCollider collider) {
        steppedUpDist = 0;
        stepped = false;
        Vector3f position = new Vector3f(startPosition);
        boolean hitTop = false;
        boolean hitBottom = false;
        boolean hitSide = false;
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

    protected boolean moveDown(float dist, float slopeFactor, CharacterMoverCollider collider, Vector3f position) {
        float remainingDist = -dist;
        Vector3f targetPos = new Vector3f(position);
        targetPos.y -= remainingDist + VERTICAL_PENETRATION_LEEWAY;
        Vector3f normalizedDir = new Vector3f(0, -1, 0);
        boolean hit = false;
        int iteration = 0;
        while (remainingDist > physics.getEpsilon() && iteration++ < 10) {
            SweepCallback callback = collider.sweep(position, targetPos, VERTICAL_PENETRATION, -1.0f);
            float actualDist = Math.max(0, (remainingDist + VERTICAL_PENETRATION_LEEWAY) * callback.getClosestHitFraction() - VERTICAL_PENETRATION_LEEWAY);
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
                    float slope = callback.calculateSafeSlope(originalSlope, CHECK_FORWARD_DIST);
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

    protected boolean moveHorizontal(Vector3f horizMove, CharacterMoverCollider collider, Vector3f position, float slopeFactor, float stepHeight) {
        float remainingFraction = 1.0f;
        float dist = horizMove.length();
        if (dist < physics.getEpsilon()) {
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
            SweepCallback callback = collider.sweep(position, targetPos, HORIZONTAL_PENETRATION, slopeFactor);
            /* Note: this isn't quite correct (after the first iteration the closestHitFraction is only for part of the moment)
             but probably close enough */
            float actualDist = Math.max(0, (dist + HORIZONTAL_PENETRATION_LEEWAY) * callback.getClosestHitFraction() - HORIZONTAL_PENETRATION_LEEWAY);
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
                        Vector3f horizNormal = new Vector3f(callback.getHitNormalWorld().x, 0, callback.getHitNormalWorld().z);
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

    protected float moveUp(float riseAmount, CharacterMoverCollider collider, Vector3f position) {
        Vector3f to = new Vector3f(position.x, position.y + riseAmount + VERTICAL_PENETRATION_LEEWAY, position.z);
        SweepCallback callback = collider.sweep(position, to, VERTICAL_PENETRATION_LEEWAY, -1f);
        if (callback.hasHit()) {
            float actualDist = Math.max(0, ((riseAmount + VERTICAL_PENETRATION_LEEWAY) * callback.getClosestHitFraction()) - VERTICAL_PENETRATION_LEEWAY);
            position.y += actualDist;
            return actualDist;
        }
        position.y += riseAmount;
        return riseAmount;
    }

    @Override
    public CharacterStateEvent step(CharacterStateEvent initial, CharacterMoveInputEvent input, EntityRef entity) {
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        CharacterStateEvent result = new CharacterStateEvent(initial);
        result.setSequenceNumber(input.getSequenceNumber());
        if (worldProvider.isBlockRelevant(initial.getPosition())) {
            updatePosition(characterMovementComponent, result, input, entity);
            if (result.getMode() != MovementMode.GHOSTING) {
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

    protected void swim(final CharacterMovementComponent movementComp, final CharacterStateEvent state, CharacterMoveInputEvent input, EntityRef entity) {
        Vector3f desiredVelocity = new Vector3f(input.getMovementDirection());
        float lengthSquared = desiredVelocity.lengthSquared();
        if (lengthSquared > 1) {
            desiredVelocity.normalize();
        }
        float maxSpeed = movementComp.maxWaterSpeed;
        if (input.isRunning()) {
            maxSpeed *= movementComp.runFactor;
        }
        desiredVelocity.scale(maxSpeed);
        desiredVelocity.y -= UNDERWATER_GRAVITY;
        // Modify velocity towards desired, up to the maximum rate determined by friction
        Vector3f velocityDiff = new Vector3f(desiredVelocity);
        velocityDiff.sub(state.getVelocity());
        velocityDiff.scale(Math.min(UNDERWATER_INERTIA * input.getDelta(), 1.0f));
        state.getVelocity().x += velocityDiff.x;
        state.getVelocity().y += velocityDiff.y;
        state.getVelocity().z += velocityDiff.z;
        // Slow down due to friction
        float speed = state.getVelocity().length();
        if (speed > movementComp.maxWaterSpeed) {
            state.getVelocity().scale((speed - 4 * (speed - movementComp.maxWaterSpeed) * input.getDelta()) / speed);
        }
        Vector3f moveDelta = new Vector3f(state.getVelocity());
        moveDelta.scale(input.getDelta());
        BulletPhysics.BulletCharacterMoverCollider collider = (BulletPhysics.BulletCharacterMoverCollider) physics.getCollider(entity);
        // Note: No stepping underwater, no issue with slopes
        MoveResult moveResult = move(state.getPosition(), moveDelta, 0, 0.1f, collider);
        Vector3f distanceMoved = new Vector3f(moveResult.getFinalPosition());
        distanceMoved.sub(state.getPosition());
        state.getPosition().set(moveResult.getFinalPosition());
        if (input.isFirstRun() && distanceMoved.length() > 0) {
            entity.send(new MovedEvent(distanceMoved, state.getPosition()));
            state.setFootstepDelta(state.getFootstepDelta() + distanceMoved.length() / movementComp.distanceBetweenSwimStrokes);
            if (state.getFootstepDelta() > 1) {
                state.setFootstepDelta(state.getFootstepDelta() - 1);
                if (input.isFirstRun()) {
                    entity.send(new SwimStrokeEvent(worldProvider.getBlock(state.getPosition())));
                }
            }
        }
    }

    protected void updatePosition(final CharacterMovementComponent movementComp, final CharacterStateEvent state, CharacterMoveInputEvent input, EntityRef entity) {
        switch (state.getMode()) {
            case GHOSTING:
                ghost(movementComp, state, input, entity);
                break;
            case SWIMMING:
                swim(movementComp, state, input, entity);
                break;
            case WALKING:
                walk(movementComp, state, input, entity);
                break;
            case CLIMBING:
                climb(movementComp, state, input, entity);
                break;
            default:
                walk(movementComp, state, input, entity);
                break;
        }
    }

    @SuppressWarnings(value = "SuspiciousNameCombination")
    protected void updateRotation(CharacterMovementComponent movementComp, CharacterStateEvent result, CharacterMoveInputEvent input) {
        if (movementComp.faceMovementDirection && result.getVelocity().lengthSquared() > 0.01f) {
            float yaw = (float) Math.atan2(result.getVelocity().x, result.getVelocity().z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            result.getRotation().set(axisAngle);
        } else {
            QuaternionUtil.setEuler(result.getRotation(), TeraMath.DEG_TO_RAD * input.getYaw(), 0, 0);
        }
    }

    protected void walk(final CharacterMovementComponent movementComp, final CharacterStateEvent state, CharacterMoveInputEvent input, EntityRef entity) {
        Vector3f desiredVelocity = new Vector3f(input.getMovementDirection());
        float lengthSquared = desiredVelocity.lengthSquared();
        //If the movement direction length is smaller than 1, so will be the movement speed?? huh? Shouldnt this be lengthSquared != 0
        if (lengthSquared > 1) {
            desiredVelocity.normalize();
        }
        float maxSpeed = movementComp.maxGroundSpeed;
        if (input.isRunning()) {
            maxSpeed *= movementComp.runFactor;
        }
        // As we can't use it, remove the y component of desired movement while maintaining speed.
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
        velocityDiff.sub(state.getVelocity());
        velocityDiff.scale(Math.min(movementComp.groundFriction * input.getDelta(), 1.0f));
        Vector3f endVelocity = new Vector3f(state.getVelocity());
        endVelocity.x += velocityDiff.x;
        endVelocity.z += velocityDiff.z;
        endVelocity.y = Math.max(-TERMINAL_VELOCITY, state.getVelocity().y - GRAVITY * input.getDelta());
        Vector3f moveDelta = new Vector3f(endVelocity);
        moveDelta.scale(input.getDelta());
        BulletPhysics.BulletCharacterMoverCollider collider = (BulletPhysics.BulletCharacterMoverCollider) physics.getCollider(entity);
        MoveResult moveResult = move(state.getPosition(), moveDelta, (state.isGrounded()) ? movementComp.stepHeight : 0, movementComp.slopeFactor, collider);
        Vector3f distanceMoved = new Vector3f(moveResult.getFinalPosition());
        distanceMoved.sub(state.getPosition());
        state.getPosition().set(moveResult.getFinalPosition());
        if (input.isFirstRun() && distanceMoved.length() > 0) {
            entity.send(new MovedEvent(distanceMoved, state.getPosition()));
        }
        collider.setLocation(moveResult.getFinalPosition());
        if (moveResult.isBottomHit()) {
            if (!state.isGrounded()) {
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
        if (state.isGrounded()) {
            state.setFootstepDelta(state.getFootstepDelta() + distanceMoved.length() / movementComp.distanceBetweenFootsteps);
            if (state.getFootstepDelta() > 1) {
                state.setFootstepDelta(state.getFootstepDelta() - 1);
                if (input.isFirstRun()) {
                    entity.send(new FootstepEvent());
                }
            }
        }
    }

    /**
     * Holds the result of some movement.
     */
    public class MoveResult {

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
