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
 *
 */
package com.github.begla.blockmania.world.characters;

import com.github.begla.blockmania.audio.AudioManager;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.datastructures.ViewFrustum;
import com.github.begla.blockmania.intersections.RayBlockIntersection;
import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.noise.PerlinNoise;
import com.github.begla.blockmania.world.World;
import javolution.util.FastList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import java.util.Collections;

import static org.lwjgl.opengl.GL11.*;

/**
 * Extends the character class and provides support for player functionality. Also provides the
 * modelview matrix from the player's point of view.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Player extends Character {

    private byte _selectedBlockType = 1;
    private final PerlinNoise _pGen = new PerlinNoise(42);

    private final ViewFrustum _viewFrustum = new ViewFrustum();

    public Player(World parent) {
        super(parent, Configuration.getSettingNumeric("WALKING_SPEED"), Configuration.getSettingNumeric("RUNNING_FACTOR"), Configuration.getSettingNumeric("JUMP_INTENSITY"));
    }

    public void update() {
        _godMode = Configuration.getSettingBoolean("GOD_MODE");
        _walkingSpeed = Configuration.getSettingNumeric("WALKING_SPEED");
        _runningFactor = Configuration.getSettingNumeric("RUNNING_FACTOR");
        _jumpIntensity = Configuration.getSettingNumeric("JUMP_INTENSITY");

        super.update();
    }

    /**
     * Positions the player within the world and adjusts the player's view accordingly.
     */
    public void render() {
        RayBlockIntersection.Intersection is = calcSelectedBlock();

        // Display the block the player is aiming at
        if (Configuration.getSettingBoolean("PLACING_BOX")) {
            if (is != null) {
                if (Block.getBlockForType(_parent.getBlockAtPosition(is.getBlockPosition())).shouldRenderBoundingBox()) {
                    Block.AABBForBlockAt(is.getBlockPosition()).render();
                }
            }
        }

        super.render();
    }

    public void applyPlayerModelViewMatrix() {

        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();

        if (!(Configuration.getSettingBoolean("DEMO_FLIGHT") && Configuration.getSettingBoolean("GOD_MODE"))) {

            if (Configuration.getSettingBoolean("BOBBING") && !Configuration.getSettingBoolean("GOD_MODE")) {
                double bobbing = _pGen.noise(getPosition().x * 0.5, 0, getPosition().z * 0.5);
                glRotated(bobbing * Configuration.BOBBING_ANGLE, 0, 0, 1);
            }

            Vector3f eyePosition = calcEyePosition();
            GLU.gluLookAt(eyePosition.x, eyePosition.y, eyePosition.z, eyePosition.x + _viewingDirection.x, eyePosition.y + _viewingDirection.y, eyePosition.z + _viewingDirection.z, 0, 1, 0);


        } else {
            GLU.gluLookAt(getPosition().x, getPosition().y, getPosition().z, getPosition().x, 40, getPosition().z + 128, 0, 1, 0);
        }
        // Update the current view frustum
        _viewFrustum.updateFrustum();
    }

    public void applyNormalizedModelViewMatrix() {

        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();

        if (!(Configuration.getSettingBoolean("DEMO_FLIGHT") && Configuration.getSettingBoolean("GOD_MODE"))) {
            GLU.gluLookAt(0, 0, 0, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, 0, 1, 0);
        }
    }

    public void updatePosition() {
        /*
        * DEMO MODE
        */
        if (Configuration.getSettingBoolean("DEMO_FLIGHT") && Configuration.getSettingBoolean("GOD_MODE")) {
            getPosition().z += Configuration.getSettingNumeric("WALKING_SPEED");

            int maxHeight = _parent.maxHeightAt((int) getPosition().x, (int) getPosition().z + 8) + 16;

            getPosition().y += (maxHeight - getPosition().y) / 128f;

            if (getPosition().y > 128)
                getPosition().y = 128;

            if (getPosition().y < 40f)
                getPosition().y = 40f;

            return;
        }

        super.updatePosition();
    }

    /**
     * Calculates the currently looked at block in front of the player.
     *
     * @return Intersection point of the looked at block
     */
    RayBlockIntersection.Intersection calcSelectedBlock() {
        FastList<RayBlockIntersection.Intersection> inters = new FastList<RayBlockIntersection.Intersection>();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    byte blockType = _parent.getBlock((int) (getPosition().x + x), (int) (getPosition().y + y), (int) (getPosition().z + z));

                    // Ignore special blocks
                    if (Block.getBlockForType(blockType).letSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    FastList<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(_parent, (int) getPosition().x + x, (int) getPosition().y + y, (int) getPosition().z + z, calcEyePosition(), _viewingDirection);

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
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     */
    public void placeBlock(byte type) {
        if (getParent() != null) {
            RayBlockIntersection.Intersection is = calcSelectedBlock();
            if (is != null) {
                Block centerBlock = Block.getBlockForType(getParent().getBlock((int) is.getBlockPosition().x, (int) is.getBlockPosition().y, (int) is.getBlockPosition().z));

                if (!centerBlock.playerCanAttachBlocks()) {
                    return;
                }

                Vector3f blockPos = is.calcAdjacentBlockPos();

                // Prevent players from placing blocks inside their bounding boxes
                if (Block.AABBForBlockAt((int) blockPos.x, (int) blockPos.y, (int) blockPos.z).overlaps(getAABB())) {
                    return;
                }

                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, type, true, false);
                AudioManager.getInstance().getAudio("PlaceRemoveBlock").playAsSoundEffect(0.7f + (float) Math.abs(_rand.randomDouble()) * 0.3f, 0.7f + (float) Math.abs(_rand.randomDouble()) * 0.3f, false);
            }
        }
    }

    /**
     * Plants a tree of a given type in front of the player.
     *
     * @param type The type of the tree
     */
    public void plantTree(int type) {
        RayBlockIntersection.Intersection is = calcSelectedBlock();
        if (is != null) {
            Vector3f blockPos = is.getBlockPosition();

            if (type == 0) {
                _parent.getObjectGenerator("tree").generate((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, true);
            } else {
                _parent.getObjectGenerator("pineTree").generate((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, true);
            }
        }
    }

    /**
     * Removes a block.
     */
    void removeBlock() {
        if (getParent() != null) {
            RayBlockIntersection.Intersection is = calcSelectedBlock();
            if (is != null) {
                Vector3f blockPos = is.getBlockPosition();
                byte currentBlockType = getParent().getBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z);
                getParent().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, (byte) 0x0, true, true);

                _parent.getBlockParticleEmitter().setOrigin(blockPos);
                _parent.getBlockParticleEmitter().emitParticles(128, currentBlockType);
                AudioManager.getInstance().getAudio("PlaceRemoveBlock").playAsSoundEffect(0.6f + (float) Math.abs(_rand.randomDouble()) * 0.4f, 0.7f + (float) Math.abs(_rand.randomDouble()) * 0.3f, false);
            }
        }
    }

    /**
     * Processes the keyboard input.
     *
     * @param key         Pressed key on the keyboard
     * @param state       The state of the key
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
     * @param state  State of the mouse button
     */
    public void processMouseInput(int button, boolean state) {
        if (button == 0 && state) {
            placeBlock(_selectedBlockType);
        } else if (button == 1 && state) {
            removeBlock();
        }
    }

    /**
     * Checks for pressed keys and mouse movement and executes the respective movement
     * command.
     */
    public void processMovement() {
        double dx = Mouse.getDX();
        double dy = Mouse.getDY();

        yaw(dx * Configuration.MOUSE_SENS);
        pitch(dy * Configuration.MOUSE_SENS);

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
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && _touchingGround) {
            _running = true;
        } else {
            _running = false;
        }
    }

    /**
     * Cycles the selected block type.
     *
     * @param upDown Cycling direction
     */
    void cycleBlockTypes(int upDown) {
        _selectedBlockType += upDown;

        if (_selectedBlockType >= Block.getBlockCount()) {
            _selectedBlockType = 0;
        } else if (_selectedBlockType < 0) {
            _selectedBlockType = (byte) (Block.getBlockCount() - 1);
        }
    }

    /**
     * Returns some information about the player as a string.
     *
     * @return The string
     */
    @Override
    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f | b: %d | gravity: %.2f | x: %.2f, y: %.2f, z:, %.2f)", getPosition().x, getPosition().y, getPosition().z, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _selectedBlockType, _gravity, _movementDirection.x, _movementDirection.y, _movementDirection.z);
    }

    protected AABB generateAABBForPosition(Vector3f p) {
        return new AABB(p, new Vector3f(.3f, 0.7f, .3f));
    }

    /**
     * Returns player's AABB.
     *
     * @return The AABB
     */
    public AABB getAABB() {
        return generateAABBForPosition(getPosition());
    }

    @Override
    protected void handleVerticalCollision() {
        // Nothing special to do.
    }

    @Override
    protected void handleHorizontalCollision() {
        // Uh. A wall.
    }

    public ViewFrustum getViewFrustum() {
        return _viewFrustum;
    }

    public byte getSelectedBlockType() {
        return _selectedBlockType;
    }
}