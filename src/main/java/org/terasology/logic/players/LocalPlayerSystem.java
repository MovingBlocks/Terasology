/*
 * Copyright 2013 Moving Blocks
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

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.config.Config;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.input.ButtonState;
import org.terasology.input.CameraTargetSystem;
import org.terasology.input.binds.ForwardsMovementAxis;
import org.terasology.input.binds.FrobButton;
import org.terasology.input.binds.JumpButton;
import org.terasology.input.binds.RunButton;
import org.terasology.input.binds.StrafeMovementAxis;
import org.terasology.input.binds.VerticalMovementAxis;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.FrobRequest;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.math.AABB;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.BlockOverlayRenderer;
import org.terasology.rendering.cameras.DefaultCamera;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.regions.BlockRegionComponent;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: This needs a really good cleanup
// TODO: Move more input stuff to a specific input system?
// TODO: Camera should become an entity/component, so it can follow the player naturally
public class LocalPlayerSystem implements UpdateSubscriberSystem, RenderSystem {
    private LocalPlayer localPlayer;
    private CameraTargetSystem cameraTargetSystem;
    private Time time;

    private WorldProvider worldProvider;
    private DefaultCamera playerCamera;

    private long lastTimeSpacePressed;
    private long lastInteraction, lastTimeThrowInteraction;

    @In
    private Config config;
    private float bobFactor = 0;
    private float lastStepDelta = 0;

    // Input
    private Vector3f relativeMovement = new Vector3f();
    private boolean run = false;
    private boolean jump = false;
    private float lookPitch = 0;
    private float lookYaw = 0;

    private BlockOverlayRenderer aabbRenderer = new AABBRenderer(AABB.createEmpty());

    private int inputSequenceNumber = 1;

    @In
    private SlotBasedInventoryManager inventoryManager;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        time = CoreRegistry.get(Time.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
    }

    @Override
    public void shutdown() {
    }

    public void setPlayerCamera(DefaultCamera camera) {
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

        Quat4f viewRot = new Quat4f();
        switch (characterMovementComponent.mode) {
            case WALKING:
                QuaternionUtil.setEuler(viewRot, TeraMath.DEG_TO_RAD * characterComponent.yaw, 0, 0);
                QuaternionUtil.quatRotate(viewRot, relMove, relMove);
                break;
            default:
                QuaternionUtil.setEuler(viewRot, TeraMath.DEG_TO_RAD * characterComponent.yaw, TeraMath.DEG_TO_RAD * characterComponent.pitch, 0);
                QuaternionUtil.quatRotate(viewRot, relMove, relMove);
                relMove.y += relativeMovement.y;
                break;
        }
        entity.send(new CharacterMoveInputEvent(inputSequenceNumber++, lookPitch, lookYaw, relMove, run, jump));
        jump = false;
    }

    private void updateCamera(CharacterComponent characterComponent, CharacterMovementComponent characterMovementComponent, CharacterComponent characterComp, LocationComponent location) {
        // TODO: Remove, use component camera, breaks spawn camera anyway
        Quat4f lookRotation = new Quat4f();
        QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * characterComponent.yaw, TeraMath.DEG_TO_RAD * characterComponent.pitch, 0);
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
        lookPitch = TeraMath.clamp(character.pitch - event.getValue(), -89, 89);
        event.consume();
    }

    @ReceiveEvent(components = {CharacterComponent.class, CharacterMovementComponent.class})
    public void onJump(JumpButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            jump = true;
            if (time.getGameTimeInMs() - lastTimeSpacePressed < 200) {
                //characterMovement.isGhosting = !characterMovement.isGhosting;
            }
            lastTimeSpacePressed = time.getGameTimeInMs();
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
public void onRun(RunButton event, EntityRef entity) {
    run = event.isDown();
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
                if (block.isTargetable()) {
                    aabb = block.getBounds(blockPos);
                }
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
        Vector3d cameraPosition = new Vector3d();
        cameraPosition.add(new Vector3d(position), new Vector3d(0, characterComponent.eyeOffset, 0));

        playerCamera.getPosition().set(cameraPosition);
        Vector3f viewDir = Direction.FORWARD.getVector3f();
        QuaternionUtil.quatRotate(rotation, viewDir, playerCamera.getViewingDirection());

        float stepDelta = charMovementComp.footstepDelta - lastStepDelta;
        if (stepDelta < 0) stepDelta += charMovementComp.distanceBetweenFootsteps;
        bobFactor += stepDelta;
        lastStepDelta = charMovementComp.footstepDelta;

        if (config.getRendering().isCameraBobbing()) {
            playerCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.5f));
            playerCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f, 0.025f, 3f));
        } else {
            playerCamera.setBobbingRotationOffsetFactor(0.0f);
            playerCamera.setBobbingVerticalOffsetFactor(0.0f);
        }

        if (charMovementComp.mode == MovementMode.GHOSTING) {
            playerCamera.extendFov(24);
        } else {
            playerCamera.resetFov();
        }
    }


    @ReceiveEvent(components = {CharacterComponent.class})
    public void onFrobRequest(FrobButton event, EntityRef entity) {
        if (event.getState() != ButtonState.DOWN) {
            return;
        }

        entity.send(new FrobRequest());
        event.consume();
    }

    private float calcBobbingOffset(float phaseOffset, float amplitude, float frequency) {
        return (float) java.lang.Math.sin(bobFactor * frequency + phaseOffset) * amplitude;
    }

   @Override
    public void renderOpaque() {

    }

    @Override
    public void renderTransparent() {

    }

    @Override
    public void renderFirstPerson() {
    }

}
