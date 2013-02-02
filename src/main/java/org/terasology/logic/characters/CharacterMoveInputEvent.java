package org.terasology.logic.characters;

import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@ServerEvent
public class CharacterMoveInputEvent extends NetworkEvent {
    private long delta;
    private float pitch;
    private float yaw;
    private boolean running;
    private boolean jumpRequested;
    private Vector3f movementDirection = new Vector3f();
    private int sequenceNumber = 0;

    protected CharacterMoveInputEvent() {
    }

    public CharacterMoveInputEvent(int sequence, float pitch, float yaw, Vector3f movementDirection, boolean running, boolean jumpRequested) {
        Timer timer = CoreRegistry.get(Timer.class);
        this.delta = timer.getDeltaInMs();
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
}
