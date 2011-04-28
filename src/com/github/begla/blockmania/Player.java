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

import java.util.Collections;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

/**
 * The player class encapsulates all functionality regarding the player.
 * E.g. moving, gravity, placing blocks and so on.
 * 
 * @author Benjamin Glatzel <benjamin.glawwtzel@me.com>
 */
public class Player extends RenderObject {

    private boolean demoAutoFlyMode = false;
    private boolean godMode = false;
    // How high the player can jump
    private static int JUMP_INTENSITY = 10;
    // Max. gravity
    private static int MAX_GRAVITY = 32;
    // Max. speed of the playering while walking
    private static int WALKING_SPEED = 4;
    // Max. speed of the playering while walking
    private static int RUNNING_SPEED = 32;
    // Height of the player in "blocks"
    private int PLAYER_HEIGHT = 1;
    // TODO
    private int _wSpeed = WALKING_SPEED;
    // Viewing direction of the player
    private double _yaw = 135d;
    private double _pitch;
    // Acceleration
    private double _accX, _accY, _accZ;
    // Gravity (aka acceleration y)
    private double _gravity;
    // The parent world
    private World _parent = null;
    private boolean _keyPressed = false;

    /**
     * Init. the player.
     */
    public Player() {
    }

    /*
     * Positions the player within the world
     * and adjusts the player's view accordingly.
     *
     */
    @Override
    public void render() {
        glRotatef((float) _pitch, 1f, 0f, 0f);
        glRotatef((float) _yaw, 0f, 1f, 0f);
        glTranslatef(-_position.x, -_position.y, -_position.z);

        RayFaceIntersection is = calcSelectedBlock();

        if (Configuration._showPlacingBox) {
            // Display the currently looked at block

            if (is != null) {

//            glPointSize(5f);
//            glBegin(GL_POINTS);
//            glVertex3f(is.getIntersectPoint().x, is.getIntersectPoint().y, is.getIntersectPoint().z);
//            glEnd();

                int bpX = (int) is.getBlockPos().x;
                int bpY = (int) is.getBlockPos().y;
                int bpZ = (int) is.getBlockPos().z;

                glColor3f(1.0f, 1.0f, 1.0f);

                glBegin(GL_LINES);
                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ - 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ + 0.5f);
                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ + 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ - 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ + 0.5f);

                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ - 0.5f);
                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ - 0.5f);

                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ - 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ - 0.5f);

                glVertex3f(bpX - 0.5f, bpY - 0.5f, bpZ + 0.5f);
                glVertex3f(bpX - 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glVertex3f(bpX + 0.5f, bpY - 0.5f, bpZ + 0.5f);
                glVertex3f(bpX + 0.5f, bpY + 0.5f, bpZ + 0.5f);

                glEnd();

            }
        }
    }

    /**
     * Updates the player's position etc.
     */
    @Override
    public void update(long delta) {
        yaw(Mouse.getDX() * 0.1f);
        pitch(Mouse.getDY() * 0.1f);

        processMovement(delta);
        processPlayerInteraction();
    }

    /**
     * Yaws the player's point of view.
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
     * @param diff Amount of pitching to be applied.
     */
    public void pitch(float diff) {
        double nPitch = (_pitch - diff) % 360;

        if (nPitch < 0) {
            nPitch += 360;
        }

        // TODO: Problematic if the mouse movement is very fast
        if ((nPitch > 0 && nPitch < 90) || (nPitch < 360 && nPitch > 270)) {
            _pitch = nPitch;
        }
    }

    /**
     * Moves the player forward.
     */
    public void walkForward() {
        _accX += (double) _wSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));

        if (godMode) {
            _accY -= (double) _wSpeed * Math.sin(Math.toRadians(_pitch));
        }

        _accZ -= _wSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
    }

    /*
     * Moves the player backward.
     */
    public void walkBackwards() {
        _accX -= (double) _wSpeed * Math.sin(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));

        if (godMode) {
            _accY += (double) _wSpeed * Math.sin(Math.toRadians(_pitch));
        }

        _accZ += (double) _wSpeed * Math.cos(Math.toRadians(_yaw)) * Math.cos(Math.toRadians(_pitch));
    }

    /*
     * Lets the player strafe left.
     */
    public void strafeLeft() {
        _accX += (double) _wSpeed * Math.sin(Math.toRadians(_yaw - 90));
        _accZ -= (double) _wSpeed * Math.cos(Math.toRadians(_yaw - 90));
    }

    /*
     * Lets the player strafe right.
     */
    public void strafeRight() {
        _accX += (double) _wSpeed * Math.sin(Math.toRadians(_yaw + 90));
        _accZ -= (double) _wSpeed * Math.cos(Math.toRadians(_yaw + 90));
    }

    private boolean isPlayerStandingOnGround() {
        if (getParent() != null) {
            return getParent().isHitting((int) (getPosition().x + 0.5f), (int) (getPosition().y - PLAYER_HEIGHT), (int) (getPosition().z + 0.5f));
        } else {
            return false;
        }
    }

    private boolean checkForCollision(Vector3f position) {

        if (getParent() != null) {
            return getParent().isHitting((int) (position.x + 0.5f), (int) position.y, (int) (position.z + 0.5f));

        }

        return false;
    }

    /**
     * Lets the player jump. Yey.
     */
    public void jump() {
        // Jumps are only possible, if the player is hitting the ground.
        if (isPlayerStandingOnGround()) {
            _gravity = JUMP_INTENSITY;
        }
    }

    @Override
    public String toString() {
        Vector3f vD = viewDirection();
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)", _position.x, _position.y, _position.z, vD.x, vD.y, vD.z);
    }

    public Vector3f viewDirection() {
        Vector3f vD = new Vector3f((float) Math.sin(Math.toRadians(_yaw)) * (float) Math.cos(Math.toRadians(_pitch)), -1f * (float) Math.sin(Math.toRadians(_pitch)), -1 * (float) Math.cos(Math.toRadians(_pitch)) * (float) Math.cos(Math.toRadians(_yaw)));
        vD.normalise();

        return vD;
    }

    public RayFaceIntersection calcSelectedBlock() {
        ArrayList<RayFaceIntersection> inters = new ArrayList<RayFaceIntersection>();

        Vector3f vD = viewDirection();
        for (int x = -4; x < 4; x++) {
            for (int y = -4; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    ArrayList<RayFaceIntersection> iss = _parent.rayBlockIntersection((int) _position.x + x, (int) _position.y + y, (int) _position.z + z, _position, vD);
                    if (iss != null) {
                        inters.addAll(iss);
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
     * Places a block.
     */
    public void placeBlock() {
        if (getParent() != null) {
            RayFaceIntersection is = calcSelectedBlock();
            if (is != null) {
                Vector3f blockPos = is.calcAdjacentBlockPos();
                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, 0x2, true);
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
                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, 0x0, true);
            }
        }
    }

    private void processPlayerInteraction() {
        while (Keyboard.next()) {
            if (!_keyPressed) {
                if (Keyboard.getEventKey() == Keyboard.KEY_E) {
                    placeBlock();
                } else if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
                    removeBlock();
                } else if (Keyboard.getEventKey() == Keyboard.KEY_R) {
                    resetPlayer();
                } else if (Keyboard.getEventKey() == Keyboard.KEY_T) {
                    RayFaceIntersection is = calcSelectedBlock();
                    if (is != null) {
                        Vector3f blockPos = is.getBlockPos();
                        _parent.generateTree((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, true);
                    }
                } else if (Keyboard.getEventKey() == Keyboard.KEY_U) {
                    _parent.updateAllChunks();
                } else if (Keyboard.getEventKey() == Keyboard.KEY_G) {
                    this.godMode = !godMode;
                } else if (Keyboard.getEventKey() == Keyboard.KEY_H) {
                    this.demoAutoFlyMode = !demoAutoFlyMode;
                } else if (Keyboard.getEventKey() == Keyboard.KEY_P) {
                    Configuration._showPlacingBox = !Configuration._showPlacingBox;
                } else if (Keyboard.getEventKey() == Keyboard.KEY_I) {
                    Configuration._showChunkOutlines = !Configuration._showChunkOutlines;
                }
            }

            if (Keyboard.getEventKeyState()) {
                _keyPressed = true;
            } else {
                _keyPressed = false;
            }
        }
    }

    private void processMovement(long delta) {

        if (getParent() != null) {
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
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                jump();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                _wSpeed = RUNNING_SPEED;
            } else {
                _wSpeed = WALKING_SPEED;
            }

            boolean hitting = _parent.isHitting((int) (getPosition().x + 0.5f), (int) (getPosition().y - PLAYER_HEIGHT), (int) (getPosition().z + 0.5f));

            if (!godMode) {

                if (!hitting) {
                    if (_gravity > -MAX_GRAVITY) {
                        _gravity -= 0.5f;
                    }
                    getPosition().y += (_gravity / 1000.0f) * delta;
                } else if (_gravity > 0.0f) {
                    getPosition().y += (_gravity / 1000.0f) * delta;
                } else {
                    _gravity = 0.0f;
                }

                Vector2f dir = new Vector2f(_accX >= 0 ? 1 : -1, _accZ >= 0 ? 1 : -1);

                try {
                    dir.normalise();
                } catch (Exception e) {
                }

                /**
                 * Collision detection with objects along the x/z-plane.
                 */
                if (checkForCollision(new Vector3f(getPosition().x + dir.x * 0.5f, getPosition().y - PLAYER_HEIGHT + 1, getPosition().z + dir.y * 0.5f))) {
                    _accX = 0;
                    _accZ = 0;
                } else if (checkForCollision(new Vector3f(getPosition().x + dir.x * 0.5f, getPosition().y + 1, getPosition().z + dir.y * 0.5f))) {
                    _accX = 0;
                    _accZ = 0;
                }

            }

            if (demoAutoFlyMode) {
                _accX = 32.f;
                _accZ = 32.f;
            }

            getPosition().x += (_accX / 1000.0f) * delta;
            getPosition().y += (_accY / 1000.0f) * delta;
            getPosition().z += (_accZ / 1000.0f) * delta;

            _accX = 0;
            _accY = 0;
            _accZ = 0;

        }
    }

    /**
     * Resets the player's position.
     */
    public void resetPlayer() {
        _position = Helper.getInstance().calcPlayerOrigin();
    }

    /**
     * Returns the parent world.
     * @return the parent world
     */
    public World getParent() {
        return _parent;
    }

    /**
     * Sets the parent world an resets the player.
     * @param parent the parent world
     */
    public void setParent(World parent) {
        this._parent = parent;
        resetPlayer();
    }
}
