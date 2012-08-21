package org.terasology.rendering.gui.framework.events;

import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface KeyListener {
    void key(UIDisplayElement element, KeyEvent event);
}
