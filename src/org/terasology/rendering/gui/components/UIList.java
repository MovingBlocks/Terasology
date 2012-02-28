package org.terasology.rendering.gui.components;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kireev
 * Date: 21.02.12
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
public class UIList extends UIDisplayContainer {
    private final UIText _text;

    //Borders
    private final UIGraphicsElement _borderTop;
    private final UIGraphicsElement _borderRight;
    private final UIGraphicsElement _borderBottom;
    private final UIGraphicsElement _borderLeft;

    //List items
    private List<UIListItem> _items  = new ArrayList<UIListItem>();

    public UIList(Vector2f size) {
        setSize(size);
        _text = new UIText();
        _text.setVisible(true);
        _text.setColor(Color.red);
        _text.setPosition(new Vector2f((getPosition().x), (getPosition().y)));

        _text.setText("Future ListItem");

        _borderTop = new UIGraphicsElement("gui_menu");
        _borderTop.setVisible(true);
        _borderTop.getTextureSize().set(new Vector2f(256f/512f, 4f / 512f));

        _borderRight = new UIGraphicsElement("gui_menu");
        _borderRight.setVisible(true);
        _borderRight.setPosition(new Vector2f(getSize().x, getPosition().y));
        _borderRight.setRotateAngle(90f);
        _borderRight.getTextureSize().set(new Vector2f(256f/512f, 4f / 512f));

        _borderBottom = new UIGraphicsElement("gui_menu");
        _borderBottom.setVisible(true);
        _borderBottom.setPosition(new Vector2f(getSize().x, getSize().y));
        _borderBottom.setRotateAngle(180f);
        _borderBottom.getTextureSize().set(new Vector2f(256f/512f, 4f / 512f));

        _borderLeft = new UIGraphicsElement("gui_menu");
        _borderLeft.setVisible(true);
        _borderLeft.setRotateAngle(90f);
        _borderLeft.getTextureSize().set(new Vector2f(256f/512f, 4f / 512f));


        addDisplayElement(_text);
        addDisplayElement(_borderTop);
        addDisplayElement(_borderRight);
        addDisplayElement(_borderBottom);
        addDisplayElement(_borderLeft);

    }

    public void update(){
        updateBorders();
    }

    private void updateBorders(){
        _borderTop.setSize(new Vector2f(getSize().x, 4f));
        _borderTop.getTextureOrigin().set(0f, 150f / 512f);

        _borderRight.setSize(new Vector2f(getSize().y, 4f));
        _borderRight.getTextureOrigin().set(0f, 150f / 512f);

        _borderBottom.setSize(new Vector2f(getSize().x, 4f));
        _borderBottom.getTextureOrigin().set(0f, 150f / 512f);

        _borderLeft.setSize(new Vector2f(getSize().y, 4f));
        _borderLeft.getTextureOrigin().set(0f, 150f / 512f);
    }

    private void addItem(){

    }

    private void deleteItem(){

    }
}