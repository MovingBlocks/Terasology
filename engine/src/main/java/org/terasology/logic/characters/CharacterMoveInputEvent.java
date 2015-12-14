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

package org.terasology.logic.characters;

import org.terasology.math.geom.Vector3f;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 */
@ServerEvent
public class CharacterMoveInputEvent extends NetworkEvent {
    private long delta;
    private float pitch;
    private float yaw;
    private boolean running;
    private boolean jumpRequested;
    private Vector3f movementDirection = new Vector3f();
    private int sequenceNumber;
    private boolean firstRun = true;

    protected CharacterMoveInputEvent() {
    }

    public CharacterMoveInputEvent(int sequence, float pitch, float yaw, Vector3f movementDirection, boolean running, boolean jumpRequested, long delta) {
        this.delta = delta;
        this.pitch = pitch;
        this.yaw = yaw;
        this.running = running;
        this.jumpRequested = jumpRequested;
        this.movementDirection.set(movementDirection);
        this.sequenceNumber = sequence;
    }

    public CharacterMoveInputEvent(CharacterMoveInputEvent repeatInput, int withLength) {
        this.delta = withLength;
        this.pitch = repeatInput.pitch;
        this.yaw = repeatInput.yaw;
        this.running = repeatInput.running;
        this.jumpRequested = false;
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

    public Vector3f getMovementDirection() {
        return new Vector3f(movementDirection);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isJumpRequested() {
        return jumpRequested;
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
