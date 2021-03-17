// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.players;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.DoNotPersist;

@DoNotPersist
public class LocalPlayerControlComponent implements Component {
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
}
