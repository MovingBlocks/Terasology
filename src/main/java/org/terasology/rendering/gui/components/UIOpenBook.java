package org.terasology.rendering.gui.components;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.terasology.components.BookComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 *
 */
public class UIOpenBook extends UIDisplayContainer {

    private final UIGraphicsElement background;

    public UIOpenBook() {
        background = new UIGraphicsElement("openbook");
        background.setPosition(new Vector2f(-250, -200));
        background.setSize(new Vector2f(500, 300));
        addDisplayElement(background);
        background.setVisible(true);
        update();
    }
}

