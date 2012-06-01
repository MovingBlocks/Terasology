package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;  // is it needed for items with this class only being allowed in?
import org.terasology.components.BookshelfComponent;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.inventory.ReceiveItemEvent;

/**
 *
 */
@RegisterComponentSystem()
public class BookshelfHandler implements EventHandlerSystem{
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components=BookshelfComponent.class,priority=ReceiveEvent.PRIORITY_HIGH)
    public void onActivate(ReceiveItemEvent event, EntityRef entity) {
        if (!event.getItem().hasComponent(BookComponent.class)) event.cancel();
    }
}
