/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.players;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.input.binds.interaction.FrobButton;
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.input.binds.movement.AutoMoveButton;
import org.terasology.input.binds.movement.CrouchButton;
import org.terasology.input.binds.movement.CrouchModeButton;
import org.terasology.input.binds.movement.ForwardsMovementAxis;
import org.terasology.input.binds.movement.ForwardsRealMovementAxis;
import org.terasology.input.binds.movement.JumpButton;
import org.terasology.input.binds.movement.RotationPitchAxis;
import org.terasology.input.binds.movement.RotationYawAxis;
import org.terasology.input.binds.movement.StrafeMovementAxis;
import org.terasology.input.binds.movement.StrafeRealMovementAxis;
import org.terasology.input.binds.movement.ToggleSpeedPermanentlyButton;
import org.terasology.input.binds.movement.ToggleSpeedTemporarilyButton;
import org.terasology.input.binds.movement.VerticalMovementAxis;
import org.terasology.input.binds.movement.VerticalRealMovementAxis;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseAxisEvent.MouseAxis;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.GazeMountPointComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.OnItemUseEvent;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.debug.MovementDebugCommands;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.AABB;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.registry.In;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.BlockOverlayRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.List;

// TODO: This needs a really good cleanup
// TODO: Move more input stuff to a specific input system?
// TODO: Camera should become an entity/component, so it can follow the player naturally
public class LocalPlayerSystem extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem {

    @In
    NetworkSystem networkSystem;
    @In
    private LocalPlayer localPlayer;
    @In
    private WorldProvider worldProvider;
    private Camera playerCamera;
    @In
    private MovementDebugCommands movementDebugCommands;
    @In
    private PhysicsEngine physics;
    @In
    private DelayManager delayManager;

    @In
    private Config config;
    @In
    private InputSystem inputSystem;

    @In
    private BindsManager bindsManager;

    private float bobFactor;
    private float lastStepDelta;

    // Input
    private Vector3f relativeMovement = new Vector3f();
    private boolean isAutoMove;
    private boolean runPerDefault = true;
    private boolean run = runPerDefault;
    private boolean crouchPerDefault = false;
    private boolean crouch = false;

    private boolean jump;
    private float lookPitch;
    private float lookPitchDelta;
    private float lookYaw;
    private float lookYawDelta;

    @In
    private Time time;

    private BlockOverlayRenderer aabbRenderer = new AABBRenderer(AABB.createEmpty());

    private int inputSequenceNumber = 1;

    private AABB aabb;

    public void setPlayerCamera(Camera camera) {
        playerCamera = camera;
    }

    @Override
    public void update(float delta) {
        if (!localPlayer.isValid()) {
            return;
        }

        EntityRef entity = localPlayer.getCharacterEntity();
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);

