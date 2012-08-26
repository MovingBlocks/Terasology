package org.terasology.rendering.gui.framework.style;

import javax.vecmath.Vector2f;

import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.widgets.UIImage;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStyleBackgroundImage extends UIImage implements UIStyle {

    public UIStyleBackgroundImage(Texture texture) {
        super(texture);
        setSize("100%", "100%");
    }
    
    @Override
    public void setPosition(Vector2f position) {

    }    
}
