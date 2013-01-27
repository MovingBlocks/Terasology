package org.terasology.logic.characters;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class CharacterState {
    private long time;
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f(0,0,0,1);
    private MovementMode mode = MovementMode.WALKING;
    private boolean grounded = false;
    private Vector3f velocity = new Vector3f();

    public CharacterState(CharacterState previous) {
        this.time = previous.time;
        this.position.set(previous.position);
        this.rotation.set(previous.rotation);
        this.mode = previous.mode;
        this.grounded = previous.grounded;
        this.velocity.set(previous.velocity);
    }

    public CharacterState(long time, Vector3f position, Quat4f rotation, Vector3f velocity, MovementMode mode, boolean grounded) {
        this.time = time;
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.mode = mode;
        this.grounded = grounded;
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

}
