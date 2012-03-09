package org.terasology.logic.systems;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.Config;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.TeraMath;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.cameras.DefaultCamera;

import javax.vecmath.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayerSystem {
    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    private DefaultCamera playerCamera;
    
    private boolean jump = false;
    private Vector3f movementInput = new Vector3f();
    private Vector2f lookInput = new Vector2f();
    private boolean running = false;

    private double mouseSensititivy = Config.getInstance().getMouseSens();
    private float lastTimeSpacePressed;
    private boolean toggleGodMode;
    
    public LocalPlayerSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(LocalPlayerComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocalPlayerComponent localPlayerComponent = entity.getComponent(LocalPlayerComponent.class);
            CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            // Update look (pitch/yaw)
            localPlayerComponent.viewPitch = TeraMath.clamp(localPlayerComponent.viewPitch + lookInput.y, -89, 89);
            localPlayerComponent.viewYaw = (localPlayerComponent.viewYaw - lookInput.x) % 360;

            QuaternionUtil.setEuler(location.getLocalRotation(), TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, 0, 0);

            // Update movement drive
            Vector3f relMove = new Vector3f(movementInput);
            relMove.y = 0;
            if (characterMovementComponent.isGhosting || characterMovementComponent.isSwimming) {
                Quat4f viewRot = new Quat4f();
                QuaternionUtil.setEuler(viewRot, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
                QuaternionUtil.quatRotate(viewRot, relMove, relMove);
                relMove.y += movementInput.y;
            } else {
                QuaternionUtil.quatRotate(location.getLocalRotation(), relMove, relMove);
            }
            float lengthSquared = relMove.lengthSquared();
            if (lengthSquared > 1) relMove.normalize();
            characterMovementComponent.setDrive(relMove);

            characterMovementComponent.jump = jump;
            characterMovementComponent.isRunning = running;
            if (toggleGodMode) {
                characterMovementComponent.isGhosting = !characterMovementComponent.isGhosting;
            }

            // TODO: Remove, use component camera, breaks spawn camera anyway
            Quat4f lookRotation = new Quat4f();
            QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
            updateCamera(location.getWorldPosition(), lookRotation);
        }
        jump = false;
        toggleGodMode = false;
    }

    private void updateCamera(Vector3f position, Quat4f rotation) {
        // The camera position is the player's position plus the eye offset
        Vector3d cameraPosition = new Vector3d();
        // TODO: don't hardset eye position
        cameraPosition.add(new Vector3d(position), new Vector3d(0,0.6f,0));

        playerCamera.getPosition().set(cameraPosition);
        Vector3f viewDir = new Vector3f(0,0,-1);
        QuaternionUtil.quatRotate(rotation, viewDir, viewDir);
        playerCamera.getViewingDirection().set(viewDir);

        /*if (CAMERA_BOBBING) {
            _defaultCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.5f));
            _defaultCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f, 0.025f, 3.0f));
        } else {
            _defaultCamera.setBobbingRotationOffsetFactor(0.0);
            _defaultCamera.setBobbingVerticalOffsetFactor(0.0);
        } */

        /*if (!(DEMO_FLIGHT)) {
            _defaultCamera.getViewingDirection().set(getViewingDirection());
        } else {
            Vector3d viewingTarget = new Vector3d(getPosition().x, 40, getPosition().z - 128);
            _defaultCamera.getViewingDirection().sub(viewingTarget, getPosition());
        } */
    }

    public void render() {
        // Display the block the player is aiming at
        if (Config.getInstance().isPlacingBox()) {
            RayBlockIntersection.Intersection selectedBlock = calcSelectedBlock();
            if (selectedBlock != null) {
                Block block = BlockManager.getInstance().getBlock(worldProvider.getBlockAtPosition(selectedBlock.getBlockPosition().toVector3d()));
                if (block.isRenderBoundingBox()) {
                    block.getBounds(selectedBlock.getBlockPosition()).render(2f);
                }
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
            /*case Keyboard.KEY_K:
                if (!repeatEvent && state) {
                    damage(9999);
                }
                break;*/
            case Keyboard.KEY_SPACE:
                if (!repeatEvent && state) {
                    jump = true;

                    // TODO: handle time better
                    if (Terasology.getInstance().getTimeInMs() - lastTimeSpacePressed < 200) {
                        toggleGodMode = true;
                    }

                    lastTimeSpacePressed = Terasology.getInstance().getTimeInMs();
                }
                break;
            // TODO: Inventory selection switch
            /*case Keyboard.KEY_1:
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
              */
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
        /*if (isDead())
            return;

        if (wheelMoved != 0) {
            rollSelectedCubby((byte) (wheelMoved / 120));
        } else if (state && (button == 0 || button == 1)) {
            processInteractions(button);
        } */
    }

    public void updateInput() {
        //if (isDead())
        //    return;

        // Process interactions even if the mouse button is pressed down
        // and not fired by a repeated event
        //processInteractions(-1);

        movementInput.set(0, 0, 0);
        lookInput.set((float)(mouseSensititivy * Mouse.getDX()), (float)(mouseSensititivy * Mouse.getDY()));
        
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            movementInput.z -= 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            movementInput.z += 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            movementInput.x -= 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            movementInput.x += 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            movementInput.y += 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            movementInput.y -= 1.0f;
        }

        running = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    public void renderExtractionOverlay() {
        /*if (_extractionCounter <= 0 || _extractedBlock == null)
            return;

        Block block = BlockManager.getInstance().getBlock(_parent.getWorldProvider().getBlockAtPosition(_extractedBlock.getBlockPosition().toVector3d()));

        ShaderManager.getInstance().enableDefaultTextured();
        TextureManager.getInstance().bindTexture("effects");

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_DST_COLOR, GL_ZERO);

        Vector3d cameraPosition = Terasology.getInstance().getActiveCamera().getPosition();

        glPushMatrix();
        glTranslated(_extractedBlock.getBlockPosition().x - cameraPosition.x, _extractedBlock.getBlockPosition().y - cameraPosition.y, _extractedBlock.getBlockPosition().z - cameraPosition.z);

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

        glDisable(GL11.GL_BLEND);  */
    }

    public void renderFirstPersonViewElements() {
        /*if (!RENDER_FIRST_PERSON_VIEW) {
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

        glPopMatrix();*/
    }

    public void setPlayerCamera(DefaultCamera camera) {
        this.playerCamera = camera;
    }

    public void setWorldProvider(IWorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    /**
     * Calculates the currently targeted block in front of the player.
     *
     * @return Intersection point of the targeted block
     */
    private RayBlockIntersection.Intersection calcSelectedBlock() {
        // TODO: Proper and centralised ray tracing support though world
        List<RayBlockIntersection.Intersection> inters = new ArrayList<RayBlockIntersection.Intersection>();

        Vector3f pos = new Vector3f(playerCamera.getPosition());
        
        int blockPosX, blockPosY, blockPosZ;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    // Make sure the correct block positions are calculated relatively to the position of the player
                    blockPosX = (int) (pos.x + (pos.x >= 0 ? 0.5f : -0.5f)) + x;
                    blockPosY = (int) (pos.y + (pos.y >= 0 ? 0.5f : -0.5f)) + y;
                    blockPosZ = (int) (pos.z + (pos.z >= 0 ? 0.5f : -0.5f)) + z;

                    byte blockType = worldProvider.getBlock(blockPosX, blockPosY, blockPosZ);

                    // Ignore special blocks
                    if (BlockManager.getInstance().getBlock(blockType).isSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    List<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(worldProvider, blockPosX, blockPosY, blockPosZ, playerCamera.getPosition(), playerCamera.getViewingDirection());

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
}
