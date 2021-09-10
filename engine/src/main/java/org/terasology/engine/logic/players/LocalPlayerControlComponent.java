// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.players;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;

public class LocalPlayerControlComponent implements Component<LocalPlayerControlComponent> {
    public double lookPitchDelta;
    public double lookYawDelta;
    public float lookPitch;
    public float lookYaw;
    public Vector3f relativeMovement = new Vector3f();
    public boolean runPerDefault = true;
    public boolean run = runPerDefault;
    public boolean crouchPerDefault;
    public boolean crouch;
    public boolean jump;
    public int inputSequenceNumber = 1;

    @Override
    public void copyFrom(LocalPlayerControlComponent other) {
        this.lookPitchDelta = other.lookPitchDelta;
        this.lookYawDelta = other.lookYawDelta;
        this.lookPitch = other.lookPitch;
        this.lookYaw = other.lookYaw;
        this.relativeMovement.set(other.relativeMovement);
        this.runPerDefault = other.runPerDefault;
        this.run = other.run;
        this.crouchPerDefault = other.crouchPerDefault;
        this.crouch = other.crouch;
        this.jump = other.jump;
        this.inputSequenceNumber = other.inputSequenceNumber;
    }
}
