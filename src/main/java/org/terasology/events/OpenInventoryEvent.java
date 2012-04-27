package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class OpenInventoryEvent extends AbstractEvent {
    private EntityRef container;

    public OpenInventoryEvent(EntityRef container) {
        this.container = container;
    }

    public EntityRef getContainer() {
        return container;
    }
}
