package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.menus.UIOpenBookScreen;


/**
 * Reading the Book calls the UI + Contents.
 * @author bi0hax
 *
 */
@RegisterComponentSystem
public class ReadBookAction implements EventHandlerSystem {
    public void initialise() {}

    public EntityRef entity;

    @ReceiveEvent(components = {BookComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        GUIManager.getInstance().addWindow(new UIOpenBookScreen(), "openbook");

    }
}
