package org.terasology.rendering.gui.menus;

import org.terasology.entitySystem.EntityRef;        //for Contents in a future?
import org.terasology.rendering.gui.components.UIOpenBook;
import org.terasology.rendering.gui.framework.UIDisplayRenderer; //Not sure if it should be a Renderer instead of Window
import org.terasology.rendering.gui.framework.UIDisplayWindow;

public class UIOpenBookScreen extends UIDisplayWindow{

    private final UIOpenBook _openBook;

    public UIOpenBookScreen() {

        _openBook = new UIOpenBook();
        _openBook.setVisible(true);
        addDisplayElement(_openBook);
        update();
    }

    @Override
    public void update() {
        super.update();
        _openBook.center();
        _openBook.setVisible(true);
    }
}
