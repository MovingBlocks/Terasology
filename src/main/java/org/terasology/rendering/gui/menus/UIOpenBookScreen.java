package org.terasology.rendering.gui.menus;

import org.terasology.rendering.gui.components.UIOpenBook;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UIOpenBookScreen extends UIDisplayWindow {

    private final UIOpenBook _openbook;

    public UIOpenBookScreen() {
        maximaze();
        _openbook = new UIOpenBook();
        _openbook.setVisible(true);
        addDisplayElement(_openbook);
        update();
        setModal(true);
    }

    @Override
    public void update() {
        super.update();
        _openbook.isVisible();
        _openbook.center();
    }
}