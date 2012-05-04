package org.terasology.rendering.gui.components;

import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;   //Container or Window... hmmm??
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import javax.vecmath.Vector2f;

public class UIOpenBook extends UIDisplayContainer {
    private final UIGraphicsElement _background;

    public UIOpenBook() {
        setSize(new Vector2f(256.0f * 2.5f, 256.0f * 2.5f));

        _background = new UIGraphicsElement("openbook");
        _background.setSize(getSize());
        _background.getTextureSize().set(new Vector2f(256.0f / 256.0f, 256.0f / 256.0f));
        _background.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        _background.setVisible(true);

        addDisplayElement(_background);

    }
}

