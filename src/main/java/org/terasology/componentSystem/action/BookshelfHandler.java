package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;
import org.terasology.components.BookshelfComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.inventory.ReceiveItemEvent;

/**
 *
 */
public class BookshelfHandler implements EventHandlerSystem {
    @Override
    public void initialise() {
    }

    @ReceiveEvent(components = BookshelfComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onActivate(ReceiveItemEvent event, EntityRef entity) {
        if (!event.getItem().hasComponent(BookComponent.class)) event.cancel();
    }
}
