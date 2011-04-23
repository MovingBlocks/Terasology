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
    private int wSpeed = WALKING_SPEED;
    // Viewing direction of the player
    private double yaw = 135d;
    private double pitch;
    // Acceleration
    private double accX, accY, accZ;
    // Gravity (aka acceleration y)
    private double gravity;
    // The parent world
    private World parent = null;
    // Limit the actions a player can in one second
    private long lastAction = Helper.getInstance().getTime();

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
        glRotatef((float) pitch, 1f, 0f, 0f);
        glRotatef((float) yaw, 0f, 1f, 0f);
        glTranslatef(-_position.x, -_position.y, -_position.z);
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
        yaw += diff;
    }

    /**
     * Pitches the player's point of view.
     * @param diff Amount of pitching to be applied.
     */
    public void pitch(float diff) {
        pitch -= diff;
    }

    /**
     * Moves the player forward.
     */
    public void walkForward() {
        accX += (double) wSpeed * Math.sin(Math.toRadians(yaw));
        if (godMode) {
            accY -= (double) wSpeed * Math.sin(Math.toRadians(pitch));
        }
        accZ -= wSpeed * Math.cos(Math.toRadians(yaw));
    }

    /*
     * Moves the player backward.
     */
    public void walkBackwards() {
        accX -= (double) wSpeed * Math.sin(Math.toRadians(yaw));
        if (godMode) {
            accY += (double) wSpeed * Math.sin(Math.toRadians(pitch));
        }
        accZ += (double) wSpeed * Math.cos(Math.toRadians(yaw));
    }

    /*
     * Lets the player strafe left.
     */
    public void strafeLeft() {
        accX += (double) wSpeed * Math.sin(Math.toRadians(yaw - 90));
        accZ -= (double) wSpeed * Math.cos(Math.toRadians(yaw - 90));
    }

    /*
     * Lets the player strafe right.
     */
    public void strafeRight() {
        accX += (double) wSpeed * Math.sin(Math.toRadians(yaw + 90));
        accZ -= (double) wSpeed * Math.cos(Math.toRadians(yaw + 90));
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
            gravity = JUMP_INTENSITY;
        }
    }

    public Vector3f calcViewBlockPosition() {
        Vector3f blockPosition = new Vector3f((int) _position.x + 0.5f, (int) _position.y + 0.5f, (int) _position.z + 0.5f);
        Vector3f vD = new Vector3f((float) Math.sin(Math.toRadians(yaw)), -1f * (float) Math.sin(Math.toRadians(pitch)), -1f * (float) Math.cos(Math.toRadians(yaw)));
        vD.normalise();

        // Maximum distance the player can reach
        for (int z = 0; z < 8; z++) {
            blockPosition.x += vD.x;
            blockPosition.y += vD.y;
            blockPosition.z += vD.z;

            if (parent.getBlock((int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z) > 0) {
                return blockPosition;
            }
        }

        return blockPosition;
    }

    /**
     * Places a block.
     * TODO: Yeah... Should do more than that. :-)
     */
    public void placeBlock() {
        if (getParent() != null) {
            Vector3f blockPosition = calcViewBlockPosition();
            getParent().setBlock((int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z, 0x2, true);
        }
    }

    /**
     * Removes a block.
     * TODO: Yeah... Should do more than that. :-)
     */
    public void removeBlock() {
        if (getParent() != null) {
            Vector3f blockPosition = calcViewBlockPosition();
            getParent().setBlock((int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z, 0x0, true);
        }
    }

    private void processPlayerInteraction() {
        if (Helper.getInstance().getTime() - lastAction > 50) {
            if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                placeBlock();
            } else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                removeBlock();
            } else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                resetPlayer();
            } else if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
                parent.generateForest();
            } else if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                parent.generateTree((int) calcViewBlockPosition().x, (int) calcViewBlockPosition().y, (int) calcViewBlockPosition().z);
            } else if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
                parent.updateAllChunks();
            } else if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
                this.godMode = !godMode;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
                this.demoAutoFlyMode = !demoAutoFlyMode;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                Configuration._showPlacingBox = !Configuration._showPlacingBox;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
                Configuration._showChunkOutlines = !Configuration._showChunkOutlines;
            }

            lastAction = Helper.getInstance().getTime();
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
                wSpeed = RUNNING_SPEED;
            } else {
                wSpeed = WALKING_SPEED;
            }

            boolean hitting = parent.isHitting((int) (getPosition().x + 0.5f), (int) (getPosition().y - PLAYER_HEIGHT), (int) (getPosition().z + 0.5f));

            if (!godMode) {
                /*
                 * Apply gravity.
                 */
                if (!hitting) {
                    if (gravity > -MAX_GRAVITY) {
                        gravity -= 0.5f;
                    }
                    getPosition().y += (gravity / 1000.0f) * delta;
                } else if (gravity > 0.0f) {
                    getPosition().y += (gravity / 1000.0f) * delta;
                } else {
                    gravity = 0.0f;
                }

                Vector2f dir = new Vector2f((float) accX, (float) accZ);
                try {
                    dir.normalise();
                } catch (Exception e) {
                }

                /**
                 * Collision detection with objects along the x/z-plane.
                 */
                if (checkForCollision(new Vector3f(getPosition().x + dir.x * 0.1f, getPosition().y - PLAYER_HEIGHT + 1, getPosition().z + dir.y * 0.1f))) {
                    accX = 0;
                    accZ = 0;
                }

            }

            if (demoAutoFlyMode) {
                accX = 8.f;
                accZ = 8.f;
            }

            getPosition().x += (accX / 1000.0f) * delta;
            getPosition().y += (accY / 1000.0f) * delta;
            getPosition().z += (accZ / 1000.0f) * delta;

            accX = 0;
            accY = 0;
            accZ = 0;

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
        return parent;
    }

    /**
     * Sets the parent world an resets the player.
     * @param parent the parent world
     */
    public void setParent(World parent) {
        this.parent = parent;
        resetPlayer();
    }
}
