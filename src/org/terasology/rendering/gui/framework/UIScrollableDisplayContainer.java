package org.terasology.rendering.gui.framework;

import org.terasology.rendering.gui.components.UIScrollBar;

import javax.vecmath.Vector2f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;


public class UIScrollableDisplayContainer extends UIDisplayContainer{

    /*
     * ScrollBars
     */
    private UIScrollBar _scrollBarVertical   = null;
    private UIScrollBar _scrollBarHorizontal = null;

    private float      _contentHeight         = 1.0f;
    private float      _contentWidth          = 1.0f;
    private float      _scrollShiftVertical   = 0.0f;
    private float      _scrollShiftHorizontal = 0.0f;

    private float      _oldVertivalValue       = 0.0f;
    private float      _oldHorizontalValue     = 0.0f;

    private Vector2f   _containerPosVertical   = null;
    private Vector2f   _containerPosHorizontal = null;

    public UIScrollableDisplayContainer(){
        super();
        _scrollBarVertical   = new UIScrollBar(getSize(), UIScrollBar.ScrollType.vertical);
        _scrollBarHorizontal = new UIScrollBar(getSize(), UIScrollBar.ScrollType.horizontal);

        _scrollBarVertical.setVisible(true);
        _scrollBarHorizontal.setVisible(true);

        _scrollBarVertical.setCroped(false);
        _scrollBarHorizontal.setCroped(false);

        addDisplayElement(_scrollBarVertical);
        addDisplayElement(_scrollBarHorizontal);

        _scrollBarVertical.addScrollListener(new IScrollListener() {
            public void scrolled(UIDisplayElement element) {
                float shift = (_scrollBarVertical.getValue() - _oldVertivalValue);
                _scrollShiftVertical += shift;

                for (UIDisplayElement displayElement : getDisplayElements()) {
                    if (!displayElement.isFixed()) {
                        displayElement.getPosition().y -= shift;
                    }
                }
                _oldVertivalValue = _scrollBarVertical.getValue();
            }
        });

        _scrollBarHorizontal.addScrollListener(new IScrollListener() {
            public void scrolled(UIDisplayElement element) {
                float shift = (_scrollBarHorizontal.getValue() - _oldHorizontalValue);
                _scrollShiftHorizontal += shift;

                for (UIDisplayElement displayElement : getDisplayElements()) {
                    if (!displayElement.isFixed()) {
                        displayElement.getPosition().x -= shift;
                    }
                }
                _oldHorizontalValue = _scrollBarHorizontal.getValue();
            }
        });
    }

    public void setScrollBarsPosition(Vector2f position, Vector2f size){
        _containerPosVertical = new Vector2f(position.x + size.x, position.y);
        _scrollBarVertical.setPosition(_containerPosVertical);
        _scrollBarVertical.setMaxMin(0.0f, getSize().y);

        _containerPosHorizontal = new Vector2f(position.x, position.y + size.y);
        _scrollBarHorizontal.setPosition(_containerPosHorizontal);
        _scrollBarHorizontal.setMaxMin(0.0f, getSize().x);
    }

    public void render(){
        super.render();
    }

    //ToDo Refator this
    public void update(){

        boolean verticalScrollIsScrolled   = _scrollBarVertical.isScrolled();
        boolean horizontalScrollIsScrolled = _scrollBarHorizontal.isScrolled();
        float checkConfusionVertical   = 0.0f;
        float checkConfusionHorizontal = 0.0f;


        if(!verticalScrollIsScrolled){
            _contentHeight = 0.0f;
        }

        if(!horizontalScrollIsScrolled){
            _contentWidth = 0.0f;
        }

        for(UIDisplayElement displayElement:getDisplayElements()){
            if(!displayElement.isFixed()){
                if(!verticalScrollIsScrolled){
                    if(_contentHeight<=(displayElement.getPosition().y + _scrollShiftVertical + displayElement.getSize().y)){
                        _contentHeight = displayElement.getPosition().y + displayElement.getSize().y + _scrollShiftVertical;
                    }

                    if(!_scrollBarVertical.isVisible()&&displayElement.getPosition().y<checkConfusionVertical){
                        checkConfusionVertical = displayElement.getPosition().y;
                    }
                }

                if(!horizontalScrollIsScrolled){
                    if(_contentWidth<=(displayElement.getPosition().x+_scrollShiftHorizontal+ displayElement.getSize().x)){
                        _contentWidth = displayElement.getPosition().x + displayElement.getSize().x + _scrollShiftHorizontal;
                    }

                    if(!_scrollBarHorizontal.isVisible()&&displayElement.getPosition().x<checkConfusionHorizontal){
                        checkConfusionHorizontal = displayElement.getPosition().x;
                    }
                }
            }
        }


        if(_contentHeight<=getSize().y&&_scrollBarVertical.isVisible()){
            _scrollBarVertical.setVisible(false);
        }else if(_contentHeight>getSize().y&&!_scrollBarVertical.isVisible()){
            _scrollBarVertical.setPosition(_containerPosVertical);
            _scrollBarVertical.setVisible(true);
        }

        if(_contentWidth<=getSize().x&&_scrollBarHorizontal.isVisible()){
            _scrollBarHorizontal.setVisible(false);
        }else if(_contentWidth>getSize().x&&!_scrollBarHorizontal.isVisible()){
            _scrollBarHorizontal.setPosition(_containerPosHorizontal);
            _scrollBarHorizontal.setVisible(true);
        }

        if(checkConfusionVertical<0.0f){
            for(UIDisplayElement displayElement:getDisplayElements()){
                if(!displayElement.isFixed()){
                    displayElement.getPosition().y += (-1)*checkConfusionVertical;
                }
            }
        }

        if(checkConfusionHorizontal<0.0f){
            for(UIDisplayElement displayElement:getDisplayElements()){
                if(!displayElement.isFixed()){
                    displayElement.getPosition().x += (-1)*checkConfusionHorizontal;
                }
            }
        }

        _scrollBarVertical.setStep(_contentHeight, getSize().y);

        _scrollBarHorizontal.setStep(_contentWidth, getSize().x);

        super.update();
    }
}