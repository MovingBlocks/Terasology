package org.terasology.game.client;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * Event when the camera ceases to be over an entity
 */
public class MouseOutEvent extends AbstractEvent {
    private EntityRef target;

    public MouseOutEvent(EntityRef target) {
        this.target = target;
    }

    /**
     * The target the camera used to be over. May be a NullEntity if the target was destroyed
     *
     * @return The target the camera used to be over
     */
    public EntityRef getTarget() {
        return target;
    }
}
