package org.terasology.physics;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Pencilcheck
 * Date: 12/23/12
 * Time: 12:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class MovedEvent extends AbstractEvent {
    private Vector3f delta;
    private Vector3f final_position;

    public MovedEvent(Vector3f delta, Vector3f final_position) {
        this.delta = delta;
        this.final_position = final_position;
    }

    public Vector3f getDelta() {
        return delta;
    }

    public Vector3f getPosition() {
        return final_position;
    }
}
