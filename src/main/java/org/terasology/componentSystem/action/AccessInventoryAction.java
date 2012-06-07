package org.terasology.componentSystem.action;

import org.terasology.components.InventoryComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.OpenInventoryEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.menus.UIContainerScreen;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class AccessInventoryAction implements EventHandlerSystem {

    private UIContainerScreen containerScreen;

    public void initialise() {
        containerScreen = GUIManager.getInstance().addWindow(new UIContainerScreen(), "container");
    }

    @ReceiveEvent(components = {AccessInventoryActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        event.getInstigator().send(new OpenInventoryEvent(entity));
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onOpenContainer(OpenInventoryEvent event, EntityRef entity) {
        if (event.getContainer().hasComponent(InventoryComponent.class)) {
            containerScreen.openContainer(event.getContainer(), entity);
            GUIManager.getInstance().setFocusedWindow(containerScreen);
        }
    }

}
