package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.IScrollListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class UIScrollBar extends UIDisplayContainer {

    //Graphics
    private List<UIGraphicsElement> _scrollGraphicsElements = new ArrayList<UIGraphicsElement>(); // <- HEADER(0); -- BODY(1); -> FOOTER(2)


    //Scroll Listeners
    private final ArrayList<IScrollListener> _scrollListeners = new ArrayList<IScrollListener>();

    //Options
    private float   _max;
    private float   _min;
    private float   _step;

    private Vector2f _prevMousePos = null;

    private ScrollType _scrollType = ScrollType.vertical;

    private boolean _scrolled = false;

    private float _containerHeight = 0.0f;
    private float _scrollShift     = 0.0f;


    public static enum ScrollType {
        vertical, horizontal
    }

    public UIScrollBar(Vector2f size){
        for(int i=0; i<3; i++){
            _scrollGraphicsElements.add(new UIGraphicsElement("gui_menu"));
            _scrollGraphicsElements.get(i).setVisible(true);
            addDisplayElement(_scrollGraphicsElements.get(i));
        }

        switch(_scrollType){
            case vertical:
                setSize(new Vector2f(15f, size.y));
                setVerticalOptions(size);
                break;
            case horizontal:
                setSize(new Vector2f(size.x, 15f));
                setHorizontalPositions();
                break;
        }

    }

    public void update(){
        updateGraphicsElementsPosition();

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if(intersects(mousePos)){
            if(_mouseDown){
                _scrolled = true;
                if(_prevMousePos==null){
                    _prevMousePos =  mousePos;
                }
            }
        }

        if(_scrolled){
            scrolled(mousePos);
        }

        if(!_mouseDown||!_scrolled || _mouseUp){
            _scrolled     = false;
            _mouseDown    = false;
            _prevMousePos = null;
            _mouseUp      = false;
        }
    }

    private void scrolled(Vector2f mousePos){

        if(_max<(getPosition().y +  mousePos.y - _prevMousePos.y  + getSize().y)){
            mousePos.y = _max - getSize().y + _prevMousePos.y - getPosition().y;
        }else if(_min>(getPosition().y +  mousePos.y - _prevMousePos.y)){
            mousePos.y = _min + _prevMousePos.y - getPosition().y;
        }

        float tempPos = getPosition().y +  mousePos.y - _prevMousePos.y;
        _scrollShift = (mousePos.y - _prevMousePos.y)/_step;
        getPosition().y = tempPos;
        _prevMousePos   = mousePos;

        for (int i = 0; i < _scrollListeners.size(); i++) {
            _scrollListeners.get(i).scrolled(this);
        }
    }

    public void addScrollListener(IScrollListener listener) {
        _scrollListeners.add(listener);
    }

    private void updateGraphicsElementsPosition(){
        float newScrollSize     = _containerHeight*_step;
        float newBodyScrollSize = newScrollSize - _scrollGraphicsElements.get(0).getSize().x*2;

        if(newScrollSize!=getSize().y){
            setSize(new Vector2f(15f, newScrollSize));

            _scrollGraphicsElements.get(1).setSize(new Vector2f(newBodyScrollSize, 15f));
            _scrollGraphicsElements.get(2).getPosition().y = _scrollGraphicsElements.get(1).getPosition().y +
                                                             _scrollGraphicsElements.get(0).getSize().x     +
                                                             _scrollGraphicsElements.get(1).getSize().x;

            if(getPosition().y + getSize().y>_max){
                _prevMousePos = getPosition();
                scrolled(new Vector2f(getPosition().x, (getPosition().y + getSize().y) - _max));
            }

        }

        _scrollGraphicsElements.get(0).getTextureOrigin().set(0f, 155f / 512f);
        _scrollGraphicsElements.get(1).getTextureOrigin().set(7f/512f, 155f / 512f);
        _scrollGraphicsElements.get(2).getTextureOrigin().set(0f, 155f / 512f);

    }

    public void setScrollType(ScrollType scrollType){
        _scrollType = scrollType;
    }

    private void setVerticalOptions(Vector2f size){
        /*SET POS FOR HEADER*/
        _scrollGraphicsElements.get(0).setRotateAngle(90);
        _scrollGraphicsElements.get(0).setPosition(getPosition());
        _scrollGraphicsElements.get(0).getPosition().x += 15f;
        _scrollGraphicsElements.get(0).setSize(new Vector2f(7f, 15f));
        _scrollGraphicsElements.get(0).getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));

        /*SET POS FOR BODY*/
        _scrollGraphicsElements.get(1).setRotateAngle(90);
        _scrollGraphicsElements.get(1).setPosition(new Vector2f(getPosition().x, getPosition().y +  _scrollGraphicsElements.get(0).getSize().x));
        _scrollGraphicsElements.get(1).getPosition().x += 15f;
        _scrollGraphicsElements.get(1).getTextureSize().set(new Vector2f(10f/512f, 15f / 512f));

        /*SET POS FOR FOOTER*/
        _scrollGraphicsElements.get(2).setRotateAngle(270);
        _scrollGraphicsElements.get(2).setPosition(new Vector2f(getPosition().x, getPosition().y +  2*_scrollGraphicsElements.get(0).getTextureSize().y + _scrollGraphicsElements.get(1).getSize().y));
        _scrollGraphicsElements.get(2).setSize(new Vector2f(7f, 15f));
        _scrollGraphicsElements.get(2).getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));

    }

    private void setHorizontalPositions(){
        /*SET POS FOR HEADER*/
        _scrollGraphicsElements.get(0).setPosition(getPosition());
        _scrollGraphicsElements.get(0).setSize(new Vector2f(7f, 15f));
        _scrollGraphicsElements.get(0).getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));

        /*SET POS FOR BODY*/
        _scrollGraphicsElements.get(1).setPosition(new Vector2f(getPosition().x + _scrollGraphicsElements.get(0).getSize().x, getPosition().y));
        _scrollGraphicsElements.get(1).getTextureSize().set(new Vector2f(10f/512f, 15f / 512f));

        /*SET POS FOR FOOTER*/
        _scrollGraphicsElements.get(2).setRotateAngle(180);
        _scrollGraphicsElements.get(2).setPosition(new Vector2f((getPosition().x +  2*_scrollGraphicsElements.get(0).getTextureSize().x + _scrollGraphicsElements.get(1).getSize().x), getPosition().y));
        _scrollGraphicsElements.get(2).setSize(new Vector2f(7f, 15f));
        _scrollGraphicsElements.get(2).getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));
    }

    public void setMaxMin(float min, float max){
        _min = min;
        _max = max;
    }

    public boolean isScrolled(){
        return _scrolled;
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