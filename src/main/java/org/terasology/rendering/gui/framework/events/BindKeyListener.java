package org.terasology.rendering.gui.framework.events;

import org.terasology.input.BindButtonEvent;
import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface BindKeyListener {
    void key(UIDisplayElement element, BindButtonEvent event);
}
