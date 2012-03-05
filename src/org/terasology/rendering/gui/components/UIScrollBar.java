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
    private float   _max;
    private float   _min;

    private float _step;
    private float _value;

    private Vector2f _prevMousePos = null;

    private ScrollType _scrollType = ScrollType.vertical;

    private boolean _moveable = false;

    private float _containerHeight = 0.0f;
    private float _scrollShift     = 0.0f;


    public static enum ScrollType {
        vertical, horizontal
    }

    public UIScrollBar(Vector2f size){

        _scroll = new UIGraphicsElement("gui_menu");
        _scroll.setVisible(true);

        switch(_scrollType){
            case vertical:
                setVerticalOptions(size);
                break;
            case horizontal:
                setHorizontalPositions();
                break;
        }

    }

    public void update(){
        float newScrollSize = _containerHeight*_step;

        if(newScrollSize!=getSize().y){
            _scroll.getPosition().y += newScrollSize - getSize().y;
            setSize(new Vector2f(15f, newScrollSize));
        }

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if(intersects(mousePos)){
            if(_mouseDown){
                _moveable = true;
            }

            if(_mouseUp){
                _moveable     = false;
                _mouseDown    = false;
                _prevMousePos = null;
            }
        }else{
            if(!_mouseDown||!_moveable){
                _prevMousePos = null;
                _mouseDown    = false;
                _mouseUp      = false;
                _moveable     = false;
                _scrollShift  = 0.0f;
            }
        }

        if(_moveable){
            if(_prevMousePos==null){
                _prevMousePos =  mousePos;
            }else{
                float tempPos = getPosition().y +  mousePos.y - _prevMousePos.y;

                if(_max>(tempPos  + getSize().y)&&_min<tempPos){
                    _scrollShift = (mousePos.y - _prevMousePos.y)*_step;
                    getPosition().y = tempPos;
                    _prevMousePos   = mousePos;
                }else{
                    _scrollShift = 0.0f;
                }

            }
        }

        _scroll.setSize(new Vector2f(getSize().y, 15f));
        _scroll.getTextureOrigin().set(0f, 155f / 512f);
        _scroll.update();
    }

    public void render(){
        glPushMatrix();
        glTranslatef(_scroll.getPosition().x, _scroll.getPosition().y, 0);
        _scroll.render();
        glPopMatrix();
    }

    public void setScrollType(ScrollType scrollType){
        _scrollType = scrollType;
    }

    private void setVerticalOptions(Vector2f size){
        setSize(new Vector2f(15f, size.y));
        _scroll.setRotateAngle(270f);
        _scroll.getPosition().y += size.y;
        _scroll.getTextureSize().set(new Vector2f(98f/512f, 15f / 512f));
    }

    private void setHorizontalPositions(){
        _scroll.setPosition(new Vector2f());
    }

    public void setMaxMin(float min, float max){
        _min = min;
        _max = max;
    }

    public boolean isMoveable(){
        return _moveable;
    }

    public void setStep(float contentHeight, float containerHeight){
        try{
            _step = 1.0f/(contentHeight/containerHeight);

            if(_step>1.0f){
                _step = 1.0f;
            }

        }catch(ArithmeticException e){
            _step = 1.0f;
        }

        _containerHeight = containerHeight;
    }

    public float getScrollShift(){
        return _scrollShift;
    }
}