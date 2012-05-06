package org.terasology.rendering.gui.components;

import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class UIOpenBook extends UIDisplayContainer {

    private final UIGraphicsElement background;

    public UIOpenBook() {
        background = new UIGraphicsElement("openbook");
        background.setSize(new Vector2f(500, 300));
        //background.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        addDisplayElement(background);
        background.setVisible(true);

        update();
    }
}

