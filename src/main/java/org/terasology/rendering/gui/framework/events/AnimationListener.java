package org.terasology.rendering.gui.framework.events;

import org.terasology.rendering.gui.framework.UIDisplayElement;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public interface AnimationListener {
    public void start(UIDisplayElement element);
    
    public void stop(UIDisplayElement element);
    
    public void repeat(UIDisplayElement element);
}
