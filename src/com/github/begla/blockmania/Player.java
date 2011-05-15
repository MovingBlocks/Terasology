/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania;

import com.github.begla.blockmania.utilities.Helper;
import com.github.begla.blockmania.utilities.RayFaceIntersection;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.utilities.PerlinNoise;
import java.util.Collections;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

/**
 * This class contains all functions regarding the player's actions,
 * movement and the orientation of the camera.
 * 
 * @author Benjamin Glatzel <benjamin.glawwtzel@me.com>
 */
public final class Player extends RenderableObject {

    private static final PlacingBox _placingBox = new PlacingBox();
    private boolean _jump = false;
    private byte _selectedBlockType = 1;
    private double _wSpeed = Configuration.getSettingNumeric("WALKING_SPEED");
    private double _yaw = 135d;
    private double _pitch;
    private final Vector3f _moveVector = new Vector3f(0, 0, 0);
    private final Vector3f _accVector = new Vector3f(0, 0, 0);
    private float _gravity = 0.0f;
    private World _parent = null;
    private final PerlinNoise _pGen = new PerlinNoise((int) Helper.getInstance().getTime());
    private Vector3f _viewDirection = new Vector3f();

    public Player() {
        resetPlayer();
    }

    /**
     * Positions the player within the world and adjusts the player's view accordingly.
     */
    @Override
    public void render() {

        if (Configuration.getSettingBoolean("ENABLE_BOBBING") && !Configuration.getSettingBoolean("GOD_MODE")) {
            float bobbing2 = _pGen.noise(_position.x / 1.5f, _position.z / 1.5f, 0f) * 2f;
            glRotatef(bobbing2, 0f, 0f, 1f);
        }

        glRotatef((float) _pitch, 1f, 0f, 0f);
        glRotatef((float) _yaw, 0f, 1f, 0f);

        if (Configuration.getSettingBoolean("ENABLE_BOBBING") && !Configuration.getSettingBoolean("GOD_MODE")) {
            float bobbing1 = _pGen.noise(_position.x * 1.5f, _position.z * 1.5f, 0f) * 0.15f;
            glTranslatef(0.0f, bobbing1, 0);
        }

        glTranslatef(-_position.x, -_position.y, -_position.z);

//        glPushMatrix();
//        glTranslatef(_position.x, _position.y, _position.z);
//        glColor3f(1f, 0f, 0f);
//        Sphere s = new Sphere();
//        s.draw(0.1f, 128, 12);
//        glPopMatrix();

        RayFaceIntersection is = calcSelectedBlock();

        // Display the block the player is aiming at
        if (Configuration.getSettingBoolean("SHOW_PLACING_BOX")) {
            if (is != null) {

//                glPointSize(5f);
//                glBegin(GL_POINTS);
//                glVertex3f(is.getIntersectPoint().x, is.getIntersectPoint().y, is.getIntersectPoint().z);
//                glEnd();

                glPushMatrix();
                glTranslatef((int) is.getBlockPos().x, (int) is.getBlockPos().y, (int) is.getBlockPos().z);
                _placingBox.render();
                glPopMatrix();
            }
        }
    }

    /**
     * Updates the player.
     * 
     * @param delta Delta value since the last frame update
     */
    @Override
    public void update(long delta) {
        yaw(Mouse.getDX() * 0.1f);
        pitch(Mouse.getDY() * 0.1f);

        processMovement();
        updatePlayerPosition(delta);

        // Update the view direction
        _viewDirection.set((float) Math.sin(Math.toRadians(_yaw)) * (float) Math.cos(Math.toRadians(_pitch)), -1f * (float) Math.sin(Math.toRadians(_pitch)), -1 * (float) Math.cos(Math.toRadians(_pitch)) * (float) Math.cos(Math.toRadians(_yaw)));
        _viewDirection.normalise();

        _moveVector.set(0, 0, 0);
    }

    /**
     * Yaws the player's point of view.
     *
     * @param diff Amount of yawing to be applied.
     */
    public void yaw(float diff) {
        double nYaw = (_yaw + diff) % 360;
        if (nYaw < 0) {
            nYaw += 360;
        }
        _yaw = nYaw;
    }

    /**
     * Pitches the player's point of view.
     *
     * @param diff Amount of pitching to be applied.
     */
    public void pitch(float diff) {
        double nPitch = (_pitch - diff) % 360;
        if (nPitch < 0) {
            nPitch += 360;
        }
        // Do not allow the player to "look on his back" :-)
        // TODO: Problematic if the mouse movement is very fast
        if ((nPitch > 0 && nPitch < 90) || (nPitch < 360 && nPitch > 270)) {
            _pitch = nPitch;
        }
    }

