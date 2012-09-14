package org.terasology.rendering.gui.layout;

import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * Interface for layout classes which can be used in the UIComposite container.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public interface Layout {
    /**
     * Arranges the child elements within a UIComposite.
     * @param elements The child elements to arrange.
     * @param container The container of the child elements.
     * @param fitSize True if the layout should set the size of the container.
     */
    public void layout(UIDisplayContainer container, boolean fitSize);
    
    /**
     * Render something in the layout class itself.
     */
    public void render();
}
