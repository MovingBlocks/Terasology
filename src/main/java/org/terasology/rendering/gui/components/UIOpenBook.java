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
    private Logger logger = Logger.getLogger(getClass().getName());

    private final UIGraphicsElement background;

    public UIOpenBook() {
        background = new UIGraphicsElement("containerWindow");
        background.getTextureSize().set(new Vector2f(256f / 256f, 231f / 256f));
        background.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        addDisplayElement(background);

        background.setVisible(true);


        logger.log(Level.WARNING, "The Action calls the UIOpenBook");
        update();
    }
}

