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
package com.github.begla.blockmania.logic.characters;

import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.logic.manager.ConfigurationManager;
import com.github.begla.blockmania.logic.manager.ShaderManager;
import com.github.begla.blockmania.logic.manager.TextureManager;
import com.github.begla.blockmania.logic.manager.ToolManager;
import com.github.begla.blockmania.logic.tools.Tool;
import com.github.begla.blockmania.logic.world.BlockObserver;
import com.github.begla.blockmania.logic.world.Chunk;
import com.github.begla.blockmania.model.blocks.Block;
import com.github.begla.blockmania.model.blocks.BlockManager;
import com.github.begla.blockmania.model.inventory.BlockItem;
import com.github.begla.blockmania.model.inventory.Inventory;
import com.github.begla.blockmania.model.inventory.Item;
import com.github.begla.blockmania.model.inventory.Toolbar;
import com.github.begla.blockmania.model.structures.AABB;
import com.github.begla.blockmania.model.structures.BlockPosition;
import com.github.begla.blockmania.model.structures.RayBlockIntersection;
import com.github.begla.blockmania.rendering.cameras.Camera;
import com.github.begla.blockmania.rendering.cameras.FirstPersonCamera;
import com.github.begla.blockmania.rendering.world.WorldRenderer;
import com.github.begla.blockmania.utilities.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.*;

