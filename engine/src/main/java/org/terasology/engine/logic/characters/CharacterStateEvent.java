// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters;

import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

@BroadcastEvent
public class CharacterStateEvent extends NetworkEvent {
    private long time;
    private int sequenceNumber;
    private Vector3f position = new Vector3f();
    private final Quat4f rotation = new Quat4f(0, 0, 0, 1);
    private MovementMode mode = MovementMode.WALKING;
    private boolean grounded;
    private final Vector3f velocity = new Vector3f();
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
            Vector3f position,
            Quat4f rotation,
            Vector3f velocity,
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

    public void setTime(long time) {
        this.time = time;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Quat4f getRotation() {
        return rotation;
    }

    public MovementMode getMode() {
        return mode;
    }

    public void setMode(MovementMode mode) {
        this.mode = mode;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Vector3i getClimbDirection() {
        return climbDirection;
    }

    public void setClimbDirection(Vector3i climbDirection) {
        this.climbDirection = climbDirection;
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
}
