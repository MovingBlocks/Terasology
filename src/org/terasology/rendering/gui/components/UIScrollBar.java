package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

public class UIScrollBar extends UIDisplayElement {

    //Graphics
    private UIGraphicsElement _scroll;
    //private UIGraphicsElement _increaseBtn;
    //private UIGraphicsElement _decreaseBtn;

    //Options
    private int   _max;
    private int   _min;
    private float _step;
    private float _value;
    private ScrollType _scrollType = ScrollType.vertical;

    public static enum ScrollType {
        vertical, horizontal
    }

    public UIScrollBar(Vector2f size){
        setSize(size);

        _scroll = new UIGraphicsElement("gui_menu");

        _scroll.setVisible(true);

        switch(_scrollType){
            case vertical:
                setVerticalPositions();
                break;
            case horizontal:
                setHorizontalPositions();
                break;
        }

    }

    public void update(){
        _scroll.setSize(new Vector2f(98f, 15f));
        _scroll.getTextureOrigin().set(0f, 155f / 512f);
        _scroll.update();
    }

    public void render(){
        _scroll.render();
    }

    public void setValue(float value){
        _value = value;
    }

    public float getValue(){
        return _value;
    }

    public void setScrollType(ScrollType scrollType){
        _scrollType = scrollType;
    }

    private void setVerticalPositions(){
        _scroll.setPosition(new Vector2f(getPosition().x, getPosition().y));
        _scroll.setRotateAngle(90f);
        _scroll.getTextureSize().set(new Vector2f(98f/512f, 15f / 512f));
    }

    private void setHorizontalPositions(){
        _scroll.setPosition(new Vector2f());
    }
}
