package org.terasology.rendering.gui.layout;

import org.terasology.rendering.gui.framework.UIDisplayContainer;

/**
 * Interface for layout classes which can be used in the UIComposite container.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public interface UICompositeLayout {
    /**
     * Arranges the child elements within a UIComposite class.
     * @param elements The child elements to arrange.
     * @param container The container of the child elements.
     */
    public void layout(UIDisplayContainer container);
    
    /**
     * Render something in the layout class itself.
     */
    public void render();
}
