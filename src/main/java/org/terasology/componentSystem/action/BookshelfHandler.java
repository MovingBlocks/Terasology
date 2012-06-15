package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;
import org.terasology.components.BookshelfComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.inventory.ReceiveItemEvent;

/**
 *
 */
@RegisterComponentSystem()
public class BookshelfHandler implements EventHandlerSystem {
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = BookshelfComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onActivate(ReceiveItemEvent event, EntityRef entity) {
        if (!event.getItem().hasComponent(BookComponent.class)) event.cancel();
    }
}
