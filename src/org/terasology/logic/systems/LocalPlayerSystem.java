package org.terasology.logic.systems;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.manager.Config;
import org.terasology.math.TeraMath;
import org.terasology.rendering.cameras.DefaultCamera;

import javax.vecmath.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayerSystem {
    private EntityManager entityManager;
    private DefaultCamera playerCamera;
    
    private boolean jump = false;
    private Vector3f movementInput = new Vector3f();
    private Vector2f lookInput = new Vector2f();
    private boolean running = false;

    private double mouseSensititivy = Config.getInstance().getMouseSens();
    
    public LocalPlayerSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(LocalPlayerComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocalPlayerComponent localPlayerComponent = entity.getComponent(LocalPlayerComponent.class);
            CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            // Update look (pitch/yaw)
            localPlayerComponent.pitch = TeraMath.clamp(localPlayerComponent.pitch + lookInput.y, -89, 89);
            localPlayerComponent.yaw = (localPlayerComponent.yaw - lookInput.x) % 360;

            QuaternionUtil.setEuler(location.getLocalRotation(), TeraMath.DEG_TO_RAD * localPlayerComponent.yaw, 0, 0);

            // Update movement drive
            Vector3f relMove = QuaternionUtil.quatRotate(location.getLocalRotation(), movementInput, new Vector3f());
            characterMovementComponent.setDrive(relMove);
            characterMovementComponent.jump = jump;

            // TODO: Remove, use component camera, breaks spawn camera anyway
            Quat4f lookRotation = new Quat4f();
            QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * localPlayerComponent.yaw, TeraMath.DEG_TO_RAD * localPlayerComponent.pitch, 0);
            updateCamera(location.getWorldPosition(), lookRotation);
        }
        jump = false;
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

                    // TODO: God mode
                    /*if (Terasology.getInstance().getTimeInMs() - _lastTimeSpacePressed < 200) {
                        _godMode = !_godMode;
                    }

                    _lastTimeSpacePressed = Terasology.getInstance().getTimeInMs();
                    */
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
}
