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
package org.terasology.logic.characters;

import groovy.util.ConfigObject;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.TextureManager;
import org.terasology.logic.manager.ToolManager;
import org.terasology.logic.tools.ITool;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.IBlockObserver;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.inventory.*;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.FirstPersonCamera;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.*;

/**
 * Extends the character class and provides support for player functionality. Also provides the
 * modelview matrix from the player's point of view.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Player extends Character {

    /* CONSTANT VALUES */
    private static final double MOUSE_SENS = Config.getInstance().getMouseSens();
    private static final boolean DEMO_FLIGHT = Config.getInstance().isDemoFlight();
    private static final double DEMO_FLIGHT_SPEED = Config.getInstance().getDemoFlightSpeed();
    private static final boolean GOD_MODE = Config.getInstance().isGodMode();
    private static final boolean CAMERA_BOBBING = Config.getInstance().isCameraBobbing();

    private static final boolean RENDER_FIRST_PERSON_VIEW = Config.getInstance().isRenderFirstPersonView();
    private static final boolean SHOW_PLACING_BOX = Config.getInstance().isPlacingBox();

    /* OBSERVERS */
    private final ArrayList<IBlockObserver> _observers = new ArrayList<IBlockObserver>();

    /* CAMERA */
    private final FirstPersonCamera _firstPersonCamera = new FirstPersonCamera();
    private final Camera _activeCamera = _firstPersonCamera;

    /* INTERACTIONS */
    private long _lastInteraction;
    private byte _extractionCounter;
    private RayBlockIntersection.Intersection _selectedBlock, _extractedBlock;
    private float _handMovementAnimationOffset;
    private Mesh _handMesh, _overlayMesh;

    /* INVENTORY */
    private Inventory _inventory = new Inventory();

    /* GOD MODE */
    private long _lastTimeSpacePressed;

    /* RESPAWNING */
    private long _timeOfDeath = -1;

    /* TOOLS */
    private final ToolManager _toolManager = new ToolManager(this);

    public Player(WorldRenderer parent) {
        super(parent);

        // Set the default value for the god mode
        _godMode = GOD_MODE;

        load();
        loadDefaultItems();
    }

    private void loadDefaultItems() {
        _inventory.addItem(new ItemBlock(BlockManager.getInstance().getBlockGroup("Companion")), 1);
        _inventory.addItem(new ItemBlock(BlockManager.getInstance().getBlockGroup("Torch")), 16);
        _inventory.addItem(new ItemPickAxe(), 1);
        _inventory.addItem(new ItemAxe(), 1);
        _inventory.addItem(new ItemBlueprint(), 1);
        _inventory.addItem(new ItemDynamite(), 1);
        _inventory.addItem(new ItemDebug(), 1);
    }

    public void render() {
        super.render();
        updateCameraParameters();

        // Display the block the player is aiming at
        if (SHOW_PLACING_BOX) {
            if (_selectedBlock != null) {
                Block block = BlockManager.getInstance().getBlock(_parent.getWorldProvider().getBlockAtPosition(_selectedBlock.getBlockPosition().toVector3d()));
                if (block.isRenderBoundingBox()) {
                    block.getBounds(_selectedBlock.getBlockPosition()).render(8f);
                }
            }
        }
    }

    public void update(double delta) {
        PerformanceMonitor.startActivity("Player Camera");
        if (_activeCamera != null) {
            _activeCamera.update();

            // Slightly adjust the field of view when flying
            if (_godMode) {
                _activeCamera.extendFov(10);
            } else {
                _activeCamera.resetFov();
            }
        }
        PerformanceMonitor.endActivity();

        if (_handMovementAnimationOffset > 0) {
            _handMovementAnimationOffset -= 0.04;
        } else if (_handMovementAnimationOffset < 0) {
            _handMovementAnimationOffset = 0;
        }

        super.update(delta);

        PerformanceMonitor.startActivity("Player Select Block");
        _selectedBlock = calcSelectedBlock();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Player Death Check");

        // Respawn the player after a certain amount of time
        if (isDead() && _timeOfDeath == -1) {
            _timeOfDeath = Terasology.getInstance().getTimeInMs();
            reset();
        } else if (isDead() && Terasology.getInstance().getTimeInMs() - _timeOfDeath > 1000) {
            revive();
            respawn();
            _timeOfDeath = -1;
        }
        PerformanceMonitor.endActivity();
    }

    /**
     * Processes interactions for the given mouse button.
     *
     * @param button The pressed mouse button
     */
    private void processInteractions(int button) {
        // Throttle interactions
        if (Terasology.getInstance().getTimeInMs() - _lastInteraction < 200) {
            return;
        }

        ITool activeTool = getActiveTool();

        if (activeTool != null) {
            // Check if one of the mouse buttons is pressed
            if (Mouse.isButtonDown(0) || button == 0) {
                activeTool.executeLeftClickAction();
                _lastInteraction = Terasology.getInstance().getTimeInMs();
                poke();
            } else if (Mouse.isButtonDown(1) || button == 1) {
                activeTool.executeRightClickAction();
                _lastInteraction = Terasology.getInstance().getTimeInMs();
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
        if (!RENDER_FIRST_PERSON_VIEW) {
            return;
        }

        glPushMatrix();
        glLoadIdentity();
        glClear(GL_DEPTH_BUFFER_BIT);
        getActiveCamera().loadProjectionMatrix(75f);

        if (getActiveItem() != null) {
            getActiveItem().renderFirstPersonView(this);
        } else {
            renderHand();
        }

        glPopMatrix();
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

        return java.lang.Math.sin(_stepCounter * frequency + phaseOffset) * amplitude;
    }

    public void updateCameraParameters() {
        _firstPersonCamera.getPosition().set(calcEyeOffset());

        if (CAMERA_BOBBING) {
            _firstPersonCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.5f));
            _firstPersonCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f, 0.025f, 1.25f));
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

    public void updatePosition(double delta) {
        // DEMO MODE
        if (DEMO_FLIGHT) {
            getPosition().z -= DEMO_FLIGHT_SPEED;

            int maxHeight = _parent.maxHeightAt((int) getPosition().x, (int) getPosition().z + 8) + 16;

            getPosition().y += (maxHeight - getPosition().y) / 64f;

            if (getPosition().y > 128)
                getPosition().y = 128;

            if (getPosition().y < 40f)
                getPosition().y = 40f;

            return;
        }

        super.updatePosition(delta);
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

                    PerformanceMonitor.startActivity("Player Get Block");
                    byte blockType = _parent.getWorldProvider().getBlock(blockPosX, blockPosY, blockPosZ);
                    PerformanceMonitor.endActivity();

                    // Ignore special blocks
                    if (BlockManager.getInstance().getBlock(blockType).isSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    PerformanceMonitor.startActivity("Player Intersect Block");
                    ArrayList<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(_parent.getWorldProvider(), blockPosX, blockPosY, blockPosZ, calcEyePosition(), _viewingDirection);


                    if (iss != null) {
                        inters.addAll(iss);
                    }
                    PerformanceMonitor.endActivity();
                }
            }
        }

        /**
         * Calculated the closest intersection.
         */
        PerformanceMonitor.startActivity("Player sort intersects");
        if (inters.size() > 0) {
            Collections.sort(inters);
            return inters.get(0);
        }
        PerformanceMonitor.endActivity();

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

                    if (Terasology.getInstance().getTimeInMs() - _lastTimeSpacePressed < 200) {
                        _godMode = !_godMode;
                    }

                    _lastTimeSpacePressed = Terasology.getInstance().getTimeInMs();
                }
                break;
            case Keyboard.KEY_1:
                _inventory.setSelctedCubbyhole(0);
                break;
            case Keyboard.KEY_2:
                _inventory.setSelctedCubbyhole(1);
                break;
            case Keyboard.KEY_3:
                _inventory.setSelctedCubbyhole(2);
                break;
            case Keyboard.KEY_4:
                _inventory.setSelctedCubbyhole(3);
                break;
            case Keyboard.KEY_5:
                _inventory.setSelctedCubbyhole(4);
                break;
            case Keyboard.KEY_6:
                _inventory.setSelctedCubbyhole(5);
                break;
            case Keyboard.KEY_7:
                _inventory.setSelctedCubbyhole(6);
                break;
            case Keyboard.KEY_8:
                _inventory.setSelctedCubbyhole(7);
                break;
            case Keyboard.KEY_9:
                _inventory.setSelctedCubbyhole(8);
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
            rollSelectedCubby((byte) (wheelMoved / 120));
        } else if (state && (button == 0 || button == 1)) {
            processInteractions(button);
        }
    }

    private void rollSelectedCubby(byte wheelMotion) {
        int selectedCubby = (_inventory.getSelectedCubbyhole() + wheelMotion) % 9;

        if (selectedCubby < 0) {
            selectedCubby = 9 + selectedCubby;
        }

        _inventory.setSelctedCubbyhole(selectedCubby);
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

        ShaderManager.getInstance().enableDefaultTextured();
        TextureManager.getInstance().bindTexture("effects");

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_DST_COLOR, GL_ZERO);

        glPushMatrix();
        glTranslated(_extractedBlock.getBlockPosition().x - getPosition().x, _extractedBlock.getBlockPosition().y - getPosition().y, _extractedBlock.getBlockPosition().z - getPosition().z);

        float offset = java.lang.Math.round(((float) _extractionCounter / block.getHardness()) * 10.0f) * 0.0625f;

        if (_overlayMesh == null) {
            Vector2f texPos = new Vector2f(0.0f, 0.0f);
            Vector2f texWidth = new Vector2f(0.0624f, 0.0624f);

            Tessellator tessellator = new Tessellator();
            TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
            _overlayMesh = tessellator.generateMesh();
        }

        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(offset, 0f, 0f);
        glMatrixMode(GL_MODELVIEW);

        _overlayMesh.render();

        glPopMatrix();

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glDisable(GL11.GL_BLEND);
    }

    /**
     * Renders a simple hand displayed in front of the player's first person perspective.
     */
    public void renderHand() {
        ShaderManager.getInstance().enableShader("block");
        TextureManager.getInstance().bindTexture("char");

        glPushMatrix();
        glTranslatef(0.8f, -1.1f + (float) calcBobbingOffset((float) java.lang.Math.PI / 8f, 0.05f, 2.5f) - _handMovementAnimationOffset * 0.5f, -1.0f - _handMovementAnimationOffset * 0.5f);
        glRotatef(-45f - _handMovementAnimationOffset * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(35f, 0.0f, 1.0f, 0.0f);
        glTranslatef(0f, 0.25f, 0f);
        glScalef(0.3f, 0.6f, 0.3f);

        if (_handMesh == null) {
            Vector2f texPos = new Vector2f(40.0f * 0.015625f, 32.0f * 0.03125f);
            Vector2f texWidth = new Vector2f(4.0f * 0.015625f, -12.0f * 0.03125f);

            Tessellator tessellator = new Tessellator();
            TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.0f, 1.0f, 0.9f, 0.0f, 0.0f, 0.0f);
            _handMesh = tessellator.generateMesh();
        }

        _handMesh.render();

        glPopMatrix();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writePropertiesToConfigObject(ConfigObject co) {
        co.put("playerPositionX", getPosition().x);
        co.put("playerPositionY", getPosition().y);
        co.put("playerPositionZ", getPosition().z);
        co.put("godMode", _godMode);
    }

    @Override
    public void readPropertiesFromConfigObject(ConfigObject co) {
        getPosition().x = ((BigDecimal) co.get("playerPositionX")).doubleValue();
        getPosition().y = ((BigDecimal) co.get("playerPositionY")).doubleValue();
        getPosition().z = ((BigDecimal) co.get("playerPositionZ")).doubleValue();
        _godMode = (Boolean) co.get("godMode");
    }

    @Override
    public String getObjectSavePath() {
        return Terasology.getInstance().getWorldSavePath(_parent.getWorldProvider().getTitle());
    }

    @Override
    public String getObjectFileName() {
        return "PlayerManifest.groovy";
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public Item getActiveItem() {
        return _inventory.getSelectedItem();
    }

    public BlockGroup getActiveBlock() {
        Item item = getActiveItem();

        if (item != null) {
            if (item.getClass() == ItemBlock.class) {
                return ((ItemBlock) getActiveItem()).getBlockGroup();
            }
        }

        return null;
    }

    public ITool getActiveTool() {
        if (getActiveItem() != null) {
            return _toolManager.getToolForIndex(getActiveItem().getToolId());
        }

        return _toolManager.getToolForIndex((byte) 0x01);
    }

    @Override
    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f | velEarth: %.2f | x: %.2f, y: %.2f, z: %.2f)", getPosition().x, getPosition().y, getPosition().z, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _earthVelocity, _movementDirection.x, _movementDirection.y, _movementDirection.z);
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

    public void registerObserver(IBlockObserver observer) {
        _observers.add(observer);
    }

    public void unregisterObserver(IBlockObserver observer) {
        _observers.remove(observer);
    }

    public void notifyObserversBlockPlaced(Chunk chunk, BlockPosition pos, boolean update) {
        for (IBlockObserver ob : _observers)
            ob.blockPlaced(chunk, pos, update);
    }

    public void notifyObserversBlockRemoved(Chunk chunk, BlockPosition pos, boolean update) {
        for (IBlockObserver ob : _observers)
            ob.blockRemoved(chunk, pos, update);
    }

    public Inventory getInventory() {
        return _inventory;
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

    public boolean isCarryingTorch() {
        if (getActiveBlock() != null) {
            if (getActiveBlock().getArchetypeBlock().getLuminance() > 0)
                return true;
        }

        return false;
    }
}