/**
 * Extends the character class and provides support for player functionality. Also provides the
 * modelview matrix from the player's point of view.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Player extends Character {

    /* CONSTANT VALUES */
    private static final double MOUSE_SENS = (Double) ConfigurationManager.getInstance().getConfig().get("Controls.mouseSens");
    private static final boolean DEMO_FLIGHT = (Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.demoFlight");
    private static final boolean GOD_MODE = (Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.godMode");
    private static final boolean CAMERA_BOBBING = (Boolean) ConfigurationManager.getInstance().getConfig().get("Player.cameraBobbing");

    private static final double WALKING_SPEED = (Double) ConfigurationManager.getInstance().getConfig().get("Player.walkingSpeed");
    private static final boolean SHOW_PLACING_BOX = (Boolean) ConfigurationManager.getInstance().getConfig().get("HUD.placingBox");
    private static final double RUNNING_FACTOR = (Double) ConfigurationManager.getInstance().getConfig().get("Player.runningFactor");
    private static final double JUMP_INTENSITY = (Double) ConfigurationManager.getInstance().getConfig().get("Player.jumpIntensity");

    /* OBSERVERS */
    private final ArrayList<BlockObserver> _observers = new ArrayList<BlockObserver>();

    /* CAMERA */
    private final FirstPersonCamera _firstPersonCamera = new FirstPersonCamera();
    private final Camera _activeCamera = _firstPersonCamera;

    /* HAND */
    private float _handMovementAnimationOffset;

    /* INTERACTIONS */
    private long _lastInteraction;
    private byte _extractionCounter;
    private RayBlockIntersection.Intersection _selectedBlock, _extractedBlock;

    /* INVENTORY */
    private Inventory _inventory = new Inventory(this);
    private Toolbar _toolbar = new Toolbar(this);

    /* GOD MODE */
    private long _lastTimeSpacePressed;

    /* RESPAWNING */
    private long _timeOfDeath;

    /* TOOLS */
    private final ToolManager _toolManager = new ToolManager(this);

    public Player(WorldRenderer parent) {
        super(parent, WALKING_SPEED, RUNNING_FACTOR, JUMP_INTENSITY);

        // Set the default value for the god mode
        _godMode = GOD_MODE;
    }

    public void render() {
        super.render();
        updateCameraParameters();

        // Display the block the player is aiming at
        if (SHOW_PLACING_BOX) {
            if (_selectedBlock != null) {
                if (BlockManager.getInstance().getBlock(_parent.getWorldProvider().getBlockAtPosition(_selectedBlock.getBlockPosition().toVector3d())).isRenderBoundingBox()) {
                    Block.AABBForBlockAt(_selectedBlock.getBlockPosition().toVector3d()).render(8f);
                }
            }
        }

        calcSelectedBlock();
    }

    public void update() {
        _walkingSpeed = WALKING_SPEED;

        if (_activeCamera != null) {
            _activeCamera.update();

            // Slightly adjust the field of view when flying
            if (_godMode) {
                _activeCamera.extendFov(10);
            } else {
                _activeCamera.resetFov();
            }
        }

        // Speedup if the player is playing god
        if (_godMode) {
            _walkingSpeed *= 1.5;
        }

        if (_handMovementAnimationOffset > 0) {
            _handMovementAnimationOffset -= 0.04;
        } else if (_handMovementAnimationOffset < 0) {
            _handMovementAnimationOffset = 0;
        }

        super.update();

        _selectedBlock = calcSelectedBlock();

        // Respawn the player after a certain amount of time
        if (isDead() && _timeOfDeath == -1) {
            _timeOfDeath = Blockmania.getInstance().getTime();
        } else if (isDead() && Blockmania.getInstance().getTime() - _timeOfDeath > 1000) {
            revive();
            respawn();
            _timeOfDeath = -1;
        }
    }

    /**
     * Processes interactions for the given mouse button.
     *
     * @param button The pressed mouse button
     */
    private void processInteractions(int button) {
        // Throttle interactions
        if (Blockmania.getInstance().getTime() - _lastInteraction < 200) {
            return;
        }

        Tool activeTool = getActiveTool();

        if (activeTool != null) {
            // Check if one of the mouse buttons is pressed
            if (Mouse.isButtonDown(0) || button == 0) {
                activeTool.executeLeftClickAction();
                _lastInteraction = Blockmania.getInstance().getTime();
                poke();
            } else if (Mouse.isButtonDown(1) || button == 1) {
                activeTool.executeRightClickAction();
                _lastInteraction = Blockmania.getInstance().getTime();
                poke();
            }
        }
    }

    public void resetExtraction() {
        _extractedBlock = _selectedBlock;
        _extractionCounter = 0;
    }

    public void poke() {
        _handMovementAnimationOffset = 0.5f;
    }

    public void renderFirstPersonViewElements() {
        if (getActiveItem() != null) {
            if (getActiveItem().renderFirstPersonView()) {
                return;
            }
        }

        renderHand();
    }

    /**
     * Calculates the bobbing offset factor using sine/cosine functions. The offset adjusted linearly to
     * the player's movement speed.
     *
     * @param phaseOffset Phase offset
     * @param amplitude   Amplitude
     * @param frequency   Frequency
     * @return The bobbing offset
     */
    public double calcBobbingOffset(float phaseOffset, float amplitude, float frequency) {
        if (_godMode || DEMO_FLIGHT)
            return 0;

        float speedFactor = 1.0f;

        if (_activeWalkingSpeed > 0)
            speedFactor = (float) MathHelper.clamp(Math.max(Math.abs(_velocity.x), Math.abs(_velocity.z)) / _activeWalkingSpeed);

        return Math.sin(_stepCounter * frequency + phaseOffset) * amplitude * speedFactor;
    }

    public void updateCameraParameters() {
        _firstPersonCamera.getPosition().set(calcEyeOffset());

        if (CAMERA_BOBBING) {
            _firstPersonCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.2f));
            _firstPersonCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) Math.PI / 4f, 0.025f, 4.4f));
        } else {
            _firstPersonCamera.setBobbingRotationOffsetFactor(0.0);
            _firstPersonCamera.setBobbingVerticalOffsetFactor(0.0);
        }

        if (!(DEMO_FLIGHT)) {
            _firstPersonCamera.getViewingDirection().set(getViewingDirection());
        } else {
            Vector3d viewingTarget = new Vector3d(getPosition().x, 40, getPosition().z - 128);
            _firstPersonCamera.getViewingDirection().sub(viewingTarget, getPosition());
        }
    }

    public void updatePosition() {
        // DEMO MODE
        if (DEMO_FLIGHT) {
            getPosition().z -= 0.2f;

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
        ArrayList<RayBlockIntersection.Intersection> inters = new ArrayList<RayBlockIntersection.Intersection>();

        int blockPosX, blockPosY, blockPosZ;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    // Make sure the correct block positions are calculated relatively to the position of the player
                    blockPosX = (int) (getPosition().x + (getPosition().x >= 0 ? 0.5f : -0.5f)) + x;
                    blockPosY = (int) (getPosition().y + (getPosition().y >= 0 ? 0.5f : -0.5f)) + y;
                    blockPosZ = (int) (getPosition().z + (getPosition().z >= 0 ? 0.5f : -0.5f)) + z;

                    byte blockType = _parent.getWorldProvider().getBlock(blockPosX, blockPosY, blockPosZ);

                    // Ignore special blocks
                    if (BlockManager.getInstance().getBlock(blockType).isSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    ArrayList<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(_parent.getWorldProvider(), blockPosX, blockPosY, blockPosZ, calcEyePosition(), _viewingDirection);

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
     * Processes the keyboard input.
     *
     * @param key         Pressed key on the keyboard
     * @param state       The state of the key
     * @param repeatEvent True if repeat event
     */
    public void processKeyboardInput(int key, boolean state, boolean repeatEvent) {
        switch (key) {
            case Keyboard.KEY_K:
                if (!repeatEvent && state) {
                    damage(9999);
                }
                break;
            case Keyboard.KEY_SPACE:
                if (!repeatEvent && state) {
                    jump();

                    if (Blockmania.getInstance().getTime() - _lastTimeSpacePressed < 200) {
                        _godMode = !_godMode;
                    }

                    _lastTimeSpacePressed = Blockmania.getInstance().getTime();
                }
                break;
            case Keyboard.KEY_1:
                _toolbar.setSelectedSlot(0);
                break;
            case Keyboard.KEY_2:
                _toolbar.setSelectedSlot(1);
                break;
            case Keyboard.KEY_3:
                _toolbar.setSelectedSlot(2);
                break;
            case Keyboard.KEY_4:
                _toolbar.setSelectedSlot(3);
                break;
            case Keyboard.KEY_5:
                _toolbar.setSelectedSlot(4);
                break;
            case Keyboard.KEY_6:
                _toolbar.setSelectedSlot(5);
                break;
            case Keyboard.KEY_7:
                _toolbar.setSelectedSlot(6);
                break;
            case Keyboard.KEY_8:
                _toolbar.setSelectedSlot(7);
                break;
            case Keyboard.KEY_9:
                _toolbar.setSelectedSlot(8);
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
        if (isDead())
            return;

        if (wheelMoved != 0) {
            _toolbar.rollSelectedSlot((byte) (wheelMoved / 120));
        } else if (state && (button == 0 || button == 1)) {
            processInteractions(button);
        }
    }

    /**
     * Checks for pressed keys and mouse movement and executes the respective movement
     * command.
     */
    public void processMovement() {

    }


    public void updateInput() {
        if (isDead())
            return;

        // Process interactions even if the mouse button is pressed down
        // and not fired by a repeated event
        processInteractions(-1);

        _movementDirection.set(0, 0, 0);

        double dx = Mouse.getDX();
        double dy = Mouse.getDY();

        yaw(dx * MOUSE_SENS);
        pitch(dy * MOUSE_SENS);

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
            moveUp();
        }

        _running = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && (_touchingGround || _godMode);
    }

    /**
     * Renders the actively selected block in the front of the player's first person perspective.
     */
    public void renderExtractionOverlay() {
        if (_extractionCounter <= 0 || _extractedBlock == null)
            return;

        Block block = BlockManager.getInstance().getBlock(_parent.getWorldProvider().getBlockAtPosition(_extractedBlock.getBlockPosition().toVector3d()));

        glEnable(GL_TEXTURE_2D);
        TextureManager.getInstance().bindTexture("effects");

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_DST_COLOR, GL_ZERO);

        glPushMatrix();
        glTranslated(_extractedBlock.getBlockPosition().x - getPosition().x, _extractedBlock.getBlockPosition().y - getPosition().y, _extractedBlock.getBlockPosition().z - getPosition().z);

        float offset = Math.round(((float) _extractionCounter / block.getHardness()) * 10.0f) * 0.0625f;

        glBegin(GL11.GL_QUADS);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // TOP
        GL11.glTexCoord2f(offset, 0.0f);
        GL11.glVertex3f(-0.5f - 0.01f, 0.5f + 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f);
        GL11.glVertex3f(0.5f + 0.01f, 0.5f + 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0624f);
        GL11.glVertex3f(0.5f + 0.01f, 0.5f + 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset, 0.0624f);
        GL11.glVertex3f(-0.5f - 0.01f, 0.5f + 0.01f, -0.5f - 0.01f);

        // LEFT
        GL11.glTexCoord2f(offset, 0.0f + 0.0624f);
        GL11.glVertex3f(-0.5f - 0.01f, -0.5f - 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f + 0.0624f);
        GL11.glVertex3f(-0.5f - 0.01f, -0.5f - 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f);
        GL11.glVertex3f(-0.5f - 0.01f, 0.5f + 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset, 0.0f);
        GL11.glVertex3f(-0.5f - 0.01f, 0.5f + 0.01f, -0.5f - 0.01f);

        // BACK
        GL11.glTexCoord2f(offset, 0.0f + 0.0624f);
        GL11.glVertex3f(-0.5f - 0.01f, -0.5f - 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f + 0.0624f);
        GL11.glVertex3f(0.5f + 0.01f, -0.5f - 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f);
        GL11.glVertex3f(0.5f + 0.01f, 0.5f + 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset, 0.0f);
        GL11.glVertex3f(-0.5f - 0.01f, 0.5f + 0.01f, 0.5f + 0.01f);

        // RIGHT
        GL11.glTexCoord2f(offset, 0.0f);
        GL11.glVertex3f(0.5f + 0.01f, 0.5f + 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f);
        GL11.glVertex3f(0.5f + 0.01f, 0.5f + 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f + 0.0624f);
        GL11.glVertex3f(0.5f + 0.01f, -0.5f - 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset, 0.0f + 0.0624f);
        GL11.glVertex3f(0.5f + 0.01f, -0.5f - 0.01f, -0.5f - 0.01f);

        // FRONT
        GL11.glTexCoord2f(offset, 0.0f);
        GL11.glVertex3f(-0.5f - 0.01f, 0.5f + 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f);
        GL11.glVertex3f(0.5f + 0.01f, 0.5f + 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f + 0.0624f);
        GL11.glVertex3f(0.5f + 0.01f, -0.5f - 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset, 0.0f + 0.0624f);
        GL11.glVertex3f(-0.5f - 0.01f, -0.5f - 0.01f, -0.5f - 0.01f);

        // BOTTOM
        GL11.glTexCoord2f(offset, 0.0f);
        GL11.glVertex3f(-0.5f - 0.01f, -0.5f - 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f);
        GL11.glVertex3f(0.5f + 0.01f, -0.5f - 0.01f, -0.5f - 0.01f);
        GL11.glTexCoord2f(offset + 0.0624f, 0.0f + 0.0624f);
        GL11.glVertex3f(0.5f + 0.01f, -0.5f - 0.01f, 0.5f + 0.01f);
        GL11.glTexCoord2f(offset, 0.0f + 0.0624f);
        GL11.glVertex3f(-0.5f - 0.01f, -0.5f - 0.01f, 0.5f + 0.01f);

        glEnd();

        glPopMatrix();

        glDisable(GL11.GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }

    /**
     * Renders a simple hand displayed in front of the player's first person perspective.
     * TODO: Should not be rendered using immediate mode!
     */
    public void renderHand() {
        glEnable(GL11.GL_TEXTURE_2D);
        TextureManager.getInstance().bindTexture("char");
        ShaderManager.getInstance().enableShader("block");

        int light = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("block"), "light");
        GL20.glUniform1f(light, _parent.getRenderingLightValueAt(getPosition()));

        // Make sure the hand is not affected by the biome color. ZOMBIES!!!!
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(3);
        colorBuffer.put(1.0f);
        colorBuffer.put(1.0f);
        colorBuffer.put(1.0f);
        colorBuffer.flip();

        int colorOffset = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("block"), "colorOffset");
        GL20.glUniform3(colorOffset, colorBuffer);

        glPushMatrix();
        glTranslatef(0.8f, -1.1f + (float) calcBobbingOffset((float) Math.PI / 8f, 0.05f, 2.5f) - _handMovementAnimationOffset * 0.5f, -1.0f - _handMovementAnimationOffset * 0.5f);
        glRotatef(-45f - _handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(35f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0f, 0.25f, 0f);
        glScalef(0.3f, 0.6f, 0.3f);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glNormal3f(-1, 0, 0);

        // LEFT
        GL11.glTexCoord2f(40.0f * 0.015625f, 20.0f * 0.03125f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(44.0f * 0.015625f, 20.0f * 0.03125f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(44.0f * 0.015625f, 1.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(40.0f * 0.015625f, 1.0f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        GL11.glNormal3f(0, 0, -1);

        // BACK
        GL11.glTexCoord2f(44.0f * 0.015625f, 20.0f * 0.03125f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(48.0f * 0.015625f, 20.0f * 0.03125f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(48.0f * 0.015625f, 1.0f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(44.0f * 0.015625f, 1.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        GL11.glEnd();
        glPopMatrix();

        glDisable(GL11.GL_TEXTURE_2D);

        ShaderManager.getInstance().enableShader(null);

    }

    public Item getActiveItem() {
        return getToolbar().getItemForSelectedSlot();
    }

    public Block getActiveBlock() {
        Item item = getActiveItem();

        if (item != null) {
            if (item.getClass() == BlockItem.class) {
                return BlockManager.getInstance().getBlock(((BlockItem) getActiveItem()).getBlockId());
            }
        }

        return null;
    }

    public Tool getActiveTool() {
        if (getActiveItem() != null) {
            return _toolManager.getToolForIndex(getActiveItem().getToolId());
        }

        return _toolManager.getToolForIndex((byte) 0x01);
    }

    @Override
    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f | gravity: %.2f | x: %.2f, y: %.2f, z: %.2f)", getPosition().x, getPosition().y, getPosition().z, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _gravity, _movementDirection.x, _movementDirection.y, _movementDirection.z);
    }

    protected AABB generateAABBForPosition(Vector3d p) {
        return new AABB(p, new Vector3d(.3f, 0.8f, .3f));
    }

    public AABB getAABB() {
        return generateAABBForPosition(getPosition());
    }

    public Camera getActiveCamera() {
        return _activeCamera;
    }

    public void registerObserver(BlockObserver observer) {
        _observers.add(observer);
    }

    public void unregisterObserver(BlockObserver observer) {
        _observers.remove(observer);
    }

    public void notifyObserversBlockPlaced(Chunk chunk, BlockPosition pos) {
        for (BlockObserver ob : _observers)
            ob.blockPlaced(chunk, pos);
    }

    public void notifyObserversBlockRemoved(Chunk chunk, BlockPosition pos) {
        for (BlockObserver ob : _observers)
            ob.blockRemoved(chunk, pos);
    }

    public Inventory getInventory() {
        return _inventory;
    }

    public Toolbar getToolbar() {
        return _toolbar;
    }

    public ToolManager getToolManager() {
        return _toolManager;
    }

    public RayBlockIntersection.Intersection getSelectedBlock() {
        return _selectedBlock;
    }

    public RayBlockIntersection.Intersection getExtractedBlock() {
        return _extractedBlock;
    }

    public void setExtractedBlock(RayBlockIntersection.Intersection block) {
        _extractedBlock = block;
    }

    public void setExtractionCounter(byte counter) {
        _extractionCounter = counter;
    }

    public byte getExtractionCounter() {
        return _extractionCounter;
    }

    public float getHandMovementAnimationOffset() {
        return _handMovementAnimationOffset;
    }

}