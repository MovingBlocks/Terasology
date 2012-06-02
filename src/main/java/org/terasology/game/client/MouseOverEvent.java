package org.terasology.game.client;


import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * This event is sent when the camera starts pointing at a target
 */
public class MouseOverEvent extends AbstractEvent {
    private EntityRef target;

    public MouseOverEvent(EntityRef target) {
        this.target = target;
    }

    /**
     * The target the camera is now over
     *
     * @return The target the camera is now over
     */
    public EntityRef getTarget() {
        return target;
    }
}
