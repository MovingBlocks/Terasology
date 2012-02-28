package org.terasology.componentSystem.action;

import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.OpenInventoryEvent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class AccessInventoryAction implements EventHandlerSystem {

    public void initialise() {
    }

    @ReceiveEvent(components = {AccessInventoryActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        event.getInstigator().send(new OpenInventoryEvent(entity));
    }

}
