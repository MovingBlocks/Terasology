package org.terasology.events.input;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3f;


public abstract class InputEvent extends AbstractEvent {
    private boolean consumed;
    private float delta;

    private EntityRef target = EntityRef.NULL;
    private Vector3f hitPosition;
    private Vector3f hitNormal;

    public InputEvent(float delta) {
        this.delta = delta;
    }

    public void setTarget(EntityRef target, Vector3f hitPosition, Vector3f hitNormal) {
        this.target = target;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getHitPosition() {
        return hitPosition;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public float getDelta() {
        return delta;
    }

    public void consume() {
        consumed = true;
        cancel();
    }

    public boolean isConsumed() {
        return consumed;
    }

    protected void reset(float delta) {
        consumed = false;
        cancelled = false;
        this.delta = delta;
        this.target = EntityRef.NULL;
    }
}
