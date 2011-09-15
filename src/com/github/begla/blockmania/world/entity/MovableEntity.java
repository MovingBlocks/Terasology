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
package com.github.begla.blockmania.world.entity;

import com.github.begla.blockmania.audio.AudioManager;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockWater;
import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.World;
import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.openal.Audio;

import java.util.Collections;

/**
 * Movable entities extend normal entities to support collision detection, basic physics, movement and audio.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class MovableEntity extends Entity {

    protected final FastRandom _rand = new FastRandom();

    protected Audio _currentFootstepSound;
    protected Audio[] _footstepSounds;

    protected double _walkingSpeed;
    protected double _runningFactor;
    protected double _jumpIntensity;
    protected boolean _godMode;

    protected World _parent;

    protected boolean _jump = false;
    protected double _activeWalkingSpeed;
    protected double _yaw = 135d, _pitch;
    protected double _gravity;
    protected final Vector3f _movementDirection = new Vector3f(), _velocity = new Vector3f(), _viewingDirection = new Vector3f();
    protected boolean _isSwimming = false, _headUnderWater = false, _touchingGround = false, _running = false;

    public MovableEntity(World parent, double walkingSpeed, double runningFactor, double jumpIntensity) {
        _parent = parent;
        _walkingSpeed = walkingSpeed;
        _runningFactor = runningFactor;
        _jumpIntensity = jumpIntensity;

        resetEntity();
        initAudio();
    }

    private void initAudio() {
        _footstepSounds = new Audio[5];
        _footstepSounds[0] = AudioManager.getInstance().loadAudio("FootGrass1");
        _footstepSounds[1] = AudioManager.getInstance().loadAudio("FootGrass2");
        _footstepSounds[2] = AudioManager.getInstance().loadAudio("FootGrass3");
        _footstepSounds[3] = AudioManager.getInstance().loadAudio("FootGrass4");
        _footstepSounds[4] = AudioManager.getInstance().loadAudio("FootGrass5");
    }

    public abstract void processMovement();

    protected abstract AABB generateAABBForPosition(Vector3f p);

    public void render() {
        if (Configuration.getSettingBoolean("DEBUG_COLLISION")) {
            getAABB().render();

            FastList<BlockPosition> blocks = gatherAdjacentBlockPositions(getPosition());

            for (FastList.Node<BlockPosition> n = blocks.head(), end = blocks.tail(); (n = n.getNext()) != end; ) {
                AABB blockAABB = Block.AABBForBlockAt(n.getValue().x, n.getValue().y, n.getValue().z);
                blockAABB.render();
            }
        }
    }

    public void update() {
        if (!_running)
            _activeWalkingSpeed = _walkingSpeed;
        else
            _activeWalkingSpeed = _walkingSpeed * _runningFactor;


        // Update the viewing direction
        setViewingDirection(_yaw, _pitch);

        processMovement();
        updatePosition();
        updateSwimStatus();

        _movementDirection.set(0, 0, 0);

        playMovementSound();
    }

    private void playMovementSound() {
        if (_godMode)
            return;

        if ((Math.abs(_velocity.x) > 0.01 || Math.abs(_velocity.z) > 0.01) && _touchingGround) {
            if (_currentFootstepSound == null) {
                Vector3f playerDirection = directionOfPlayer();
                _currentFootstepSound = _footstepSounds[Math.abs(_rand.randomInt()) % 5];
                _currentFootstepSound.playAsSoundEffect(0.7f + (float) Math.abs(_rand.randomDouble()) * 0.3f, 0.2f + (float) Math.abs(_rand.randomDouble()) * 0.3f, false, playerDirection.x, playerDirection.y, playerDirection.z);
            } else {
                if (!_currentFootstepSound.isPlaying()) {
                    _currentFootstepSound = null;
                }
            }
        }
    }


    /**
     * Resets the entity's attributes.
     */
    public void resetEntity() {
        _velocity.set(0, 0, 0);
        _movementDirection.set(0, 0, 0);
        _gravity = 0.0f;
    }

    /**
     * Checks for blocks below and above the entity.
     *
     * @param origin The origin position of the entity
     * @return True if a vertical collision was detected
     */
    private boolean verticalHitTest(Vector3f origin) {
        FastList<BlockPosition> blockPositions = gatherAdjacentBlockPositions(origin);

        for (FastList.Node<BlockPosition> n = blockPositions.head(), end = blockPositions.tail(); (n = n.getNext()) != end; ) {
            byte blockType1 = _parent.getBlockAtPosition(new Vector3f(n.getValue().x, n.getValue().y, n.getValue().z));
            AABB entityAABB = getAABB();

            if (Block.getBlockForType(blockType1).isPenetrable() || !entityAABB.overlaps(Block.AABBForBlockAt(n.getValue().x, n.getValue().y, n.getValue().z)))
                continue;

            double direction = origin.y - getPosition().y;

            if (direction >= 0)
                getPosition().y = n.getValue().y + 0.50001f + entityAABB.getDimensions().y;
            else
                getPosition().y = n.getValue().y - 0.50001f - entityAABB.getDimensions().y;

            return true;
        }

        return false;
    }

    /**
     * @param origin The originating entity position
     * @return A list of adjacent block positions
     */
    protected FastList<BlockPosition> gatherAdjacentBlockPositions(Vector3f origin) {
        /*
         * Gather the surrounding block positions
         * and order those by the distance to the originating point.
         */
        FastList<BlockPosition> blockPositions = new FastList<BlockPosition>();

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
    private boolean horizontalHitTest(Vector3f origin) {
        boolean result = false;
        FastList<BlockPosition> blockPositions = gatherAdjacentBlockPositions(origin);

        // Check each block position for collision
        for (FastList.Node<BlockPosition> n = blockPositions.head(), end = blockPositions.tail(); (n = n.getNext()) != end; ) {
            byte blockType = _parent.getBlockAtPosition(new Vector3f(n.getValue().x, n.getValue().y, n.getValue().z));
            AABB blockAABB = Block.AABBForBlockAt(n.getValue().x, n.getValue().y, n.getValue().z);

            if (!Block.getBlockForType(blockType).isPenetrable()) {
                if (getAABB().overlaps(blockAABB)) {
                    result = true;

                    // Calculate the direction from the origin to the current position
                    Vector3f direction = new Vector3f(getPosition().x, 0f, getPosition().z);
                    direction.x -= origin.x;
                    direction.z -= origin.z;

                    // Calculate the point of intersection on the block's AABB
                    Vector3f blockPoi = blockAABB.closestPointOnAABBToPoint(origin);
                    Vector3f entityPoi = generateAABBForPosition(origin).closestPointOnAABBToPoint(blockPoi);

                    Vector3f planeNormal = blockAABB.normalForPlaneClosestToOrigin(blockPoi, origin, true, false, true);

                    // Find a vector parallel to the surface normal
                    Vector3f slideVector = new Vector3f(planeNormal.z, 0, -planeNormal.x);
                    Vector3f pushBack = new Vector3f();

                    Vector3f.sub(blockPoi, entityPoi, pushBack);

                    // Calculate the intensity of the diversion alongside the block
                    double length = Vector3f.dot(slideVector, direction);

                    Vector3f newPosition = new Vector3f();
                    newPosition.z = (float) (origin.z + pushBack.z * 0.2 + length * slideVector.z);
                    newPosition.x = (float) (origin.x + pushBack.x * 0.2 + length * slideVector.x);
                    newPosition.y = origin.y;

                    // Update the position
                    getPosition().set(newPosition);
                }
            }
        }

        return result;
    }

    /**
     * Updates the position of the entity.
     */
    protected void updatePosition() {
        // Save the previous position before changing any of the values
        Vector3f oldPosition = new Vector3f(getPosition());

        /*
         * Slowdown the speed of the entity each time this method is called.
         */
        if (Math.abs(_velocity.y) > 0f) {
            _velocity.y += -1f * _velocity.y * Configuration.getSettingNumeric("FRICTION");
        }

        if (Math.abs(_velocity.x) > 0f) {
            _velocity.x += -1f * _velocity.x * Configuration.getSettingNumeric("FRICTION");
        }

        if (Math.abs(_velocity.z) > 0f) {
            _velocity.z += -1f * _velocity.z * Configuration.getSettingNumeric("FRICTION");
        }

        /*
         * Apply friction.
         */
        if (Math.abs(_velocity.x) > _activeWalkingSpeed || Math.abs(_velocity.z) > _activeWalkingSpeed || Math.abs(_velocity.y) > _activeWalkingSpeed) {
            double max = Math.max(Math.max(Math.abs(_velocity.x), Math.abs(_velocity.z)), Math.abs(_velocity.y));
            double div = max / _activeWalkingSpeed;

            _velocity.x /= div;
            _velocity.z /= div;
            _velocity.y /= div;
        }

        /*
         * Increase the speed of the entity by adding the movement
         * vector to the acceleration vector.
         */
        _velocity.x += _movementDirection.x;
        _velocity.y += _movementDirection.y;
        _velocity.z += _movementDirection.z;

        // Normal gravity
        if (_gravity > -Configuration.getSettingNumeric("MAX_GRAVITY") && !_godMode && !_isSwimming) {
            _gravity -= Configuration.getSettingNumeric("GRAVITY");
        }

        if (_gravity < -Configuration.getSettingNumeric("MAX_GRAVITY") && !_godMode && !_isSwimming) {
            _gravity = -Configuration.getSettingNumeric("MAX_GRAVITY");
        }

        // Gravity under water
        if (_gravity > -Configuration.getSettingNumeric("MAX_GRAVITY_SWIMMING") && !_godMode && _isSwimming) {
            _gravity -= Configuration.getSettingNumeric("GRAVITY_SWIMMING");
        }

        if (_gravity < -Configuration.getSettingNumeric("MAX_GRAVITY_SWIMMING") && !_godMode && _isSwimming) {
            _gravity = -Configuration.getSettingNumeric("MAX_GRAVITY_SWIMMING");
        }

        getPosition().y += _velocity.y;
        getPosition().y += _gravity;

        if (!_godMode) {
            if (verticalHitTest(oldPosition)) {
                double oldGravity = _gravity;
                _gravity = 0;

                if (oldGravity <= 0) {
                    // Jumping is only possible, if the entity is standing on ground
                    if (_jump) {
                        _jump = false;
                        _gravity = _jumpIntensity;
                    }

                    // Player reaches the ground
                    if (_touchingGround == false) {
                        Vector3f playerDirection = directionOfPlayer();
                        _footstepSounds[Math.abs(_rand.randomInt()) % 5].playAsSoundEffect(0.7f + (float) Math.abs(_rand.randomDouble()) * 0.3f, 0.2f + (float) Math.abs(_rand.randomDouble()) * 0.3f, false, playerDirection.x, playerDirection.y, playerDirection.z);
                        _touchingGround = true;
                    }
                } else {
                    _touchingGround = false;
                }

                handleVerticalCollision();
            } else {
                _touchingGround = false;
            }
        } else {
            _gravity = 0f;
        }

        oldPosition.set(getPosition());

        /*
         * Update the position of the entity
         * according to the acceleration vector.
         */
        getPosition().x += _velocity.x;
        getPosition().z += _velocity.z;

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

    protected void updateSwimStatus() {
        FastList<BlockPosition> blockPositions = gatherAdjacentBlockPositions(getPosition());

        boolean swimming = false, headUnderWater = false;

        for (FastList.Node<BlockPosition> n = blockPositions.head(), end = blockPositions.tail(); (n = n.getNext()) != end; ) {
            byte blockType = _parent.getBlockAtPosition(new Vector3f(n.getValue().x, n.getValue().y, n.getValue().z));
            AABB blockAABB = Block.AABBForBlockAt(n.getValue().x, n.getValue().y, n.getValue().z);

            if (Block.getBlockForType(blockType).getClass().equals(BlockWater.class) && getAABB().overlaps(blockAABB)) {
                swimming = true;
            }

            Vector3f eyePos = calcEyePosition();
            eyePos.y += 0.25;

            if (Block.getBlockForType(blockType).getClass().equals(BlockWater.class) && blockAABB.contains(eyePos)) {
                headUnderWater = true;
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
    protected void yaw(double diff) {
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
    protected void pitch(double diff) {
        double nPitch = (_pitch - diff);

        if (nPitch > 89)
            nPitch = 89;
        else if (nPitch < -89)
            nPitch = -89;

        _pitch = nPitch;
    }

    protected void walkForward() {
        if (!_godMode && !_isSwimming) {
            _movementDirection.x += _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw));
            _movementDirection.z -= _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw));
        } else if (!_godMode && _isSwimming) {
            _movementDirection.x += _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.z -= _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.y -= _activeWalkingSpeed * Math.sin(Math.toRadians(_pitch));
        } else {
            _movementDirection.x += _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.z -= _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.y -= _activeWalkingSpeed * Math.sin(Math.toRadians(_pitch));
        }
    }

    protected void walkBackwards() {
        if (!_godMode && !_isSwimming) {
            _movementDirection.x -= _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw));
            _movementDirection.z += _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw));
        } else if (!_godMode && _isSwimming) {
            _movementDirection.x -= _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.z += _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.y += _activeWalkingSpeed * Math.sin(Math.toRadians(_pitch));
        } else {
            _movementDirection.x -= _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.z += _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
            _movementDirection.y += _activeWalkingSpeed * Math.sin(Math.toRadians(_pitch));
        }
    }

    protected void strafeLeft() {
        _movementDirection.x += _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw - 90));
        _movementDirection.z -= _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw - 90));
    }

    protected void strafeRight() {
        _movementDirection.x += _activeWalkingSpeed * Math.sin(Math.toRadians(_yaw + 90));
        _movementDirection.z -= _activeWalkingSpeed * Math.cos(Math.toRadians(_yaw + 90));
    }

    protected void jump() {
        if (_touchingGround) {
            _jump = true;
        }
    }

    protected Vector3f directionOfPlayer() {
        return Vector3f.sub(_parent.getPlayer().getPosition(), getPosition(), null);
    }

    protected double distanceSquaredTo(Vector3f target) {
        Vector3f targetDirection = new Vector3f();
        Vector3f.sub(target, getPosition(), targetDirection);
        double distance = targetDirection.lengthSquared();

        return distance;
    }

    protected void lookAt(Vector3f target) {
        Vector3f targetDirection = new Vector3f();
        Vector3f.sub(target, getPosition(), targetDirection);
        targetDirection.normalise();

        setPitchYawFromVector(targetDirection);
    }

    public AABB getAABB() {
        return generateAABBForPosition(getPosition());
    }

    protected Vector3f calcEyePosition() {
        AABB aabb = getAABB();
        Vector3f eyePosition = new Vector3f(aabb.getPosition());
        eyePosition.y += aabb.getDimensions().y - 0.2;
        return eyePosition;
    }

    protected abstract void handleVerticalCollision();

    protected abstract void handleHorizontalCollision();

    public Vector3f getViewingDirection() {
        return _viewingDirection;
    }

    public void setViewingDirection(double yaw, double pitch) {
        _viewingDirection.set((float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))), (float) -Math.sin(Math.toRadians(pitch)), (float) (-Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw))));
        _viewingDirection.normalise(_viewingDirection);
    }

    public void setPitchYawFromVector(Vector3f v) {
        _pitch = Math.toDegrees(-Math.asin(v.getY()));
        _yaw = Math.toDegrees(Math.atan2(v.x, -v.z));

        if (_yaw < 0)
            _yaw = 360 + _yaw;
    }

    public boolean isSwimming() {
        return _isSwimming;
    }

    public boolean isHeadUnderWater() {
        return _headUnderWater;
    }

    public World getParent() {
        return _parent;
    }
}
