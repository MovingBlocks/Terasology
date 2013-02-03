package org.terasology.physics;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.network.BroadcastEvent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@BroadcastEvent
public class PhysicsResynchEvent extends AbstractEvent {
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f();
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();

    protected PhysicsResynchEvent() {
    }

    public PhysicsResynchEvent(Vector3f position, Quat4f rotation, Vector3f velocity, Vector3f angularVelocity) {
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.angularVelocity.set(angularVelocity);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quat4f getRotation() {
        return rotation;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
}
