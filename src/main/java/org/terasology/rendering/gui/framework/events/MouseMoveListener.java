package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

/**
 * Notified on mouse events.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public interface MouseMoveListener {
    /**
     * Hover event. Will be called if the mouse is over the element and moves. (Still mouse won't cause any events.)
     *
     * @param element The element of the event.
     */
    public void hover(UIDisplayElement element);

    /**
     * Enter event. Will be called if the mouse enters the element.
     *
     * @param element The element of the event.
     */
    public void enter(UIDisplayElement element);

    /**
     * Leave event. Will be called if the mouse leaves the element.
     *
     * @param element The element of the event.
     */
    public void leave(UIDisplayElement element);

    /**
     * Move event. Will be called if the mouse moves anywhere in the UI. (Still mouse won't cause any events.)
     *
     * @param element The element of the event.
     */
    public void move(UIDisplayElement element);
}
