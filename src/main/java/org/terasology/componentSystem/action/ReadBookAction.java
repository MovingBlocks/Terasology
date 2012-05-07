package org.terasology.componentSystem.action;

import org.terasology.components.BookComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.item.UseItemEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.components.UIOpenBook;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.menus.UIOpenBookScreen;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reading the Book calls the UI + Contents.
 * @author bi0hax
 *
 */
public class ReadBookAction implements EventHandlerSystem {
    private Logger logger = Logger.getLogger(getClass().getName());
    public void initialise() {
    }
    public EntityRef book;
    @ReceiveEvent(components = {BookComponent.class})
    public void onActivate(ActivateEvent event, EntityRef book) {
        GUIManager.getInstance().addWindow(new UIOpenBookScreen(), "openbook");
    }
}
