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
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.gui.ToolBelt;
import com.github.begla.blockmania.intersections.RayBlockIntersection;
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.main.BlockmaniaConfiguration;
import com.github.begla.blockmania.rendering.cameras.Camera;
import com.github.begla.blockmania.rendering.cameras.FirstPersonCamera;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.World;
import javolution.util.FastList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import java.util.Collections;
import java.util.logging.Level;

/**
 * Extends the character class and provides support for player functionality. Also provides the
 * modelview matrix from the player's point of view.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Player extends Character {

    /* PROPERTIES */
    private byte _selectedBlockType = 1;

    /* CAMERA */
    private final FirstPersonCamera _firstPersonCamera = new FirstPersonCamera();
    private Camera _activeCamera = _firstPersonCamera;

    /**
     * The ToolBelt is how the player interacts with tool events from mouse or keyboard
     */
    private ToolBelt _toolBelt = new ToolBelt(this);

    public Player(World parent) {
        super(parent, (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Player.walkingSpeed"), (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Player.runningFactor"), (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Player.jumpIntensity"));
    }

    public void update() {
        _godMode = (Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("System.Debug.godMode");
        _walkingSpeed = (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Player.walkingSpeed") + Math.abs(calcBobbingOffset((float) Math.PI / 2f, 0.005f, 2.0f));
        _runningFactor = (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Player.runningFactor");
        _jumpIntensity = (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Player.jumpIntensity");

        updateCameras();
        super.update();
    }

    public void render() {
        RayBlockIntersection.Intersection is = calcSelectedBlock();

        // Display the block the player is aiming at
        if ((Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("HUD.placingBox")) {
            if (is != null) {
                if (BlockManager.getInstance().getBlock(_parent.getWorldProvider().getBlockAtPosition(is.getBlockPosition().toVector3f())).shouldRenderBoundingBox()) {
                    Block.AABBForBlockAt(is.getBlockPosition().toVector3f()).render();
                }
            }
        }

        super.render();
    }

    public double calcBobbingOffset(float phaseOffset, float amplitude, float frequency) {
        return Math.sin(_stepCounter * frequency + phaseOffset) * amplitude;
    }

    public void updateCameras() {
        _firstPersonCamera.getPosition().set(calcEyePosition());

        if (!((Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("System.Debug.godMode"))) {
            _firstPersonCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.0f));
            _firstPersonCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) Math.PI / 4f, 0.025f, 2.75f));
        } else {
            _firstPersonCamera.setBobbingRotationOffsetFactor(0.0);
            _firstPersonCamera.setBobbingVerticalOffsetFactor(0.0);
        }

        if (!((Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("System.Debug.demoFlight") && (Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("System.Debug.godMode"))) {
            _firstPersonCamera.getViewingDirection().set(getViewingDirection());
        } else {
            Vector3f viewingTarget = new Vector3f(getPosition().x, 40, getPosition().z + 128);
            Vector3f.sub(viewingTarget, getPosition(), _firstPersonCamera.getViewingDirection());
        }
    }

    public void updatePosition() {
        // DEMO MODE
        if ((Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("System.Debug.demoFlight") && (Boolean) BlockmaniaConfiguration.getInstance().getConfig().get("System.Debug.godMode")) {
            getPosition().z += (Float) BlockmaniaConfiguration.getInstance().getConfig().get("Player.walkingSpeed");

            int maxHeight = _parent.maxHeightAt((int) getPosition().x, (int) getPosition().z + 8) + 16;

            getPosition().y += (maxHeight - getPosition().y) / 64f;

            if (getPosition().y > 128)
                getPosition().y = 128;

            if (getPosition().y < 40f)
                getPosition().y = 40f;

            return;
        }

        super.updatePosition();
    }

    /**
     * Calculates the currently targeted block in front of the player.
     *
     * @return Intersection point of the targeted block
     */
    public RayBlockIntersection.Intersection calcSelectedBlock() {
        FastList<RayBlockIntersection.Intersection> inters = new FastList<RayBlockIntersection.Intersection>();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    byte blockType = _parent.getWorldProvider().getBlock((int) (getPosition().x + x), (int) (getPosition().y + y), (int) (getPosition().z + z));

                    // Ignore special blocks
                    if (BlockManager.getInstance().getBlock(blockType).letsSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    FastList<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(_parent.getWorldProvider(), (int) getPosition().x + x, (int) getPosition().y + y, (int) getPosition().z + z, calcEyePosition(), _viewingDirection);

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
                Block centerBlock = BlockManager.getInstance().getBlock(getParent().getWorldProvider().getBlock((int) is.getBlockPosition().x, (int) is.getBlockPosition().y, (int) is.getBlockPosition().z));

                if (!centerBlock.allowsBlockAttachment()) {
                    return;
                }

                Vector3f blockPos = is.calcAdjacentBlockPos();

                // Prevent players from placing blocks inside their bounding boxes
                if (Block.AABBForBlockAt((int) blockPos.x, (int) blockPos.y, (int) blockPos.z).overlaps(getAABB())) {
                    return;
                }

                getParent().getWorldProvider().setBlock((int) blockPos.x, (int) blockPos.y, (int) blockPos.z, type, true, false);
                AudioManager.getInstance().getAudio("PlaceRemoveBlock").playAsSoundEffect(0.8f + (float) MathHelper.fastAbs(_parent.getWorldProvider().getRandom().randomDouble()) * 0.2f, 0.7f + (float) MathHelper.fastAbs(_parent.getWorldProvider().getRandom().randomDouble()) * 0.3f, false);
            }
        }
    }

    /**
     * Removes a block.
     */
    public void removeBlock() {
        if (getParent() != null) {
            RayBlockIntersection.Intersection is = calcSelectedBlock();
            if (is != null) {
                BlockPosition blockPos = is.getBlockPosition();
                byte currentBlockType = getParent().getWorldProvider().getBlock(blockPos.x, blockPos.y, blockPos.z);
                getParent().getWorldProvider().setBlock(blockPos.x, blockPos.y, blockPos.z, (byte) 0x0, true, true);

                // Remove the upper block if it's a billboard
                byte upperBlockType = getParent().getWorldProvider().getBlock(blockPos.x, blockPos.y + 1, blockPos.z);
                if (BlockManager.getInstance().getBlock(upperBlockType).getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                    getParent().getWorldProvider().setBlock(blockPos.x, blockPos.y + 1, blockPos.z, (byte) 0x0, true, true);
                }

                _parent.getBlockParticleEmitter().setOrigin(blockPos.toVector3f());
                _parent.getBlockParticleEmitter().emitParticles(256, currentBlockType);
                AudioManager.getInstance().getAudio("PlaceRemoveBlock").playAsSoundEffect(0.6f + (float) MathHelper.fastAbs(_parent.getWorldProvider().getRandom().randomDouble()) * 0.2f, 0.5f + (float) MathHelper.fastAbs(_parent.getWorldProvider().getRandom().randomDouble()) * 0.3f, false);
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
            // Hot keys for selecting a tool bar slot
            case Keyboard.KEY_1:
                _toolBelt.setSelectedTool((byte) 1);
                break;
            case Keyboard.KEY_2:
                _toolBelt.setSelectedTool((byte) 2);
                break;
            case Keyboard.KEY_3:
                _toolBelt.setSelectedTool((byte) 3);
                break;
            case Keyboard.KEY_4:
                _toolBelt.setSelectedTool((byte) 4);
                break;
            case Keyboard.KEY_5:
                _toolBelt.setSelectedTool((byte) 5);
                break;
            case Keyboard.KEY_6:
                _toolBelt.setSelectedTool((byte) 6);
                break;
            case Keyboard.KEY_7:
                _toolBelt.setSelectedTool((byte) 7);
                break;
            case Keyboard.KEY_8:
                _toolBelt.setSelectedTool((byte) 8);
                break;
            case Keyboard.KEY_9:
                _toolBelt.setSelectedTool((byte) 9);
                break;
            case Keyboard.KEY_0:
                _toolBelt.setSelectedTool((byte) 10);
                break;
        }
    }

    /**
     * Processes the mouse input.
     *
     * @param button     Pressed mouse button
     * @param state      State of the mouse button
     * @param wheelMoved Distance the mouse wheel moved since last
     */
    public void processMouseInput(int button, boolean state, int wheelMoved) {
        if (button == 0 && state) {
            //placeBlock(_selectedBlockType);
            _toolBelt.activateTool(true);
        } else if (button == 1 && state) {
            //removeBlock();
            _toolBelt.activateTool(false);
        } else if (wheelMoved != 0) {
            Blockmania.getInstance().getLogger().log(Level.INFO, "Mouse wheel moved " + wheelMoved + " for button " + button + ", state " + state);
            _toolBelt.rollSelectedTool((byte) (wheelMoved / 120));
        }
    }

    /**
     * Checks for pressed keys and mouse movement and executes the respective movement
     * command.
     */
    public void processMovement() {
        double dx = Mouse.getDX();
        double dy = Mouse.getDY();

        double mouseSens = (Double) BlockmaniaConfiguration.getInstance().getConfig().get("Controls.mouseSens");

        yaw(dx * mouseSens);
        pitch(dy * mouseSens);

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

        if (_selectedBlockType >= BlockManager.getInstance().availableBlocksSize()) {
            _selectedBlockType = 1;
        } else if (_selectedBlockType < 1) {
            _selectedBlockType = (byte) (BlockManager.getInstance().availableBlocksSize() - 1);
        }
    }

    /**
     * Returns some information about the player as a string.
     *
     * @return The string
     */
    @Override
    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f | b: %d | gravity: %.2f | x: %.2f, y: %.2f, z: %.2f)", getPosition().x, getPosition().y, getPosition().z, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _selectedBlockType, _gravity, _movementDirection.x, _movementDirection.y, _movementDirection.z);
    }

    protected AABB generateAABBForPosition(Vector3f p) {
        return new AABB(p, new Vector3f(.3f, 0.8f, .3f));
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

    public byte getSelectedBlockType() {
        return _selectedBlockType;
    }

    public byte getSelectedTool() {
        return _toolBelt.getSelectedTool();
    }

    public Camera getActiveCamera() {
        return _activeCamera;
    }
}