    /**
     * Moves the player forward.
     */
    public void walkForward() {
        _moveVector.x += (double) _wSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));

        if (Configuration.getSettingBoolean("GOD_MODE")) {
            _moveVector.y -= (double) _wSpeed * Math.sin(Math.toRadians(_pitch));
        }

        _moveVector.z -= _wSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
    }

    /*
     * Moves the player backward.
     */
    /**
     *
     */
    public void walkBackwards() {
        _moveVector.x -= (double) _wSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));

        if (Configuration.getSettingBoolean("GOD_MODE")) {
            _moveVector.y += (double) _wSpeed * Math.sin(Math.toRadians(_pitch));
        }

        _moveVector.z += (double) _wSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
    }

    /**
     * Lets the player strafe left.
     */
    public void strafeLeft() {
        _moveVector.x += (double) _wSpeed * Math.sin(Math.toRadians(_yaw - 90));
        _moveVector.z -= (double) _wSpeed * Math.cos(Math.toRadians(_yaw - 90));
    }

    /**
     * Lets the player strafe right.
     */
    public void strafeRight() {
        _moveVector.x += (double) _wSpeed * Math.sin(Math.toRadians(_yaw + 90));
        _moveVector.z -= (double) _wSpeed * Math.cos(Math.toRadians(_yaw + 90));
    }

    /**
     * Lets the player jump. Yey.
     */
    public void jump() {
        _jump = true;
    }

    /**
     * Calcluates the currently looked at block in front
     * of the player.
     * 
     * @return Intersection point of the looked at block
     */
    public RayFaceIntersection calcSelectedBlock() {
        ArrayList<RayFaceIntersection> inters = new ArrayList<RayFaceIntersection>();

        // The ray should originate from the player's eye
        for (int x = -4; x < 4; x++) {
            for (int y = -4; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        ArrayList<RayFaceIntersection> iss = _parent.rayBlockIntersection((int) _position.x + x, (int) _position.y + y, (int) _position.z + z, _position, _viewDirection);
                        if (iss != null) {
                            inters.addAll(iss);
                        }
                    }
                }
            }
        }

        /**
         * Calculated the closest intersection.
         */
        if (inters.size() > 0) {
            Collections.sort(inters);
            return inters.get(0);
        }

        return null;
    }

    /**
     * 
     * @return
     */
    public String selectedBlockInformation() {
        RayFaceIntersection r = calcSelectedBlock();
        Vector3f bp = r.getBlockPos();
        byte blockType = _parent.getBlock((int) bp.x, (int) bp.y, (int) bp.z);
        byte blockLight = _parent.getLight((int) bp.x, (int) bp.y, (int) bp.z);

        return String.format("%s (t: %d, l: %d) ", r, blockType, blockLight);
    }

    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     */
    public void placeBlock(byte type) {
        if (getParent() != null) {
            RayFaceIntersection is = calcSelectedBlock();
            if (is != null) {
                Vector3f blockPos = is.calcAdjacentBlockPos();
                // Players should not place blocks inside themselves! That would be silly!
                Vector3f playerBlockPos = new Vector3f(_position);
                playerBlockPos.x = (int) (playerBlockPos.x + 0.5f);
                playerBlockPos.y = (int) (playerBlockPos.y);
                playerBlockPos.z = (int) (playerBlockPos.z + 0.5f);

                if (blockPos.x != playerBlockPos.x || (blockPos.y != playerBlockPos.y && blockPos.y != playerBlockPos.y + 1f) || blockPos.z != playerBlockPos.z) {
                    getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, type, true, false);
                }
            }
        }
    }

    /**
     * Plants a tree of a given type in front of the player.
     * 
     * @param type The type of the tree
     */
    public void plantTree(int type) {
        RayFaceIntersection is = calcSelectedBlock();
        if (is != null) {
            Vector3f blockPos = is.getBlockPos();

            if (type == 0) {
                _parent.getGeneratorTree().generate((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, true);
            } else {
                _parent.getGeneratorPineTree().generate((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, true);
            }
        }
    }

    /**
     * Removes a block.
     */
    public void removeBlock() {
        if (getParent() != null) {
            RayFaceIntersection is = calcSelectedBlock();
            if (is != null) {
                Vector3f blockPos = is.getBlockPos();
                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, (byte) 0x0, true, true);
            }
        }
    }

    /**
     * Processes the keyboard input.
     * 
     * @param key Pressed key on the keyboard
     * @param state The state of the key
     * @param repeatEvent True if repeat event
     */
    public void processKeyboardInput(int key, boolean state, boolean repeatEvent) {
        switch (key) {
            case Keyboard.KEY_E:
                if (state && !repeatEvent) {
                    placeBlock(_selectedBlockType);
                }
                break;
            case Keyboard.KEY_Q:
                if (state && !repeatEvent) {
                    removeBlock();
                }
                break;
            case Keyboard.KEY_UP:
                if (!repeatEvent && state) {
                    cycleBlockTypes(1);
                }
                break;
            case Keyboard.KEY_DOWN:
                if (!repeatEvent && state) {
                    cycleBlockTypes(-1);
                }
                break;
            case Keyboard.KEY_SPACE:
                if (!repeatEvent && state) {
                    jump();
                }
                break;
        }
    }

    /**
     * Processes the mouse input.
     * 
     * @param button Pressed mouse button
     * @param state State of the mouse button
     */
    public void processMouseInput(int button, boolean state) {
        if (button == 0 && state) {
            placeBlock(_selectedBlockType);
        } else if (button == 1 && state) {
            removeBlock();
        }
    }

    /**
     * Checks for pressed keys and exectutes the respective movement
     * command.
     */
    private void processMovement() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            walkForward();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            walkBackwards();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            strafeLeft();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            strafeRight();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            _wSpeed = Configuration.getSettingNumeric("WALKING_SPEED") * Configuration.getSettingNumeric("RUNNING_FACTOR");
        } else {
            _wSpeed = Configuration.getSettingNumeric("WALKING_SPEED");
        }
    }

    /**
     * Checks for blocks below and above the player.
     *
     * TODO: Not working for blocks above the player.
     * TODO: Somehow clumsy. :-(
     * TODO: Critical with bad FPS
     *
     * @param oldPosition The position before the player's position was updated
     * @return True if a vertical collision was detected
     */
    private boolean verticalHitTest(Vector3f origin) {
        float offset = Configuration.getSettingNumeric("PLAYER_HEIGHT");
        boolean result = false;
        int y = -1;
        for (int x = -1; x < 2; ++x) {
            for (int z = -1; z < 2; ++z) {
                if (y != 0 || z != 0 || x != 0) {
                    Vector3f blockPos = new Vector3f((int) (origin.x + 0.5f + x), (int) (origin.y + 0.5f) - offset + y, (int) (origin.z + 0.5f + z));
                    int blockType1 = _parent.getBlock((int) blockPos.x, (int) (blockPos.y), (int) blockPos.z);

                    if (!Block.getBlock(blockType1).isPenetrable()) {
                        if (_position.x + 0.1f > blockPos.x - 0.5f && _position.x - 0.1f < blockPos.x + 0.5f && _position.z + 0.1f > blockPos.z - 0.5f && _position.z - 0.1f < blockPos.z + 0.5f && _position.y + 0.1f - offset > blockPos.y - 0.5f && _position.y - 0.1f - offset < blockPos.y + 0.5f) {
                            result = true;
                            if (_gravity < 0f) {
                                _position.y = origin.y;
                                _gravity = 0f;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Checks for blocks around the player.
     * 
     * @param oldPosition The position before the player's position was updated
     */
    private void horizontalHitTest(Vector3f oldPosition) {
        localHorizontalHitTest(0, 1, oldPosition, new Vector3f(1, 0, 0));
        localHorizontalHitTest(0, -1, oldPosition, new Vector3f(-1, 0, 0));
        localHorizontalHitTest(1, 0, oldPosition, new Vector3f(0, 0, 1));
        localHorizontalHitTest(-1, 0, oldPosition, new Vector3f(0, 0, -1));
        localHorizontalHitTest(1, 1, oldPosition, new Vector3f(-1, 0, 1));
        localHorizontalHitTest(-1, -1, oldPosition, new Vector3f(-1, 0, 1));
        localHorizontalHitTest(1, -1, oldPosition, new Vector3f(1, 0, 1));
        localHorizontalHitTest(-1, 1, oldPosition, new Vector3f(1, 0, 1));
    }

    /**
     * Checks for horizontal collisiosn in one specific direction.
     * 
     * @param x Direction along the x-axis
     * @param z Direction along the z-axis
     * @param oldPosition The position before the player's position was updated
     * @param normal The normal of the surface in the given direction
     */
    private void localHorizontalHitTest(int x, int z, Vector3f oldPosition, Vector3f normal) {
        float offset = Configuration.getSettingNumeric("PLAYER_HEIGHT");
        for (int y = 0; y < Math.ceil(offset) + 1; y++) {
            Vector3f blockPos = new Vector3f((int) (oldPosition.x + 0.5f) + x, (int) (oldPosition.y + 0.5f) + y - offset, (int) (oldPosition.z + 0.5f) + z);
            int blockType1 = _parent.getBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z);
            if (!Block.getBlock(blockType1).isPenetrable()) {
                if (_position.x + 0.1f > blockPos.x - 0.5f && _position.x - 0.1f < blockPos.x + 0.5f && _position.z + 0.1f > blockPos.z - 0.5f && _position.z - 0.1f < blockPos.z + 0.5f) {
                    Vector3f scratch = new Vector3f(_position.x, 0f, _position.z);
                    scratch.x -= oldPosition.x;
                    scratch.z -= oldPosition.z;

                    float length = Vector3f.dot(normal, scratch);
                    _position.z = oldPosition.z + length * normal.z;
                    _position.x = oldPosition.x + length * normal.x;
                }
            }
        }
    }

    /**
     * Updates the position of the player.
     * 
     * @param delta Delta value since the last frame update
     */
    private void updatePlayerPosition(float delta) {
        // Save the previous position before chaning any of the values
        Vector3f oldPosition = new Vector3f(_position);

        if (Configuration.getSettingBoolean("DEMO_FLIGHT") && Configuration.getSettingBoolean("GOD_MODE")) {
            _position.z += 8f / 1000f * delta;
            return;
        }

        /*
         * Slowdown the speed of the player each time this method is called.
         */
        if (Math.abs(_accVector.y) > 0f) {
            _accVector.y += -1f * _accVector.y * Configuration.getSettingNumeric("FRICTION") * delta;
        }

        if (Math.abs(_accVector.x) > 0f) {
            _accVector.x += -1f * _accVector.x * Configuration.getSettingNumeric("FRICTION") * delta;
        }

        if (Math.abs(_accVector.z) > 0f) {
            _accVector.z += -1f * _accVector.z * Configuration.getSettingNumeric("FRICTION") * delta;
        }

        if (Math.abs(_accVector.x) > _wSpeed || Math.abs(_accVector.z) > _wSpeed || Math.abs(_accVector.z) > _wSpeed) {
            double max = Math.max(Math.max(Math.abs(_accVector.x), Math.abs(_accVector.z)), _accVector.y);
            double div = max / _wSpeed;

            _accVector.x /= div;
            _accVector.z /= div;
            _accVector.y /= div;
        }


        /*
         * Increase the speed of the player by adding the movement
         * vector to the acceleration vector.
         */
        _accVector.x += _moveVector.x;
        _accVector.y += _moveVector.y;
        _accVector.z += _moveVector.z;

        getPosition().y += (_accVector.y / 1000.0f) * delta;
        getPosition().y += (_gravity / 1000.0f) * delta;

        if (!Configuration.getSettingBoolean("GOD_MODE")) {
            boolean vHit = verticalHitTest(oldPosition);
            if (!vHit) {
                // If the player is not standing on ground: increase the g-force
                if (_gravity > -Configuration.getSettingNumeric("MAX_GRAVITY")) {
                    _gravity -= Configuration.getSettingNumeric("GRAVITY") * delta;
                }
            } else {
                // Jumping is only possible, if the player is standing on ground
                if (_jump) {
                    _jump = false;
                    _gravity = Configuration.getSettingNumeric("JUMP_INTENSITY");
                }
            }
        } else {
            _gravity = 0f;
        }

        /*
         * Update the position of the player
         * according to the acceleration vector.
         */
        getPosition().x += (_accVector.x / 1000.0f) * delta;
        getPosition().z += (_accVector.z / 1000.0f) * delta;

        /*
         * Check for horizontal collisions __after__ checking for vertical
         * collisions.
         */
        if (!Configuration.getSettingBoolean("GOD_MODE")) {
            horizontalHitTest(oldPosition);
        }
    }

    /**
     * Resets the player's position.
     */
    public void resetPlayer() {
        _position = Helper.getInstance().calcPlayerOrigin();
        _accVector.set(0, 0, 0);
        _moveVector.set(0, 0, 0);
        _gravity = 0.0f;
    }

    /**
     * Returns the parent world.
     *
     * @return the parent world
     */
    public World getParent() {
        return _parent;
    }

    /**
     * Sets the parent world an resets the player.
     * 
     * @param parent the parent world
     */
    public void setParent(World parent) {
        this._parent = parent;
    }

    /**
     * 
     * @return 
     */
    public Vector3f getViewDirection() {
        return _viewDirection;
    }

    /**
     * Cycles the selected block type.
     * 
     * @param upDown Cycling direction
     */
    public void cycleBlockTypes(int upDown) {
        _selectedBlockType += upDown;

        if (_selectedBlockType >= Block.getBlockCount()) {
            _selectedBlockType = 0;
        } else if (_selectedBlockType < 0) {
            _selectedBlockType = (byte) (Block.getBlockCount() - 1);
        }
    }

    /**
     * Some information about the player.
     * 
     * @return
     */
    @Override
    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f | b: %d | gravity: %.2f | x: %.2f, y: %.2f, z:, %.2f)", _position.x, _position.y, _position.z, _viewDirection.x, _viewDirection.y, _viewDirection.z, _selectedBlockType, _gravity, _moveVector.x, _moveVector.y, _moveVector.z);
    }
}
