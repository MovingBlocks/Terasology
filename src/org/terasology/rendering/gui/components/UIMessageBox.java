package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;


public class UIMessageBox extends UIDialogBox{
    private UIText   _text;
    private UIButton _buttonOk;

    public UIMessageBox(String title, String text){
        super(title, new Vector2f());
        _buttonOk = new UIButton(new Vector2f(128f, 32f));
        _buttonOk.getLabel().setText("Ok");
        _buttonOk.setVisible(true);

        _text = new UIText(text);
        _text.setVisible(true);

        setSize(new Vector2f(_text.getTextWidth() + 15f, _text.getTextHeight() + 75f));

        _text.setPosition(new Vector2f(getSize().x / 2 - _text.getTextWidth()/2, getSize().y / 2 - _text.getTextHeight()/2));
        _buttonOk.setPosition(new Vector2f(getSize().x / 2 - _buttonOk.getSize().x/2, getSize().y - _buttonOk.getSize().y - 10f));

        addDisplayElement(_text);
        addDisplayElement(_buttonOk,"buttonOk");
    }
}
