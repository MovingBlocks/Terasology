// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.players;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.input.InputSystem;
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
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.registry.In;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;

import java.util.List;

public class LocalPlayerControlSystem extends BaseComponentSystem {

    @In
    private InputSystem inputSystem;
    @In
    private BindsManager bindsManager;

    private boolean isAutoMove;

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onLocalPlayerInit(LocalPlayerInitializedEvent event, EntityRef entity) {
        entity.addComponent(new LocalPlayerControlComponent());
    }

    @ReceiveEvent(components = LocalPlayerControlComponent.class)
    public void onMouseMove(MouseAxisEvent event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        MouseAxisEvent.MouseAxis axis = event.getMouseAxis();
        if (axis == MouseAxisEvent.MouseAxis.X) {
            control.lookYawDelta = event.getValue();
        } else if (axis == MouseAxisEvent.MouseAxis.Y) {
            control.lookPitchDelta = event.getValue();
        }
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateRotationYaw(RotationYawAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.lookYawDelta = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateRotationPitch(RotationPitchAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.lookPitchDelta = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class, CharacterMovementComponent.class})
    public void onJump(JumpButton event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        if (event.getState() == ButtonState.DOWN) {
            control.jump = true;
            event.consume();
        } else {
            control.jump = false;
        }
    }

    @ReceiveEvent(components = LocalPlayerControlComponent.class)
    public void setRotation(SetDirectionEvent event, EntityRef entity, LocalPlayerControlComponent control) {
        control.lookPitch = event.getPitch();
        control.lookYaw = event.getYaw();
    }


    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateForwardsMovement(ForwardsMovementAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.relativeMovement.z = (float) event.getValue();
        if (control.relativeMovement.z == 0f && isAutoMove) {
            stopAutoMove();
        }
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateStrafeMovement(StrafeMovementAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.relativeMovement.x =  (float)event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateVerticalMovement(VerticalMovementAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.relativeMovement.y = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateForwardsMovement(ForwardsRealMovementAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.relativeMovement.z =  (float)event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateStrafeMovement(StrafeRealMovementAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.relativeMovement.x = (float) event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class})
    public void updateVerticalMovement(VerticalRealMovementAxis event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        control.relativeMovement.y =  (float)event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onToggleSpeedTemporarily(ToggleSpeedTemporarilyButton event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        boolean toggle = event.isDown();
        control.run = control.runPerDefault ^ toggle;
        event.consume();
    }

    // Crouches if button is pressed. Stands if button is released.
    @ReceiveEvent(components = {LocalPlayerControlComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onCrouchTemporarily(CrouchButton event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        boolean toggle = event.isDown();
        control.crouch = control.crouchPerDefault ^ toggle;
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerControlComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onCrouchMode(CrouchModeButton event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        if (event.isDown()) {
            control.crouchPerDefault = !control.crouchPerDefault;
            control.crouch = !control.crouch;
        }
        event.consume();
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

    @ReceiveEvent(components = {LocalPlayerControlComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
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

    @ReceiveEvent(components = {LocalPlayerControlComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onToggleSpeedPermanently(ToggleSpeedPermanentlyButton event, EntityRef entity) {
        LocalPlayerControlComponent control = entity.getComponent(LocalPlayerControlComponent.class);
        if (event.isDown()) {
            control.runPerDefault = !control.runPerDefault;
            control.run = !control.run;
        }
        event.consume();
    }
}
