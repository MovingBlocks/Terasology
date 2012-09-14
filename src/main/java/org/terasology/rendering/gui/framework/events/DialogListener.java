package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

public interface DialogListener {
    
    public void close(UIDisplayElement dialog, Object returnValue);
    
}
