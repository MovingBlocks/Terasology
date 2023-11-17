// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.binds.interaction.FrobButton;
import org.terasology.engine.input.binds.inventory.UseItemButton;
import org.terasology.engine.input.binds.movement.AutoMoveButton;
import org.terasology.engine.input.binds.movement.CrouchButton;
import org.terasology.engine.input.binds.movement.CrouchModeButton;
import org.terasology.engine.input.binds.movement.ForwardsMovementAxis;
import org.terasology.engine.input.binds.movement.ForwardsRealMovementAxis;
import org.terasology.engine.input.binds.movement.JumpButton;
import org.terasology.engine.input.binds.movement.RotationPitchAxis;
import org.terasology.engine.input.binds.movement.RotationYawAxis;
import org.terasology.engine.input.binds.movement.StrafeMovementAxis;
import org.terasology.engine.input.binds.movement.StrafeRealMovementAxis;
import org.terasology.engine.input.binds.movement.ToggleSpeedPermanentlyButton;
import org.terasology.engine.input.binds.movement.ToggleSpeedTemporarilyButton;
import org.terasology.engine.input.binds.movement.VerticalMovementAxis;
import org.terasology.engine.input.binds.movement.VerticalRealMovementAxis;
import org.terasology.engine.input.events.MouseAxisEvent;
import org.terasology.engine.input.events.MouseAxisEvent.MouseAxis;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.characters.MovementMode;
import org.terasology.engine.logic.characters.events.OnItemUseEvent;
import org.terasology.engine.logic.characters.events.ScaleToRequest;
import org.terasology.engine.logic.characters.interactions.InteractionUtil;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.AABBRenderer;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.cameras.PerspectiveCamera;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.joml.geom.AABBf;

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

    @In
    private Config config;
    @In
    private PlayerConfig playerConfig;
    @In
    private InputSystem inputSystem;

    @In
    private BindsManager bindsManager;

    private Camera playerCamera;
    private boolean localPlayerInitialized = false;

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
    private double lookPitchDelta;
    private float lookYaw;
    private double lookYawDelta;

    @In
    private Time time;

    private int inputSequenceNumber = 1;

    private AABBf aabb = new AABBf();
    private boolean hasTarget = false;

    public void setPlayerCamera(Camera camera) {
        playerCamera = camera;
    }

    @Override
    public void update(float delta) {
        if (!localPlayerInitialized && localPlayer.isValid()) {
            localPlayer.getClientEntity().send(new LocalPlayerInitializedEvent());
            localPlayerInitialized = true;
        }

        if (localPlayerInitialized) {
            EntityRef entity = localPlayer.getCharacterEntity();
            CharacterMovementComponent characterMovementComponent =
                    entity.getComponent(CharacterMovementComponent.class);

            processInput(entity, characterMovementComponent);
            updateCamera(characterMovementComponent, localPlayer.getViewPosition(new Vector3f()),
                    localPlayer.getViewRotation(new Quaternionf()));
        }
    }

    private void processInput(EntityRef entity, CharacterMovementComponent characterMovementComponent) {
        lookYaw = (float) ((lookYaw - lookYawDelta) % 360);
        lookYawDelta = 0f;
        lookPitch = (float) Math.clamp(-89, 89, lookPitch + lookPitchDelta);
        lookPitchDelta = 0f;

        Vector3f relMove = new Vector3f(relativeMovement);
        relMove.y = 0;

        switch (characterMovementComponent.mode) {
            case CROUCHING:
            case WALKING:
                playerCamera.getOrientation(new Quaternionf()).transform(relMove);
                break;
            case CLIMBING:
                // Rotation is applied in KinematicCharacterMover
                relMove.y += relativeMovement.y;
                break;
            default:
                playerCamera.getOrientation(new Quaternionf()).transform(relMove);
                relMove.y += relativeMovement.y;
                break;
        }
        // For some reason, Quat4f.rotate is returning NaN for valid inputs. This prevents those NaNs from causing
        // trouble down the line.
        if (relMove.isFinite()) {
            entity.send(new CharacterMoveInputEvent(inputSequenceNumber++, lookPitch, lookYaw, relMove, run, crouch,
                    jump, time.getGameDeltaInMs()));
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
     * Auto move is disabled when the associated key is pressed again. This cancels the simulated repeated key stroke
     * for the forward input button.
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
     * Append the input for moving forward to the keyboard command queue to simulate pressing of the forward key. For an
     * input that repeats, the key must be in Down state before Repeat state can be applied to it.
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
            // update character height as given in player settings
            ScaleToRequest scaleRequest = new ScaleToRequest(playerConfig.height.get());
            localPlayer.getCharacterEntity().send(scaleRequest);

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

    @ReceiveEvent(components = CharacterComponent.class)
    public void updateRotationYaw(RotationYawAxis event, EntityRef entity) {
        lookYawDelta = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void updateRotationPitch(RotationPitchAxis event, EntityRef entity) {
        lookPitchDelta = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void setRotation(SetDirectionEvent event, EntityRef entity) {
        if (localPlayer.getCharacterEntity().equals(entity)) {
            lookPitch = event.getPitch();
            lookYaw = event.getYaw();
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, CharacterMovementComponent.class})
    public void onJump(JumpButton event, EntityRef entity) {
        if (event.isDown()) {
            jump = true;
            event.consume();
        } else {
            jump = false;
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void updateForwardsMovement(ForwardsMovementAxis event, EntityRef entity) {
        relativeMovement.z = (float) event.getValue();
        if (relativeMovement.z == 0f && isAutoMove) {
            stopAutoMove();
        }
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void updateStrafeMovement(StrafeMovementAxis event, EntityRef entity) {
        relativeMovement.x = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void updateVerticalMovement(VerticalMovementAxis event, EntityRef entity) {
        relativeMovement.y = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void updateForwardsMovement(ForwardsRealMovementAxis event, EntityRef entity) {
        relativeMovement.z = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void updateStrafeMovement(StrafeRealMovementAxis event, EntityRef entity) {
        relativeMovement.x = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void updateVerticalMovement(VerticalRealMovementAxis event, EntityRef entity) {
        relativeMovement.y = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleSpeedTemporarily(ToggleSpeedTemporarilyButton event, EntityRef entity) {
        boolean toggle = event.isDown();
        run = runPerDefault ^ toggle;
        event.consume();
    }

    // Crouches if button is pressed. Stands if button is released.
    @ReceiveEvent(components = ClientComponent.class)
    public void onCrouchTemporarily(CrouchButton event, EntityRef entity) {
        boolean toggle = event.isDown();
        crouch = crouchPerDefault ^ toggle;
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onCrouchMode(CrouchModeButton event, EntityRef entity) {
        if (event.isDown()) {
            crouchPerDefault = !crouchPerDefault;
            crouch = !crouch;
        }
        event.consume();
    }

    @ReceiveEvent(components = ClientComponent.class)
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

    @ReceiveEvent(components = ClientComponent.class)
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
        hasTarget = target.exists();
        if (hasTarget) {
            LocationComponent location = target.getComponent(LocationComponent.class);
            if (location != null) {
                BlockComponent blockComp = target.getComponent(BlockComponent.class);
                BlockRegionComponent blockRegion = target.getComponent(BlockRegionComponent.class);
                if (blockComp != null || blockRegion != null) {
                    Vector3f blockPos = location.getWorldPosition(new Vector3f());
                    Block block = worldProvider.getBlock(blockPos);
                    aabb.set(block.getBounds(blockPos));
                } else {
                    MeshComponent mesh = target.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.mesh != null) {
                        aabb.set(mesh.mesh.getAABB());
                        aabb.transform(new Matrix4f().translationRotateScale(
                                location.getWorldPosition(new Vector3f()),
                                location.getWorldRotation(new Quaternionf()),
                                location.getWorldScale()));
                    }
                }
            }
        }
    }

    @Override
    public void renderOverlay() {
        // Display the block the player is aiming at
        if (config.getRendering().isRenderPlacingBox() && hasTarget) {
            try (AABBRenderer renderer = new AABBRenderer(aabb)) {
                renderer.render();
            }
        }
    }

    private void updateCamera(CharacterMovementComponent charMovementComp, Vector3f position, Quaternionf rotation) {
        playerCamera.getPosition().set(position);
        playerCamera.setOrientation(rotation);

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

    @ReceiveEvent(components = CharacterComponent.class)
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

    @ReceiveEvent(components = CharacterComponent.class)
    public void onUseItemButton(UseItemButton event, EntityRef entity,
                                CharacterHeldItemComponent characterHeldItemComponent) {
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


    /**
     * Special getter that fetches the client entity via the NetworkSystem instead of the LocalPlayer. This can be
     * needed in special cases where the local player isn't fully available (TODO: May be a bug?)
     *
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
