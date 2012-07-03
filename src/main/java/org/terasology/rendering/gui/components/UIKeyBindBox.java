package org.terasology.rendering.gui.components;

import org.newdawn.slick.Color;
import org.terasology.logic.manager.GUIManager;

import javax.vecmath.Vector2f;

public class UIKeyBindBox extends UIDialogBox {
    private UIText _text;

    private Vector2f _minSize = new Vector2f(384f, 128f);

    public UIKeyBindBox(String title, String text) {
        super(title, new Vector2f());
        setModal(true);

        float width = 0f;
        float heigh = 0f;

        _text = new UIText(text);
        _text.setVisible(true);
        _text.setColor(Color.black);

        width = _text.getTextWidth() + 15f > _minSize.x ? _text.getTextWidth() + 15f : _minSize.x;
        heigh = _text.getTextHeight() + 75f > _minSize.y ? _text.getTextHeight() + 75f : _minSize.y;
        setSize(new Vector2f(width, heigh));

        _text.setPosition(new Vector2f(getSize().x / 2 - _text.getTextWidth() / 2, getSize().y / 2 - _text.getTextHeight() / 2));

        resize();
        windowStyleSetup();

        addDisplayElement(_text);
    }

    public void close() {
        super.close(false);
        GUIManager.getInstance().setLastFocused();
        GUIManager.getInstance().removeWindow("messageBox");
    }
}
