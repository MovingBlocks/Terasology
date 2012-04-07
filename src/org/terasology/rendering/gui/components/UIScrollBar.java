package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.IScrollListener;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

import java.util.ArrayList;

public class UIScrollBar extends UIDisplayContainer {

    //ScrollBarElements
    private UIScrollBarThumb _scrolBarThumb;

    //Scroll Listeners
    private final ArrayList<IScrollListener> _scrollListeners = new ArrayList<IScrollListener>();

    //Options
    private float   _max;
    private float   _min;

    private float   _step;

    private float _prevMousePos = -1;

    private ScrollType _scrollType = ScrollType.vertical;

    private boolean _scrolled = false;

    private float _containerLength     = 0.0f;
    private float _contentLength       = 0.0f;
    private float _value               = 0.0f;
    private float _oldValue            = 0.0f;


    public static enum ScrollType {
        vertical, horizontal
    }

    public UIScrollBar(Vector2f size, ScrollType scrollType){
        setScrollType(scrollType);
        switch(_scrollType){
            case vertical:
                setSize(new Vector2f(15f, size.y));
                _scrolBarThumb = new UIScrollBarThumb(getSize(), ScrollType.vertical);
                break;
            case horizontal:
                setSize(new Vector2f(size.x, 15f));
                _scrolBarThumb = new UIScrollBarThumb(getSize(), ScrollType.horizontal);
                break;
        }
        _scrolBarThumb.setVisible(true);
        addDisplayElement(_scrolBarThumb);
    }

    public void update(){
        updateThumb();

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if(intersects(mousePos)){
            if(_mouseDown){
                _scrolled = true;
                if(_prevMousePos==-1){
                    if(_scrollType==ScrollType.vertical){
                        _prevMousePos =  mousePos.y;
                    }else{
                        _prevMousePos =  mousePos.x;
                    }
                }
            }
        }

        if(_scrolled){
            scrolled(_scrollType==ScrollType.vertical?mousePos.y:mousePos.x);
        }

        if(!_mouseDown||!_scrolled || _mouseUp){
            _scrolled     = false;
            _mouseDown    = false;
            _prevMousePos = -1;
            _mouseUp      = false;
        }
        super.update();
    }

    private void scrolled(float mousePos){

        if(_max<(_scrolBarThumb.getThumbPosition() +  mousePos - _prevMousePos  + _scrolBarThumb.getThumbSize())){
            mousePos = _max - _scrolBarThumb.getThumbSize() + _prevMousePos - _scrolBarThumb.getThumbPosition();
        }else if(_min>(_scrolBarThumb.getThumbPosition() +  mousePos - _prevMousePos)){
            mousePos = _min + _prevMousePos - _scrolBarThumb.getThumbPosition();
        }

          
        _scrolBarThumb.setThumbPosition((_scrolBarThumb.getThumbPosition() +  mousePos - _prevMousePos));

        calculateValue();

        _prevMousePos   = mousePos;

        for (int i = 0; i < _scrollListeners.size(); i++) {
            _scrollListeners.get(i).scrolled(this);
        }
    }

    public void addScrollListener(IScrollListener listener) {
        _scrollListeners.add(listener);
    }

    private void updateThumb(){
        float newScrollSize     = _containerLength*_step;


        if(newScrollSize!=_scrolBarThumb.getThumbSize()){
            _scrolBarThumb.resize(newScrollSize);
        }

        if((_scrolBarThumb.getThumbPosition() + _scrolBarThumb.getThumbSize()>_max)){
            _prevMousePos = _scrolBarThumb.getThumbPosition();
            scrolled((_scrolBarThumb.getThumbPosition() + _scrolBarThumb.getThumbSize()) - _max);
        }
        calculateValue();
    }

    public void setScrollType(ScrollType scrollType){
        _scrollType = scrollType;
    }


    private void calculateValue(){
        _value = _scrolBarThumb.getThumbPosition()/_step;
    }


    public void setMaxMin(float min, float max){
        _min = min;
        _max = max;

        switch(_scrollType){
            case vertical:
                setSize(new Vector2f(15f, max));
                break;
            case horizontal:
                setSize(new Vector2f(max, 15f));
                break;
        }
    }

    public boolean isScrolled(){
        return _scrolled;
    }

    public void setStep(float contentLength, float containerLength){
        try{
            _step = containerLength/contentLength;

            if(_step>1.0f){
                _step = 1.0f;
            }

        }catch(ArithmeticException e){
            _step = 1.0f;
        }

        _containerLength = containerLength;
        _contentLength   = contentLength;
    }

    public float getValue(){
        return _value;
    }

    public float getOldValue(){
        return _oldValue;
    }

    public float getStep(){
        return _step;
    }
}