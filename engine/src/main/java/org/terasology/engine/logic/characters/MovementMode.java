// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters;

public enum MovementMode {
    WALKING(1f, 8f, true, true, true, 3f, false),
    CROUCHING(1f, 8f, true, true, true, 1.5f, false),
    PRONING(1f, 8f, true, true, true, 3f, false),
    CLIMBING(0f, 8f, true, true, true, 3f, false),
    SWIMMING(0.05f, 1.5f, true, false, true, 2f, true),
    DIVING(0f, 2f, true, false, true, 2f, true),
    GHOSTING(0f, 4f, false, false, false, 5f, true),
    FLYING(0f, 4f, true, false, false, 3f, true),
    NONE(0f, 0f, false, false, false, 0f, true);

    public float scaleGravity;
    public float scaleInertia;
    public boolean useCollision;
    public boolean canBeGrounded;
    public boolean respondToEnvironment;
    public float maxSpeed;
    public boolean applyInertiaToVertical;

    MovementMode(float scaleGravity, float scaleInertia, boolean useCollision, boolean canBeGrounded,
                         boolean respondToEnvironment, float maxSpeed, boolean applyInertiaToVertical) {
        this.scaleGravity = scaleGravity;
        this.scaleInertia = scaleInertia;
        this.useCollision = useCollision;
        this.canBeGrounded = canBeGrounded;
        this.respondToEnvironment = respondToEnvironment;
        this.maxSpeed = maxSpeed;
        this.applyInertiaToVertical = applyInertiaToVertical;
    }
}
