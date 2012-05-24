package org.terasology.events;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class VerticalCollisionEvent extends AbstractEvent {
    private Vector3f velocity;
    private Vector3f location;

    public VerticalCollisionEvent(Vector3f velocity, Vector3f location) {
        this.velocity = new Vector3f(velocity);
        this.location = new Vector3f(location);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getLocation() {
        return location;
    }
}
