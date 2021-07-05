// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

@ServerEvent
public class CharacterMoveInputEvent extends NetworkEvent {
    private long delta;
    private float pitch;
    private float yaw;
    private boolean running;
    private boolean crouching;
    private boolean jumping;
    private Vector3f movementDirection = new Vector3f();
    private int sequenceNumber;
    private boolean firstRun = true;

    protected CharacterMoveInputEvent() {
    }

    @Deprecated
    public CharacterMoveInputEvent(int sequence, float pitch, float yaw, Vector3fc movementDirection,
                                   boolean running, boolean jumping, long delta) {
        this(sequence, pitch, yaw, movementDirection, running, false, jumping, delta);
    }

    public CharacterMoveInputEvent(int sequence, float pitch, float yaw, Vector3fc movementDirection,
                                   boolean running, boolean crouching, boolean jumping, long delta) {
        this.delta = delta;
        this.pitch = pitch;
        this.yaw = yaw;
        this.running = running;
        this.crouching = crouching;
        this.jumping = jumping;
        this.movementDirection.set(movementDirection);
        this.sequenceNumber = sequence;
    }

    public CharacterMoveInputEvent(CharacterMoveInputEvent repeatInput, int withLength) {
        this.delta = withLength;
        this.pitch = repeatInput.pitch;
        this.yaw = repeatInput.yaw;
        this.running = repeatInput.running;
        this.crouching = repeatInput.crouching;
        this.jumping = repeatInput.jumping;
        this.movementDirection.set(repeatInput.movementDirection);
        this.sequenceNumber = repeatInput.sequenceNumber;
    }

    public long getDeltaMs() {
        return delta;
    }

    public float getDelta() {
        return delta / 1000f;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Vector3fc getMovementDirection() {
        return movementDirection;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public boolean isJumping() {
        return jumping;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void runComplete() {
        firstRun = false;
    }
}
