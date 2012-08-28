package org.terasology.rendering.gui.framework.style;

import org.terasology.rendering.gui.widgets.UIImage;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBackgroundColor extends UIImage implements UIStyle {

    public UIStyleBackgroundColor(int r, int g, int b, float a) {
        super(r, g, b, a);
        setSize("100%", "100%");
    }
    
    public UIStyleBackgroundColor(String color, float a) {
        super(color, a);
        setSize("100%", "100%");
    }
}
