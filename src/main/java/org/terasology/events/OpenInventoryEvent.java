package org.terasology.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class OpenInventoryEvent implements Event {
    private EntityRef container;

    public OpenInventoryEvent(EntityRef container) {
        this.container = container;
    }

    public EntityRef getContainer() {
        return container;
    }
}
