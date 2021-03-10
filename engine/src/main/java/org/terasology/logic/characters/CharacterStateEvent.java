// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.network.NetworkEvent;

@BroadcastEvent
public class CharacterStateEvent extends NetworkEvent {
    private long time;
    private int sequenceNumber;
    private Vector3f position = new Vector3f();
    private Quaternionf rotation = new Quaternionf(0, 0, 0, 1);
    private MovementMode mode = MovementMode.WALKING;
    private boolean grounded;
    private Vector3f velocity = new Vector3f();
    private float yaw;
    private float pitch;
    private float footstepDelta;
    private Vector3i climbDirection;

    protected CharacterStateEvent() {
    }

    public CharacterStateEvent(CharacterStateEvent previous) {
        this.time = previous.time;
        this.position.set(previous.position);
        this.rotation.set(previous.rotation);
        this.mode = previous.mode;
        this.grounded = previous.grounded;
        this.velocity.set(previous.velocity);
        this.sequenceNumber = previous.sequenceNumber + 1;
        this.pitch = previous.pitch;
        this.yaw = previous.yaw;
        this.footstepDelta = previous.footstepDelta;
        this.climbDirection = previous.climbDirection;
    }

    public CharacterStateEvent(
            long time,
            int sequenceNumber,
            Vector3fc position,
            Quaternionfc rotation,
            Vector3fc velocity,
            float yaw,
            float pitch,
            MovementMode mode,
            boolean grounded) {
        this.time = time;
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.mode = mode;
        this.grounded = grounded;
        this.sequenceNumber = sequenceNumber;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public long getTime() {
        return time;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public MovementMode getMode() {
        return mode;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setMode(MovementMode mode) {
        this.mode = mode;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setClimbDirection(Vector3i climbDirection) {
        this.climbDirection = climbDirection;
    }

    public Vector3i getClimbDirection() {
        return climbDirection;
    }

    /**
     * Retrieve the pitch in degrees.
     *
     * @return
     */
    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Retrieve the yaw in degrees.
     *
     * @return
     */
    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getFootstepDelta() {
        return footstepDelta;
    }

    public void setFootstepDelta(float delta) {
        this.footstepDelta = delta;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
}
