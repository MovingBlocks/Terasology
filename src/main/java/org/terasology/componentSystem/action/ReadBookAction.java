package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.menus.UIOpenBookScreen;


/**
 * Reading the Book calls the UI + Contents.
 * @author bi0hax
 *
 */
public class ReadBookAction implements EventHandlerSystem {

    private UIDisplayWindow bookScreen;

    public void initialise() {
        bookScreen = GUIManager.getInstance().addWindow(new UIOpenBookScreen(), "engine:bookScreen");
    }

    public EntityRef entity;


    @ReceiveEvent(components = {BookComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        GUIManager.getInstance().setFocusedWindow(bookScreen);

    }
}
