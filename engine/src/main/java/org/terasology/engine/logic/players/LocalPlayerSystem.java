// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.input.binds.interaction.FrobButton;
import org.terasology.engine.input.binds.inventory.UseItemButton;
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
import org.terasology.engine.logic.players.event.OnPlayerSpawnedOrRestoredEvent;
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
import org.terasology.input.ButtonState;
import org.terasology.joml.geom.AABBf;

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
    private EntityManager entityManager;

    private Camera playerCamera;

    private float bobFactor;
    private float lastStepDelta;

    @In
    private Time time;

    private final AABBf aabb = new AABBf();
    private boolean hasTarget = false;

    public void setPlayerCamera(Camera camera) {
        playerCamera = camera;
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(CharacterMovementComponent.class, LocalPlayerControlComponent.class)) {
            processInput(entity);
        }

        for (EntityRef entity : entityManager.getEntitiesWith(CharacterMovementComponent.class, LocalPlayerComponent.class)) {
            updateCamera(entity.getComponent(CharacterMovementComponent.class), localPlayer.getViewPosition(new Vector3f()),
                    localPlayer.getViewRotation(new Quaternionf()));
        }
    }

    private void processInput(EntityRef entity) {
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        LocalPlayerControlComponent controlComponent = entity.getComponent(LocalPlayerControlComponent.class);
        controlComponent.lookYaw = (float) ((controlComponent.lookYaw - controlComponent.lookYawDelta) % 360);
        controlComponent.lookYawDelta = 0f;
        controlComponent.lookPitch = (float) Math.clamp(-89, 89, controlComponent.lookPitch + controlComponent.lookPitchDelta);
        controlComponent.lookPitchDelta = 0f;

        Vector3f relMove = new Vector3f(controlComponent.relativeMovement);
        relMove.y = 0;

        Quaternionf viewRotation = new Quaternionf();
        switch (characterMovementComponent.mode) {
            case CROUCHING:
            case WALKING:
                if (!config.getRendering().isVrSupport()) {
                    viewRotation.rotationYXZ(Math.toRadians(controlComponent.lookYaw), 0, 0);
                    playerCamera.setOrientation(viewRotation);
                }
                playerCamera.getOrientation(new Quaternionf()).transform(relMove);
                break;
            case CLIMBING:
                // Rotation is applied in KinematicCharacterMover
                relMove.y += controlComponent.relativeMovement.y;
                break;
            default:
                if (!config.getRendering().isVrSupport()) {
                    viewRotation.rotationYXZ(Math.toRadians(controlComponent.lookYaw), Math.toRadians(controlComponent.lookPitch), 0);
                    playerCamera.setOrientation(viewRotation);
                }
                playerCamera.getOrientation(new Quaternionf()).transform(relMove);
                relMove.y += controlComponent.relativeMovement.y;
                break;
        }
        // For some reason, Quat4f.rotate is returning NaN for valid inputs. This prevents those NaNs from causing
        // trouble down the line.
        if (relMove.isFinite()) {
            entity.send(new CharacterMoveInputEvent(controlComponent.inputSequenceNumber++, controlComponent.lookPitch,
                    controlComponent.lookYaw, relMove,
                    controlComponent.run, controlComponent.crouch, controlComponent.jump, time.getGameDeltaInMs()));
        }
        controlComponent.jump = false;
    }

    @ReceiveEvent
    public void onClientComponentChange(OnChangedComponent event, EntityRef entity, ClientComponent clientComponent) {
        // character is set by PlayerSystem just before the player is spawned.
        if (clientComponent.local && clientComponent.character != null
                && !clientComponent.character.hasComponent(LocalPlayerComponent.class)) {
            clientComponent.character.addComponent(new LocalPlayerComponent());
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef character) {
        // update character height as given in player settings
        ScaleToRequest scaleRequest = new ScaleToRequest(playerConfig.height.get());
        localPlayer.getCharacterEntity().send(scaleRequest);
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onPlayerSpawnedOrRestored(OnPlayerSpawnedOrRestoredEvent event, EntityRef character) {
        character.send(new LocalPlayerInitializedEvent());

        // Trigger updating the player camera position as soon as the local player is spawned.
        // This is not done while the game is still loading, since systems are not updated.
        // RenderableWorldImpl pre-generates chunks around the player camera and therefore needs
        // the correct location.
        character.getComponent(LocalPlayerControlComponent.class).lookYaw = 0f;
        character.getComponent(LocalPlayerControlComponent.class).lookPitch = 0f;
        update(0);
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
                ((PerspectiveCamera) playerCamera).setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f,
                        0.025f, 3f));
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
     * Special getter that fetches the client entity via the NetworkSystem instead of the LocalPlayer. This can be needed in special cases
     * where the local player isn't fully available (TODO: May be a bug?)
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
