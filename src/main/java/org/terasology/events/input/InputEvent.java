package org.terasology.events.input;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;


public abstract class InputEvent extends AbstractEvent {
    private EntityRef target;
    private boolean consumed;
    private float delta;

    public InputEvent(float delta, EntityRef target) {
        this.target = target;
        this.delta = delta;
    }

    public EntityRef getTarget() {
        return target;
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

    protected void reset(float delta, EntityRef target) {
        consumed = false;
        cancelled = false;
        this.delta = delta;
        this.target = target;
    }
}
