package org.terasology.rendering.gui.framework;

import org.terasology.rendering.gui.components.UIScrollBar;

import javax.vecmath.Vector2f;


public class UIScrollableDisplayContainer extends UIDisplayContainer{
    private UIScrollBar _scrollBarH   = null;
    private UIScrollBar _scrollBarV   = null;

    private float      _contentHeight = 1.0f;
    private float      _contentWidth  = 1.0f;

    private float      _verticalShift   = 0.0f;
    private float      _horizontalShift = 0.0f;

    private Vector2f   _containerPosV  = null;
    private Vector2f   _containerPosH  = null;

    public UIScrollableDisplayContainer(){
        super();
        _scrollBarV = new UIScrollBar(getSize(), UIScrollBar.ScrollType.vertical);
        _scrollBarV.setVisible(true);

        _scrollBarH = new UIScrollBar(getSize(),UIScrollBar.ScrollType.horizontal);
        _scrollBarH.setVisible(true);

        addDisplayElement(_scrollBarV);
        addDisplayElement(_scrollBarH);

        _scrollBarV.addScrollListener( new IScrollListener() {
            public void scrolled(UIDisplayElement element) {

                _verticalShift -= _scrollBarV.getScrollShift();

                for(UIDisplayElement displayElement:getDisplayElements()){
                    if(displayElement.canMove()){
                        displayElement.getPosition().y -= _scrollBarV.getScrollShift();
                    }
                }
            }
        });

        _scrollBarH.addScrollListener( new IScrollListener() {
            public void scrolled(UIDisplayElement element) {

                _horizontalShift -= _scrollBarH.getScrollShift();

                for(UIDisplayElement displayElement:getDisplayElements()){
                    if(displayElement.canMove()){
                        displayElement.getPosition().x -= _scrollBarH.getScrollShift();
                    }
                }
            }
        });

    }

    public void setScrollBarsPosition(Vector2f position, Vector2f size){
        _containerPosV = new Vector2f(position.x + size.x, position.y);
        _containerPosH = new Vector2f(position.x, position.y + size.y);

        _scrollBarV.setPosition(_containerPosV);
        _scrollBarV.setMaxMin(0.0f, getSize().y);

        _scrollBarH.setPosition(_containerPosH);
        _scrollBarH.setMaxMin(0.0f, getSize().x);

    }

    public void update(){
        if(!_scrollBarV.isScrolled()){
            updateScrollBars();
        }
        super.update();
    }

    private void updateScrollBars(){
        _contentHeight = 0.0f;
        _contentWidth  = 0.0f;

        float maxElementPosV = 0.0f;
        float maxElementPosH = 0.0f;

        float checkVerticalConfusion   = 0.0f;
        float checkHorizontalConfusion = 0.0f;

        for(UIDisplayElement displayElement:getDisplayElements()){
            if(displayElement.canMove()){

                if(maxElementPosV<displayElement.getPosition().y){
                    maxElementPosV  = displayElement.getPosition().y;
                   _contentHeight  = displayElement.getPosition().y + displayElement.getSize().y + _verticalShift;
                }

                if(maxElementPosH<displayElement.getPosition().x){
                    maxElementPosH  = displayElement.getPosition().x;
                    _contentWidth  = displayElement.getPosition().x + displayElement.getSize().x + _horizontalShift;
                }

                //ToDo: It's not worked. Fix it.
                if(displayElement.getPosition().y<checkVerticalConfusion){
                    checkVerticalConfusion = displayElement.getPosition().y;
                }

                if(displayElement.getPosition().x<checkHorizontalConfusion){
                    checkHorizontalConfusion = displayElement.getPosition().x;
                }
            }
        }


        if(_contentHeight<=getSize().y&&_scrollBarV.isVisible()){
            _scrollBarV.setVisible(false);
        }else if(_contentHeight>getSize().y&&!_scrollBarV.isVisible()){
            _scrollBarV.setPosition(_containerPosV);
            _scrollBarV.setVisible(true);
        }

        if(_contentWidth<=getSize().x&&_scrollBarH.isVisible()){
            _scrollBarH.setVisible(true);
        }else if(_contentWidth>getSize().x&&!_scrollBarH.isVisible()){
            _scrollBarH.setPosition(_containerPosH);
            _scrollBarH.setVisible(true);
        }

        //Todo: It's very bad.
        if(checkVerticalConfusion<0.0f){
            for(UIDisplayElement displayElement:getDisplayElements()){
                if(displayElement.canMove()){
                    displayElement.getPosition().y += (-1)*checkVerticalConfusion;
                }
            }
        }

        if(checkHorizontalConfusion<0.0f){
            for(UIDisplayElement displayElement:getDisplayElements()){
                if(displayElement.canMove()){
                    displayElement.getPosition().x += (-1)*checkHorizontalConfusion;
                }
            }
        }

        _scrollBarV.setStep(_contentHeight, getSize().y);
        _scrollBarH.setStep(_contentWidth,  getSize().x);
    }
}
