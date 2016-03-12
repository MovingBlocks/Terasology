/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.logic.characters;

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.NetworkEvent;

@BroadcastEvent
public class CharacterStateEvent extends NetworkEvent {
    private long time;
    private int sequenceNumber;
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f(0, 0, 0, 1);
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

    public Vector3f getPosition() {
        return position;
    }

    public Quat4f getRotation() {
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
