package org.terasology.rendering.gui.menus;

import org.terasology.rendering.gui.components.UIMinion;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 11/05/12
 * Time: 19:03
 * To change this template use File | Settings | File Templates.
 */
public class UIMinionMenu extends UIDisplayWindow {

    private final UIMinion _minionBehaviour;

    public UIMinionMenu() {
        _minionBehaviour = new UIMinion();
        _minionBehaviour.setVisible(true);
        addDisplayElement(_minionBehaviour);
        update();
    }

    /*@Override
    public void update() {
        super.update();
        _minionBehaviour.isVisible();
        _minionBehaviour.center();
    }*/
}
