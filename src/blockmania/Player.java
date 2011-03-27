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
package blockmania;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

/**
 * The player class encapsulates all functionality regarding the player.
 * E.g. moving, gravity, placing blocks and so on.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Player extends RenderObject {

    // How high the player can jump
    private static int JUMP_INTENSITY = 20;
    // Max. gravity
    private static int MAX_GRAVITY = 50;
    // Max. speed of the playering while walking
    private static int WALKING_SPEED = 4;
    // Height of the player in "blocks"
    private int PLAYER_HEIGHT = 2;
    // Viewing direction of the player
    private double yaw = 135d;
    private double pitch;
    // Acceleration
    private double accX;
    private double accZ;
    // Gravity (aka acceleration y)
    private double gravity;
    // The parent world
    private World parent = null;

    /**
     * Init. the player.
     */
    public Player(World parent) {
        this.parent = parent;
        position = new Vector3f(0f, 128.0f, 0f);
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
        glTranslatef(-position.x, -position.y, -position.z);
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
        accX += (double) WALKING_SPEED * Math.sin(Math.toRadians(yaw));
        accZ -= WALKING_SPEED * Math.cos(Math.toRadians(yaw));
    }

    /*
     * Moves the player backward.
     */
    public void walkBackwards() {
        accX -= (double) WALKING_SPEED * Math.sin(Math.toRadians(yaw));
        accZ += (double) WALKING_SPEED * Math.cos(Math.toRadians(yaw));
    }

    /*
     * Lets the player strafe left.
     */
    public void strafeLeft() {
        accX += (double) WALKING_SPEED * Math.sin(Math.toRadians(yaw - 90));
        accZ -= (double) WALKING_SPEED * Math.cos(Math.toRadians(yaw - 90));
    }

    /*
     * Lets the player strafe right.
     */
    public void strafeRight() {
        accX += (double) WALKING_SPEED * Math.sin(Math.toRadians(yaw + 90));
        accZ -= (double) WALKING_SPEED * Math.cos(Math.toRadians(yaw + 90));
    }

    private boolean isPlayerStandingOnGround() {
        return parent.isHitting(new Vector3f(getPosition().x, getPosition().y - PLAYER_HEIGHT, getPosition().z));
    }

    private boolean isObjectInFrontOfPlayer() {
        Vector2f direction = new Vector2f((float) accX, (float) accZ);

        try {
            direction.normalise();
        } catch (Exception e) {
        }

        return parent.isHitting(new Vector3f(getPosition().x + direction.x * 2.0f, getPosition().y - 1.0f, getPosition().z + direction.y * 2.0f));
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

    /**
     * Places a block.
     * TODO: Yeah... Should do more than that. :-)
     */
    public void placeBlock() {
        Vector3f blockPosition = new Vector3f(position);
        Vector2f viewingDirection = new Vector2f((float) Math.sin(Math.toRadians(yaw)), -1f * (float) Math.cos(Math.toRadians(yaw)));

        blockPosition.x += 4f * viewingDirection.x;
        blockPosition.y += -1f;
        blockPosition.z += 4f * viewingDirection.y;

        parent.setBlock(blockPosition, 0x1);
    }

    /**
     * Removes a block.
     * TODO: Yeah... Should do more than that. :-)
     */
    public void removeBlock() {
        Vector3f blockPosition = new Vector3f(position);
        Vector2f viewingDirection = new Vector2f((float) Math.sin(Math.toRadians(yaw)), -1f * (float) Math.cos(Math.toRadians(yaw)));

        blockPosition.x += 2f * viewingDirection.x;
        blockPosition.y += -1f;
        blockPosition.z += 2f * viewingDirection.y;

        parent.setBlock(blockPosition, 0x0);
    }

    private void processPlayerInteraction() {
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            placeBlock();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            removeBlock();
        }
    }

    private void processMovement(long delta) {

        if (Keyboard.isKeyDown(Keyboard.KEY_W))//move forward
        {
            walkForward();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S))//move backwards
        {
            walkBackwards();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A))//strafe left
        {
            strafeLeft();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D))//strafe right
        {
            strafeRight();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            jump();
        }

        /*
         * Apply gravity.
         */
        if (!parent.isHitting(new Vector3f(getPosition().x, getPosition().y - PLAYER_HEIGHT, getPosition().z))) {
            if (gravity > -MAX_GRAVITY) {
                gravity -= 1;
            }
            getPosition().y += (gravity * delta) / 1000.0f;
        } else if (gravity > 0.0f) {
            getPosition().y += (gravity * delta) / 1000.0f;
        }

        /**
         * Collision detection with objects along the x/z-plane.
         */
        if (isObjectInFrontOfPlayer()) {
            accX = 0;
            accZ = 0;
        }

        getPosition().x += (accX * (float) delta) / 1000.0f;
        getPosition().z += (accZ * (float) delta) / 1000.0f;

        accX = 0;
        accZ = 0;
    }
}