        processInput(entity, characterMovementComponent);
        updateCamera(characterMovementComponent, localPlayer.getViewPosition(), localPlayer.getViewRotation());
    }

    private void processInput(EntityRef entity, CharacterMovementComponent characterMovementComponent) {
        lookYaw = (lookYaw - lookYawDelta) % 360;
        lookYawDelta = 0f;
        lookPitch = TeraMath.clamp(lookPitch + lookPitchDelta, -89, 89);
        lookPitchDelta = 0f;

        Vector3f relMove = new Vector3f(relativeMovement);
        relMove.y = 0;

        Quat4f viewRotation;
        switch (characterMovementComponent.mode) {
            case CROUCHING:
            case WALKING:
                if (!config.getRendering().isVrSupport()) {
                    viewRotation = new Quat4f(TeraMath.DEG_TO_RAD * lookYaw, 0, 0);
                    playerCamera.setOrientation(viewRotation);
                }
                playerCamera.getOrientation().rotate(relMove, relMove);
                break;
            case CLIMBING:
                // Rotation is applied in KinematicCharacterMover
                relMove.y += relativeMovement.y;
                break;
            default:
                if (!config.getRendering().isVrSupport()) {
                    viewRotation = new Quat4f(TeraMath.DEG_TO_RAD * lookYaw, TeraMath.DEG_TO_RAD * lookPitch, 0);
                    playerCamera.setOrientation(viewRotation);
                }
                playerCamera.getOrientation().rotate(relMove, relMove);
                relMove.y += relativeMovement.y;
                break;
        }
        // For some reason, Quat4f.rotate is returning NaN for valid inputs. This prevents those NaNs from causing trouble down the line.
        if (!Float.isNaN(relMove.getX()) && !Float.isNaN(relMove.getY()) && !Float.isNaN(relMove.getZ())) {
            entity.send(new CharacterMoveInputEvent(inputSequenceNumber++, lookPitch, lookYaw, relMove, run, crouch, jump, time.getGameDeltaInMs()));
        }
        jump = false;
    }

    // To check if a valid key has been assigned, either primary or secondary and return it
    private Input getValidKey(List<Input> inputs) {
        for (Input input : inputs) {
            if (input != null) {
                return input;
            }
        }
        return null;
    }

    /**
     * Auto move is disabled when the associated key is pressed again.
     * This cancels the simulated repeated key stroke for the forward input button.
     */
    private void stopAutoMove() {
        List<Input> inputs = bindsManager.getBindsConfig().getBinds(new SimpleUri("engine:forwards"));
        Input forwardKey = getValidKey(inputs);
        if (forwardKey != null) {
            inputSystem.cancelSimulatedKeyStroke(forwardKey);
            isAutoMove = false;
        }

    }

    /**
     * Append the input for moving forward to the keyboard command queue to simulate pressing of the forward key.
     * For an input that repeats, the key must be in Down state before Repeat state can be applied to it.
     */
    private void startAutoMove() {
        isAutoMove = false;
        List<Input> inputs = bindsManager.getBindsConfig().getBinds(new SimpleUri("engine:forwards"));
        Input forwardKey = getValidKey(inputs);
        if (forwardKey != null) {
            isAutoMove = true;
            inputSystem.simulateSingleKeyStroke(forwardKey);
            inputSystem.simulateRepeatedKeyStroke(forwardKey);
        }
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef character) {
        if (character.equals(localPlayer.getCharacterEntity())) {

            // Change height as per PlayerSettings
            Float height = config.getPlayer().getHeight();
            movementDebugCommands.playerHeight(localPlayer.getClientEntity(), height);
            // Change eyeHeight as per PlayerSettings
            Float eyeHeight = config.getPlayer().getEyeHeight();
            GazeMountPointComponent gazeMountPointComponent = character.getComponent(GazeMountPointComponent.class);
            gazeMountPointComponent.translate = new Vector3f(0, eyeHeight, 0);

            // Trigger updating the player camera position as soon as the local player is spawned.
            // This is not done while the game is still loading, since systems are not updated.
            // RenderableWorldImpl pre-generates chunks around the player camera and therefore needs
            // the correct location.
            lookYaw = 0f;
            lookPitch = 0f;
            update(0);
        }
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void onMouseMove(MouseAxisEvent event, EntityRef entity) {
        MouseAxis axis = event.getMouseAxis();
        if (axis == MouseAxis.X) {
            lookYawDelta = event.getValue();
        } else if (axis == MouseAxis.Y) {
            lookPitchDelta = event.getValue();
        }
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void updateRotationYaw(RotationYawAxis event, EntityRef entity) {
        lookYawDelta = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void updateRotationPitch(RotationPitchAxis event, EntityRef entity) {
        lookPitchDelta = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, CharacterMovementComponent.class})
    public void onJump(JumpButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            jump = true;
            event.consume();
        } else {
            jump = false;
        }
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void updateForwardsMovement(ForwardsMovementAxis event, EntityRef entity) {
        relativeMovement.z = event.getValue();
        if (relativeMovement.z == 0f && isAutoMove) {
            stopAutoMove();
        }
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void updateStrafeMovement(StrafeMovementAxis event, EntityRef entity) {
        relativeMovement.x = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void updateVerticalMovement(VerticalMovementAxis event, EntityRef entity) {
        relativeMovement.y = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void updateForwardsMovement(ForwardsRealMovementAxis event, EntityRef entity) {
        relativeMovement.z = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void updateStrafeMovement(StrafeRealMovementAxis event, EntityRef entity) {
        relativeMovement.x = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class})
    public void updateVerticalMovement(VerticalRealMovementAxis event, EntityRef entity) {
        relativeMovement.y = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onToggleSpeedTemporarily(ToggleSpeedTemporarilyButton event, EntityRef entity) {
        boolean toggle = event.isDown();
        run = runPerDefault ^ toggle;
        event.consume();
    }

    // Crouches if button is pressed. Stands if button is released.
    @ReceiveEvent(components = {ClientComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onCrouchTemporarily(CrouchButton event, EntityRef entity) {
        boolean toggle = event.isDown();
        crouch = crouchPerDefault ^ toggle;
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onCrouchMode(CrouchModeButton event, EntityRef entity) {
        if (event.isDown()) {
            crouchPerDefault = !crouchPerDefault;
            crouch = !crouch;
        }
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onAutoMoveMode(AutoMoveButton event, EntityRef entity) {
        if (event.isDown()) {
            if (!isAutoMove) {
                startAutoMove();
            } else {
                stopAutoMove();
            }
        }
        event.consume();
    }

    @ReceiveEvent(components = {ClientComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onToggleSpeedPermanently(ToggleSpeedPermanentlyButton event, EntityRef entity) {
        if (event.isDown()) {
            runPerDefault = !runPerDefault;
            run = !run;
        }
        event.consume();
    }

    @ReceiveEvent
    public void onTargetChanged(PlayerTargetChangedEvent event, EntityRef entity) {
        EntityRef target = event.getNewTarget();
        if (target.exists()) {
            LocationComponent location = target.getComponent(LocationComponent.class);
            if (location != null) {
                BlockComponent blockComp = target.getComponent(BlockComponent.class);
                BlockRegionComponent blockRegion = target.getComponent(BlockRegionComponent.class);
                if (blockComp != null || blockRegion != null) {
                    Vector3f blockPos = location.getWorldPosition();
                    Block block = worldProvider.getBlock(blockPos);
                    aabb = block.getBounds(blockPos);
                } else {
                    MeshComponent mesh = target.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.mesh != null) {
                        aabb = mesh.mesh.getAABB();
                        aabb = aabb.transform(location.getWorldRotation(), location.getWorldPosition(), location.getWorldScale());
                    }
                }
            }
        } else {
            aabb = null;
        }
    }

    @Override
    public void renderOverlay() {
        // Display the block the player is aiming at
        if (config.getRendering().isRenderPlacingBox()) {
            if (aabb != null) {
                aabbRenderer.setAABB(aabb);
                aabbRenderer.render(2f);
            }
        }
    }

    public BlockOverlayRenderer getAABBRenderer() {
        return aabbRenderer;
    }

    public void setAABBRenderer(BlockOverlayRenderer newAABBRender) {
        aabbRenderer = newAABBRender;
    }

    private void updateCamera(CharacterMovementComponent charMovementComp, Vector3f position, Quat4f rotation) {
        playerCamera.getPosition().set(position);
        Vector3f viewDir = Direction.FORWARD.getVector3f();
        rotation.rotate(viewDir, playerCamera.getViewingDirection());

        float stepDelta = charMovementComp.footstepDelta - lastStepDelta;
        if (stepDelta < 0) {
            stepDelta += 1;
        }
        bobFactor += stepDelta;
        lastStepDelta = charMovementComp.footstepDelta;

        if (playerCamera.isBobbingAllowed()) {
            if (config.getRendering().isCameraBobbing()) {
                ((PerspectiveCamera) playerCamera).setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.5f));
                ((PerspectiveCamera) playerCamera).setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f, 0.025f, 3f));
            } else {
                ((PerspectiveCamera) playerCamera).setBobbingRotationOffsetFactor(0.0f);
                ((PerspectiveCamera) playerCamera).setBobbingVerticalOffsetFactor(0.0f);
            }
        }

        if (charMovementComp.mode == MovementMode.GHOSTING) {
            playerCamera.extendFov(24);
        } else {
            playerCamera.resetFov();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onFrobButton(FrobButton event, EntityRef character) {
        if (event.getState() != ButtonState.DOWN) {
            return;
        }

        ResourceUrn activeInteractionScreenUri = InteractionUtil.getActiveInteractionScreenUri(character);
        if (activeInteractionScreenUri != null) {
            InteractionUtil.cancelInteractionAsClient(character);
            return;
        }
        boolean activeRequestSent = localPlayer.activateTargetAsClient();
        if (activeRequestSent) {
            event.consume();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onUseItemButton(UseItemButton event, EntityRef entity, CharacterHeldItemComponent characterHeldItemComponent) {
        if (!event.isDown()) {
            return;
        }

        EntityRef selectedItemEntity = characterHeldItemComponent.selectedItem;
        if (!selectedItemEntity.exists()) {
            return;
        }

        boolean requestIsValid;
        if (networkSystem.getMode().isAuthority()) {
            // Let the ActivationRequest handler trigger the OnItemUseEvent if this is a local client
            requestIsValid = true;
        } else {
            OnItemUseEvent onItemUseEvent = new OnItemUseEvent();
            entity.send(onItemUseEvent);
            requestIsValid = !onItemUseEvent.isConsumed();
        }

        if (requestIsValid) {
            localPlayer.activateOwnedEntityAsClient(selectedItemEntity);
            entity.saveComponent(characterHeldItemComponent);
            event.consume();
        }
    }

    private float calcBobbingOffset(float phaseOffset, float amplitude, float frequency) {
        return (float) java.lang.Math.sin((double) bobFactor * frequency + phaseOffset) * amplitude;
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {

    }

    @Override
    public void renderShadows() {
    }

    /**
     * Special getter that fetches the client entity via the NetworkSystem instead of the LocalPlayer.
     * This can be needed in special cases where the local player isn't fully available (TODO: May be a bug?)
     * @return the EntityRef that the networking system says is the client associated with this player
     */
    public EntityRef getClientEntityViaNetworkSystem() {
        if (networkSystem.getMode() != NetworkMode.NONE && networkSystem.getServer() != null) {
            return networkSystem.getServer().getClientEntity();
        } else {
            return null;
        }
    }
}
