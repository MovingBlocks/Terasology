package org.terasology.rendering.gui.framework.style;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.widgets.UIImage;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBackgroundColor extends UIImage implements UIStyle {

    public UIStyleBackgroundColor(Color color) {
        super(color);
        setSize("100%", "100%");
    }
    
    public UIStyleBackgroundColor(String color) {
        super(color);
        setSize("100%", "100%");
    }
}
