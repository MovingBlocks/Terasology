/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.asset.AssetUri;
import org.terasology.config.Config;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.binds.interaction.FrobButton;
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.input.binds.movement.ForwardsMovementAxis;
import org.terasology.input.binds.movement.JumpButton;
import org.terasology.input.binds.movement.StrafeMovementAxis;
import org.terasology.input.binds.movement.ToggleSpeedPermanentlyButton;
import org.terasology.input.binds.movement.ToggleSpeedTemporarilyButton;
import org.terasology.input.binds.movement.VerticalMovementAxis;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.AABB;
import org.terasology.math.Direction;
import org.terasology.math.QuaternionUtil;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.BlockOverlayRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.PerspectiveCamera;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

/**
 * @author Immortius
 */
// TODO: This needs a really good cleanup
// TODO: Move more input stuff to a specific input system?
// TODO: Camera should become an entity/component, so it can follow the player naturally
public class LocalPlayerSystem extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem {
    @In
    private LocalPlayer localPlayer;
    @In
    private CameraTargetSystem cameraTargetSystem;
    @In
    private WorldProvider worldProvider;
    private Camera playerCamera;

    @In
    private Physics physics;

    @In
    private Config config;
    private float bobFactor;
    private float lastStepDelta;

    // Input
    private Vector3f relativeMovement = new Vector3f();
    private boolean runPerDefault = true;
    private boolean run = runPerDefault;
    private boolean jump;
    private float lookPitch;
    private float lookYaw;

    @In
    private Time time;

    @In
    private NUIManager nuiManager;

    private long lastItemUse;

    private BlockOverlayRenderer aabbRenderer = new AABBRenderer(AABB.createEmpty());

    private int inputSequenceNumber = 1;

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
        CharacterComponent characterComp = entity.getComponent(CharacterComponent.class);
        LocationComponent location = entity.getComponent(LocationComponent.class);


        processInput(entity, characterComp, characterMovementComponent);
        updateCamera(characterComp, characterMovementComponent, characterComp, location);

        // Hand animation update
        characterComp.handAnimation = Math.max(0, characterComp.handAnimation - 2.5f * delta);

