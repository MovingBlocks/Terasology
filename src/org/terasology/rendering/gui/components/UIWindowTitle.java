package org.terasology.rendering.gui.components;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Created by IntelliJ IDEA.
 * User: kireev
 * Date: 29.03.12
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */
public class UIWindowTitle extends UIDisplayContainer{
    private UIGraphicsElement _leftBackground;
    private UIGraphicsElement _centerBackground;
    private UIGraphicsElement _rightBackground;
    private UIText _text;

    public UIWindowTitle(Vector2f size, String title){
        setSize(size);

        _text = new UIText(getPosition());
        setTitle(title);
        _text.setColor(Color.orange);
        _text.setVisible(true);

        _leftBackground   = new UIGraphicsElement("gui_menu");
        _leftBackground.setSize(new Vector2f(7f, 19f));
        _leftBackground.getTextureSize().set(new Vector2f(7f / 512f, 19f / 512f));
        _leftBackground.getTextureOrigin().set(new Vector2f(111f / 512f, 155f / 512f));
        _leftBackground.setVisible(true);

        _centerBackground = new UIGraphicsElement("gui_menu");
        _centerBackground.setSize(new Vector2f(getSize().x-19f, 19f));
        _centerBackground.getTextureSize().set(new Vector2f(51f / 512f, 19f / 512f));
        _centerBackground.getTextureOrigin().set(new Vector2f(118f / 512f, 155f / 512f));
        _centerBackground.getPosition().x +=7f;
        _centerBackground.setVisible(true);

        _rightBackground  = new UIGraphicsElement("gui_menu");
        _rightBackground.setSize(new Vector2f(7f, 19f));
        _rightBackground.getTextureSize().set(new Vector2f(5f / 512f, 11f / 512f));
        _rightBackground.getTextureOrigin().set(new Vector2f(167f / 512f, 155f / 512f));
      //  _rightBackground.setVisible(true);
        addDisplayElement(_leftBackground);
        addDisplayElement(_centerBackground);

    }


    public void setTitle(String title){
        _text.setText(title);
    }

}
