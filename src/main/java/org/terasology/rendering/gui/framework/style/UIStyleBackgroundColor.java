package org.terasology.rendering.gui.framework.style;

import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBackgroundColor extends UIGraphicsElement implements UIStyle {

    public UIStyleBackgroundColor(int r, int g, int b, float a) {
        super(r, g, b, a);
    }
    
    @Override
    public void layout() {
        if (getParent() != null) {
            setSize(getParent().getSize());
        }
        
        super.layout();
    }
}