        entity.saveComponent(characterComp);
    }

    private void processInput(EntityRef entity, CharacterComponent characterComponent, CharacterMovementComponent characterMovementComponent) {
        Vector3f relMove = new Vector3f(relativeMovement);
        relMove.y = 0;

        Quat4f viewRot;
        switch (characterMovementComponent.mode) {
            case WALKING:
                viewRot = new Quat4f(TeraMath.DEG_TO_RAD * characterComponent.yaw, 0, 0);
                QuaternionUtil.quatRotate(viewRot, relMove, relMove);
                break;
            case CLIMBING:
                // Rotation is applied in KinematicCharacterMover
                break;
            default:
                viewRot = new Quat4f(TeraMath.DEG_TO_RAD * characterComponent.yaw, TeraMath.DEG_TO_RAD * characterComponent.pitch, 0);
                QuaternionUtil.quatRotate(viewRot, relMove, relMove);
                relMove.y += relativeMovement.y;
                break;
        }
        entity.send(new CharacterMoveInputEvent(inputSequenceNumber++, lookPitch, lookYaw, relMove, run, jump));
        jump = false;
    }

    private void updateCamera(CharacterComponent characterComponent, CharacterMovementComponent characterMovementComponent,
                              CharacterComponent characterComp, LocationComponent location) {
        // TODO: Remove, use component camera, breaks spawn camera anyway
        Quat4f lookRotation = new Quat4f(TeraMath.DEG_TO_RAD * characterComponent.yaw, TeraMath.DEG_TO_RAD * characterComponent.pitch, 0);
        updateCamera(characterComp, characterMovementComponent, location.getWorldPosition(), lookRotation);
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void onMouseX(MouseXAxisEvent event, EntityRef entity) {
        CharacterComponent characterComponent = entity.getComponent(CharacterComponent.class);
        lookYaw = (characterComponent.yaw - event.getValue()) % 360;
        event.consume();
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void onMouseY(MouseYAxisEvent event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        lookPitch = TeraMath.clamp(character.pitch + event.getValue(), -89, 89);
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

    @ReceiveEvent(components = {ClientComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onToggleSpeedTemporarily(ToggleSpeedTemporarilyButton event, EntityRef entity) {
        boolean toggle = event.isDown();
        run = runPerDefault ^ toggle;

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

    @Override
    public void renderOverlay() {
        // TODO: Don't render if not in first person?
        // Display the block the player is aiming at
        if (config.getRendering().isRenderPlacingBox()) {
            EntityRef target = cameraTargetSystem.getTarget();
            Vector3i blockPos = cameraTargetSystem.getTargetBlockPosition();
            AABB aabb = null;
            BlockComponent blockComp = target.getComponent(BlockComponent.class);
            BlockRegionComponent blockRegion = target.getComponent(BlockRegionComponent.class);
            if (blockComp != null || blockRegion != null) {
                Block block = worldProvider.getBlock(blockPos);
                aabb = block.getBounds(blockPos);
            } else {
                MeshComponent mesh = target.getComponent(MeshComponent.class);
                LocationComponent location = target.getComponent(LocationComponent.class);
                if (mesh != null && mesh.mesh != null && location != null) {
                    aabb = mesh.mesh.getAABB();
                    aabb = aabb.transform(location.getWorldRotation(), location.getWorldPosition(), location.getWorldScale());
                }
            }
            if (aabb != null) {
                aabbRenderer.setAABB(aabb);
                aabbRenderer.render(2f);
            }
        }
    }

    public void setAABBRenderer(BlockOverlayRenderer newAABBRender) {
        aabbRenderer = newAABBRender;
    }

    public BlockOverlayRenderer getAABBRenderer() {
        return aabbRenderer;
    }


    private void updateCamera(CharacterComponent characterComponent, CharacterMovementComponent charMovementComp, Vector3f position, Quat4f rotation) {
        // The camera position is the player's position plus the eye offset
        Vector3f cameraPosition = new Vector3f(position);
        cameraPosition.add(0, characterComponent.eyeOffset, 0);

        playerCamera.getPosition().set(cameraPosition);
        Vector3f viewDir = Direction.FORWARD.getVector3f();
        QuaternionUtil.quatRotate(rotation, viewDir, playerCamera.getViewingDirection());

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

        AssetUri activeInteractionScreenUri = InteractionUtil.getActiveInteractionScreenUri(character);
        if (activeInteractionScreenUri != null) {
            InteractionUtil.cancelInteractionAsClient(character);
            return;
        }
        boolean activeRequestSent = localPlayer.activateTargetAsClient();
        if (activeRequestSent) {
            event.consume();
        }
    }


    @ReceiveEvent(components = {CharacterComponent.class, InventoryComponent.class})
    public void onUseItemButton(UseItemButton event, EntityRef entity, CharacterComponent characterComponent) {
        if (!event.isDown() || time.getGameTimeInMs() - lastItemUse < 200) {
            return;
        }

        EntityRef selectedItemEntity = InventoryUtils.getItemAt(entity, characterComponent.selectedItem);
        if (!selectedItemEntity.exists()) {
            return;
        }

        localPlayer.activateOwnedEntityAsClient(selectedItemEntity);

        lastItemUse = time.getGameTimeInMs();
        characterComponent.handAnimation = 0.5f;
        entity.saveComponent(characterComponent);
        event.consume();
    }


    private float calcBobbingOffset(float phaseOffset, float amplitude, float frequency) {
        return (float) java.lang.Math.sin(bobFactor * frequency + phaseOffset) * amplitude;
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderAlphaBlend() {

    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

}
