package org.terasology.logic.characters;

import com.bulletphysics.linearmath.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.NetworkEvent;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@BroadcastEvent
public class CharacterStateEvent extends NetworkEvent {
    private long time;
    private int sequenceNumber;
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f(0,0,0,1);
    private MovementMode mode = MovementMode.WALKING;
    private boolean grounded = false;
    private Vector3f velocity = new Vector3f();

    protected CharacterStateEvent() {};

    public CharacterStateEvent(CharacterStateEvent previous) {
        this.time = previous.time;
        this.position.set(previous.position);
        this.rotation.set(previous.rotation);
        this.mode = previous.mode;
        this.grounded = previous.grounded;
        this.velocity.set(previous.velocity);
        this.sequenceNumber = previous.sequenceNumber + 1;
    }

    public CharacterStateEvent(long time, int sequenceNumber, Vector3f position, Quat4f rotation, Vector3f velocity, MovementMode mode, boolean grounded) {
        this.time = time;
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.mode = mode;
        this.grounded = grounded;
        this.sequenceNumber = sequenceNumber;
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

    public static void setToState(EntityRef entity, CharacterStateEvent state) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(state.getPosition());
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);
        CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);
        movementComp.mode = state.getMode();
        movementComp.setVelocity(state.getVelocity());
        movementComp.grounded = state.isGrounded();
        entity.saveComponent(movementComp);
        movementComp.collider.setInterpolationWorldTransform(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), state.getPosition(), 1.0f)));
    }

    public static void setToInterpolateState(EntityRef entity, CharacterStateEvent a, CharacterStateEvent b, long time) {
        float t = (float)(time - a.getTime()) / (b.getTime() - a.getTime());
        Vector3f newPos = new Vector3f();
        newPos.interpolate(a.getPosition(), b.getPosition(), t);
        Quat4f newRot = new Quat4f();
        newRot.interpolate(a.getRotation(), b.getRotation(), t);
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(newPos);
        location.setWorldRotation(newRot);
        entity.saveComponent(location);

        CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
        movementComponent.mode = a.getMode();
        movementComponent.setVelocity(a.getVelocity());
        movementComponent.grounded = a.isGrounded();
        entity.saveComponent(movementComponent);
        movementComponent.collider.setInterpolationWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), newPos, 1.0f)));
    }

    public static void setToExtrapolateState(EntityRef entity, CharacterStateEvent state, long time) {
        float t = (time - state.getTime()) * 0.0001f;
        Vector3f newPos = new Vector3f(state.getVelocity());
        newPos.scale(t);
        newPos.add(state.getPosition());
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(newPos);
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);

        CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
        movementComponent.mode = state.getMode();
        movementComponent.setVelocity(state.getVelocity());
        movementComponent.grounded = state.isGrounded();
        entity.saveComponent(movementComponent);
        movementComponent.collider.setInterpolationWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), newPos, 1.0f)));
    }

}
