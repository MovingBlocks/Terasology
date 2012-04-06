package org.terasology.componentSystem.action;

import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.components.actions.ExplosionActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.OpenInventoryEvent;
import org.terasology.logic.LocalPlayer;

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
