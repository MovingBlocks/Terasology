package org.terasology.rendering.gui.components;

import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.newdawn.slick.Color;
import javax.vecmath.Vector2f;

public class UIMessageBox extends UIDialogBox{
    private UIText   _text;
    private UIButton _buttonOk;

    private Vector2f _minSize = new Vector2f(384f, 128f);

    public UIMessageBox(String title, String text){
        super(title, new Vector2f());
        setModal(true);

        float width = 0f;
        float heigh = 0f;

        _buttonOk = new UIButton(new Vector2f(128f, 32f));
        _buttonOk.getLabel().setText("Ok");
        _buttonOk.setVisible(true);

        _text = new UIText(text);
        _text.setVisible(true);
        _text.setColor(Color.black);

        width = _text.getTextWidth()+15f>_minSize.x?_text.getTextWidth()+15f:_minSize.x;
        heigh = _text.getTextHeight()+75f>_minSize.y?_text.getTextHeight()+75f:_minSize.y;
        setSize(new Vector2f(width, heigh));

        _text.setPosition(new Vector2f(getSize().x / 2 - _text.getTextWidth()/2, getSize().y / 2 - _text.getTextHeight()/2));
        _buttonOk.setPosition(new Vector2f(getSize().x / 2 - _buttonOk.getSize().x/2, getSize().y - _buttonOk.getSize().y - 10f));

        _buttonOk.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
              close();
            }
        });

        resize();
        windowStyleSetup();

        addDisplayElement(_text);
        addDisplayElement(_buttonOk,"buttonOk");
    }

    public void close(){
        super.close(false);
        GUIManager.getInstance().setLastFocused();
        GUIManager.getInstance().removeWindow("messageBox");
    }
}
