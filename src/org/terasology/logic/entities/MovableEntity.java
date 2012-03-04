/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.entities;

import org.terasology.game.Terasology;
import org.terasology.logic.audio.Sound;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.Config;
import org.terasology.math.TeraMath;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Movable entities extend normal entities to support collision detection, basic physics, movement and the playback of sound clips.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class MovableEntity extends Entity {

    /* AUDIO */
    protected long _lastFootStepSoundPlayed = 0;
    protected Sound _currentFootstepSound;
    protected Sound[] _footstepSounds;

    /* PARENT WORLD */
    protected final WorldRenderer _parent;

    /* MOVEMENT SETTINGS */
    protected static final double MAX_ACC = 56.0d;
    protected static final double MAX_EARTH_ACC_WATER = 10.0d;
    protected static final double EARTH_ACC_WATER = 2.0d;
    protected static final double EARTH_ACC = 28.0d;
    protected static final double RUNNING_FACTOR = 1.5d;
    protected static final double JUMP_INTENSITY = 10.0d;
    protected static final double MOVEMENT_ACC = 1.5d;
    protected static final double GROUND_FRICTION = 8.0d;

    /* MOVEMENT */
    protected double _earthVelocity;
    protected double _activeMovementAcc = MOVEMENT_ACC;
    protected final Vector3d _movementDirection = new Vector3d(), _movementVelocity = new Vector3d();
    protected boolean _isSwimming = false, _headUnderWater = false, _touchingGround = false, _running = false, _godMode, _jump = false;

    /* LOOK AT ME! */
    protected final Vector3d _viewingDirection = new Vector3d();
    protected double _yaw = 135d, _pitch = 0;

    /* BOBBING */
    protected double _stepCounter;

    /**
     * Init. a new movable entity.
     *
     * @param parent The parent world
     */
    public MovableEntity(WorldRenderer parent) {
        _parent = parent;

        reset();
        initAudio();
    }

    protected void initAudio() {
        _footstepSounds = AudioManager.sounds("FootGrass1", "FootGrass2", "FootGrass3", "FootGrass4", "FootGrass5");
    }

    protected abstract AABB generateAABBForPosition(Vector3d p);

    protected abstract void handleVerticalCollision();

    protected abstract void handleHorizontalCollision();

    public void render() {
        // Update the viewing direction
        setViewingDirection(_yaw, _pitch);

        if (Config.getInstance().isDebugCollision()) {
            getAABB().render(2f);

            ArrayList<BlockPosition> blocks = gatherAdjacentBlockPositions(getPosition());

            for (int i = 0; i < blocks.size(); i++) {
                BlockPosition p = blocks.get(i);
                byte blockType = _parent.getWorldProvider().getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
                Block block = BlockManager.getInstance().getBlock(blockType);
                for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                    blockAABB.render(2f);
                }
            }
        }
    }

    public void update(double delta) {
        if (!_running)
            _activeMovementAcc = MOVEMENT_ACC;
        else if (_godMode)
            _activeMovementAcc = MOVEMENT_ACC * 4.0;
        else
            _activeMovementAcc = MOVEMENT_ACC * RUNNING_FACTOR;

        processMovement();
        updatePosition(delta);
        checkPosition();
        updateSwimStatus();

        playMovementSound();
    }

    private void checkPosition() {
        if (!_godMode && getPosition().y < 0) {
            getPosition().y = _parent.maxHeightAt((int) getPosition().x, (int) getPosition().y);
        }
    }

    private void playMovementSound() {
        if (_godMode)
            return;

        if ((TeraMath.fastAbs(_movementVelocity.x) > 0.01 || TeraMath.fastAbs(_movementVelocity.z) > 0.01) && _touchingGround) {
            if (_currentFootstepSound == null) {
                playFootstep(_currentFootstepSound = _footstepSounds[_parent.getWorldProvider().getRandom().randomIntAbs(_footstepSounds.length)]);
            } else {
                long timeDiff = Terasology.getInstance().getTimeInMs() - _lastFootStepSoundPlayed;

                if (timeDiff > 400 / (_activeMovementAcc / MOVEMENT_ACC)) {
                    _lastFootStepSoundPlayed = Terasology.getInstance().getTimeInMs();
                    _currentFootstepSound = null;
                }
            }
        }
    }

    public void processMovement() {
        // Do movement related stuff here
    }

    /**
     * Resets the entity's attributes.
     */
    public void reset() {
        _movementVelocity.set(0, 0, 0);
        _movementDirection.set(0, 0, 0);
        _earthVelocity = 0.0f;
    }

    /**
     * Checks for blocks below and above the entity.
     *
     * @param origin The origin position of the entity
     * @return True if a vertical collision was detected
     */
    private boolean verticalHitTest(Vector3d origin) {
        ArrayList<BlockPosition> blockPositions = gatherAdjacentBlockPositions(origin);

        boolean moved = false;

        for (int i = 0; i < blockPositions.size(); i++) {
            BlockPosition p = blockPositions.get(i);

            byte blockType1 = _parent.getWorldProvider().getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
            AABB entityAABB = getAABB();

            Block block = BlockManager.getInstance().getBlock(blockType1);
            if (block == null || block.isPenetrable())
                continue;
            for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                if (!entityAABB.overlaps(blockAABB))
                    continue;

                double direction = origin.y - getPosition().y;

                if (direction >= 0) {
                    getPosition().y = blockAABB.getPosition().y + blockAABB.getDimensions().y + entityAABB.getDimensions().y;
                    getPosition().y += java.lang.Math.ulp(getPosition().y);
                } else {
                    getPosition().y = blockAABB.getPosition().y - blockAABB.getDimensions().y - entityAABB.getDimensions().y;
                    getPosition().y -= java.lang.Math.ulp(getPosition().y);
                }

                moved = true;
            }
        }

        return moved;
    }

    /**
     * Gathers all adjacent block positions for a given block position.
     *
     * @param origin The block position
     * @return A list of adjacent block positions
     */
    private ArrayList<BlockPosition> gatherAdjacentBlockPositions(Vector3d origin) {
        /*
         * Gather the surrounding block positions
         * and order those by the distance to the originating point.
         */
        ArrayList<BlockPosition> blockPositions = new ArrayList<BlockPosition>();

        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    int blockPosX = (int) (origin.x + (origin.x >= 0 ? 0.5f : -0.5f)) + x;
                    int blockPosY = (int) (origin.y + (origin.y >= 0 ? 0.5f : -0.5f)) + y;
                    int blockPosZ = (int) (origin.z + (origin.z >= 0 ? 0.5f : -0.5f)) + z;

                    blockPositions.add(new BlockPosition(blockPosX, blockPosY, blockPosZ, origin));
                }
            }
        }

        // Sort the block positions
        Collections.sort(blockPositions);
        return blockPositions;
    }

    /**
     * Checks for blocks around the entity.
     *
     * @param origin The original position of the entity
     * @return True if the entity is colliding horizontally
     */
    private boolean horizontalHitTest(Vector3d origin) {
        boolean result = false;
        ArrayList<BlockPosition> blockPositions = gatherAdjacentBlockPositions(origin);

        // Check each block position for collision
        for (int i = 0; i < blockPositions.size(); i++) {
            BlockPosition p = blockPositions.get(i);
            byte blockType = _parent.getWorldProvider().getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
            Block block = BlockManager.getInstance().getBlock(blockType);

            if (!block.isPenetrable()) {
                for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                    if (getAABB().overlaps(blockAABB)) {
                        result = true;
                        Vector3d direction = new Vector3d(getPosition().x, 0f, getPosition().z);
                        direction.x -= origin.x;
                        direction.z -= origin.z;

                        // Calculate the point of intersection on the block's AABB
                        Vector3d blockPoi = blockAABB.closestPointOnAABBToPoint(origin);
                        Vector3d entityPoi = generateAABBForPosition(origin).closestPointOnAABBToPoint(blockPoi);

                        Vector3d planeNormal = blockAABB.getFirstHitPlane(direction, origin, getAABB().getDimensions(), true, false, true);

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
                        getPosition().set(newPosition);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Updates the position of the entity.
     */
    protected void updatePosition(double delta) {
        // Save the previous position before changing any of the values
        Vector3d oldPosition = new Vector3d(getPosition());

        double timePassedInSeconds = (delta / 1000d);

        /*
         * Slowdown the speed of the entity each time this method is called.
         */
        if (TeraMath.fastAbs(_movementVelocity.y) > 0f) {
            _movementVelocity.y += -1f * _movementVelocity.y * GROUND_FRICTION * timePassedInSeconds;
        }

        if (TeraMath.fastAbs(_movementVelocity.x) > 0f) {
            _movementVelocity.x += -1f * _movementVelocity.x * GROUND_FRICTION * timePassedInSeconds;
        }

        if (TeraMath.fastAbs(_movementVelocity.z) > 0f) {
            _movementVelocity.z += -1f * _movementVelocity.z * GROUND_FRICTION * timePassedInSeconds;
        }

        /*
         * Apply friction.
         */
        if (TeraMath.fastAbs(_movementVelocity.x) > _activeMovementAcc || TeraMath.fastAbs(_movementVelocity.z) > _activeMovementAcc || TeraMath.fastAbs(_movementVelocity.y) > _activeMovementAcc) {
            double max = java.lang.Math.max(java.lang.Math.max(TeraMath.fastAbs(_movementVelocity.x), TeraMath.fastAbs(_movementVelocity.z)), TeraMath.fastAbs(_movementVelocity.y));
            double div = max / _activeMovementAcc;

            _movementVelocity.x /= div;
            _movementVelocity.z /= div;
            _movementVelocity.y /= div;
        }

        /*
         * Increase the speed of the entity by adding the movement
         * vector to the acceleration vector.
         */
        if (_movementDirection.length() > 0.0)
            _movementDirection.normalize();

        _movementVelocity.x += _movementDirection.x * _activeMovementAcc;
        _movementVelocity.y += _movementDirection.y * _activeMovementAcc;
        _movementVelocity.z += _movementDirection.z * _activeMovementAcc;

        if (_earthVelocity > -MAX_ACC && !_godMode && !_isSwimming) {
            _earthVelocity -= EARTH_ACC * timePassedInSeconds;
        }

        if (_earthVelocity < -MAX_ACC && !_godMode && !_isSwimming) {
            _earthVelocity = -MAX_ACC;
        }

        if (_earthVelocity > -MAX_EARTH_ACC_WATER && !_godMode && _isSwimming) {
            _earthVelocity -= EARTH_ACC_WATER * timePassedInSeconds;
        }

        if (_earthVelocity < -MAX_EARTH_ACC_WATER && !_godMode && _isSwimming) {
            _earthVelocity = -MAX_EARTH_ACC_WATER;
        }

        getPosition().y += (_movementVelocity.y + _earthVelocity) * timePassedInSeconds;

        if (!_godMode) {
            if (verticalHitTest(oldPosition)) {
                handleVerticalCollision();

                double oldEarthVelocity = _earthVelocity;
                _earthVelocity = 0;

                if (oldEarthVelocity <= 0) {
                    // Jumping is only possible, if the entity is standing on ground
                    if (_jump) {
                        playRandomFootstep();
                        _jump = false;
                        _earthVelocity = JUMP_INTENSITY;
                    } else if (!_touchingGround) { // Entity reaches the ground
                        playRandomFootstep();
                        _touchingGround = true;
                    }
                } else {
                    _touchingGround = false;
                }
            } else {
                _touchingGround = false;
            }
        } else {
            _earthVelocity = 0f;
        }

        oldPosition.set(getPosition());

        /*
         * Update the position of the entity
         * according to the acceleration vector.
         */
        getPosition().x += _movementVelocity.x * timePassedInSeconds;
        getPosition().z += _movementVelocity.z * timePassedInSeconds;

        _stepCounter += java.lang.Math.max(TeraMath.fastAbs(_movementVelocity.x * timePassedInSeconds), TeraMath.fastAbs(_movementVelocity.z * timePassedInSeconds));

        /*
         * Check for horizontal collisions __after__ checking for vertical
         * collisions.
         */
        if (!_godMode) {
            if (horizontalHitTest(oldPosition)) {
                handleHorizontalCollision();
            }
        }
    }

    /**
     * Updates the status if the entity is currently swimming (in water).
     */
    private void updateSwimStatus() {
        ArrayList<BlockPosition> blockPositions = gatherAdjacentBlockPositions(getPosition());

        boolean swimming = false, headUnderWater = false;

        Vector3d eyePos = calcEyePosition();
        eyePos.y += 0.25;

        for (int i = 0; i < blockPositions.size(); i++) {
            BlockPosition p = blockPositions.get(i);
            byte blockType = _parent.getWorldProvider().getBlockAtPosition(new Vector3d(p.x, p.y, p.z));
            Block block = BlockManager.getInstance().getBlock(blockType);
            if (block.isLiquid()) {
                for (AABB blockAABB : block.getColliders(p.x, p.y, p.z)) {
                    if (getAABB().overlaps(blockAABB)) {
                        swimming = true;
                        if (blockAABB.contains(eyePos)) {
                            headUnderWater = true;
                            break;
                        }
                    }
                }
            }
        }

        _headUnderWater = headUnderWater;
        _isSwimming = swimming;
    }


    /**
     * Yaws the entity's point of view.
     *
     * @param diff Amount of yawing to be applied.
     */
    public void yaw(double diff) {
        double nYaw = (_yaw + diff) % 360;
        if (nYaw < 0) {
            nYaw += 360;
        }
        _yaw = nYaw;
    }

    /**
     * Pitches the entity's point of view.
     *
     * @param diff Amount of pitching to be applied.
     */
    public void pitch(double diff) {
        double nPitch = (_pitch - diff);

        if (nPitch > 89)
            nPitch = 89;
        else if (nPitch < -89)
            nPitch = -89;

        _pitch = nPitch;
    }

    public void walkForward() {
        if (!_godMode && !_isSwimming) {
            _movementDirection.x += java.lang.Math.sin(java.lang.Math.toRadians(_yaw));
            _movementDirection.z -= java.lang.Math.cos(java.lang.Math.toRadians(_yaw));
        } else if (!_godMode && _isSwimming) {
            _movementDirection.x += java.lang.Math.sin(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.z -= java.lang.Math.cos(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.y -= java.lang.Math.sin(java.lang.Math.toRadians(_pitch));
        } else {
            _movementDirection.x += java.lang.Math.sin(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.z -= java.lang.Math.cos(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.y -= java.lang.Math.sin(java.lang.Math.toRadians(_pitch));
        }
    }

    public void walkBackwards() {
        if (!_godMode && !_isSwimming) {
            _movementDirection.x -= java.lang.Math.sin(java.lang.Math.toRadians(_yaw));
            _movementDirection.z += java.lang.Math.cos(java.lang.Math.toRadians(_yaw));
        } else if (!_godMode && _isSwimming) {
            _movementDirection.x -= java.lang.Math.sin(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.z += java.lang.Math.cos(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.y += java.lang.Math.sin(java.lang.Math.toRadians(_pitch));
        } else {
            _movementDirection.x -= java.lang.Math.sin(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.z += java.lang.Math.cos(java.lang.Math.toRadians(_yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(_pitch));
            _movementDirection.y += java.lang.Math.sin(java.lang.Math.toRadians(_pitch));
        }
    }

    public void strafeLeft() {
        _movementDirection.x += java.lang.Math.sin(java.lang.Math.toRadians(_yaw - 90));
        _movementDirection.z -= java.lang.Math.cos(java.lang.Math.toRadians(_yaw - 90));
    }

    public void strafeRight() {
        _movementDirection.x += java.lang.Math.sin(java.lang.Math.toRadians(_yaw + 90));
        _movementDirection.z -= java.lang.Math.cos(java.lang.Math.toRadians(_yaw + 90));
    }

    public void jump() {
        if (_touchingGround && !_isSwimming && !_godMode) {
            _jump = true;
        }
    }

    public void moveUp() {
        if (_isSwimming || _godMode) {
            _movementDirection.y += 1.0f;
        }
    }

    public Vector3d calcEntityPositionRelativeToPlayer() {
        Vector3d result = new Vector3d();
        result.sub(Terasology.getInstance().getActiveCamera().getPosition(), getPosition());

        return result;
    }

    public double distanceSquaredTo(Vector3d target) {
        Vector3d targetDirection = new Vector3d();
        targetDirection.sub(target, getPosition());

        return targetDirection.lengthSquared();
    }

    public void lookAt(Vector3d target) {
        Vector3d targetDirection = new Vector3d();
        targetDirection.sub(target, getPosition());
        targetDirection.normalize();

        setPitchYawFromVector(targetDirection);
    }

    public AABB getAABB() {
        return generateAABBForPosition(getPosition());
    }

    public Vector3d calcEyePosition() {
        Vector3d eyePosition = new Vector3d(getPosition());
        eyePosition.add(calcEyeOffset());

        return eyePosition;
    }

    public Vector3d calcEyeOffset() {
        return new Vector3d(0.0f, getAABB().getDimensions().y - 0.2f, 0.0f);
    }

    public Vector3d getViewingDirection() {
        return _viewingDirection;
    }

    public void setViewingDirection(double yaw, double pitch) {
        _viewingDirection.set(java.lang.Math.sin(java.lang.Math.toRadians(yaw)) * java.lang.Math.cos(java.lang.Math.toRadians(pitch)), -java.lang.Math.sin(java.lang.Math.toRadians(pitch)), -java.lang.Math.cos(java.lang.Math.toRadians(pitch)) * java.lang.Math.cos(java.lang.Math.toRadians(yaw)));
        _viewingDirection.normalize(_viewingDirection);
    }

    public void setPitchYawFromVector(Vector3d v) {
        _pitch = java.lang.Math.toDegrees(-java.lang.Math.asin(v.y));
        _yaw = java.lang.Math.toDegrees(java.lang.Math.atan2(v.x, -v.z));

        if (_yaw < 0)
            _yaw = 360 + _yaw;
    }

    protected void playRandomFootstep() {
        this.playFootstep(_footstepSounds[_parent.getWorldProvider().getRandom().randomIntAbs(_footstepSounds.length)]);
    }

    protected void playFootstep(Sound footStep) {
        AudioManager.play(footStep, this, 0.6f, AudioManager.PRIORITY_LOW);
    }

    public boolean isSwimming() {
        return _isSwimming;
    }

    public boolean isHeadUnderWater() {
        return _headUnderWater;
    }

    public WorldRenderer getParent() {
        return _parent;
    }

    public Vector3d getVelocity() {
        return _movementVelocity;
    }
}
