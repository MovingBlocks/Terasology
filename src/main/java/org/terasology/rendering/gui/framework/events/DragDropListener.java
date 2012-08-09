package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

/**
 * Drag/Drop listener.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public interface DragDropListener {
    public void drag(UIDisplayElement element);
    
    public void drop(UIDisplayElement element);
}
