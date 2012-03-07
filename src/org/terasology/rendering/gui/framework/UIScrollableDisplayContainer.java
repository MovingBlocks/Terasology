package org.terasology.rendering.gui.framework;

import org.terasology.rendering.gui.components.UIScrollBar;

import javax.vecmath.Vector2f;


public class UIScrollableDisplayContainer extends UIDisplayContainer{
    private UIScrollBar _scrollBar        = null;
    private float      _contentHeight     = 1.0f;
    private Vector2f   _containerPos      = null;

    public UIScrollableDisplayContainer(){
        super();
        _scrollBar = new UIScrollBar(getSize());
        _scrollBar.setVisible(true);

        addDisplayElement(_scrollBar);

        _scrollBar.addScrollListener( new IScrollListener() {
            public void scrolled(UIDisplayElement element) {
                for(UIDisplayElement displayElement:getDisplayElements()){
                    if(displayElement.canMove()){
                        displayElement.getPosition().y -= _scrollBar.getScrollShift();
                    }
                }
            }
        });
    }

    public void setScrollBarPosition(Vector2f position){
        _containerPos = new Vector2f(position.x, position.y);
        _scrollBar.setPosition(_containerPos);
        _scrollBar.setMaxMin(0.0f, getSize().y);
    }

    public void update(){
        if(!_scrollBar.isScrolled()){
            _contentHeight = 0.0f;
            float checkConfusion = 0.0f;

            for(UIDisplayElement displayElement:getDisplayElements()){
                if(displayElement.canMove()){
                    _contentHeight += displayElement.getSize().y;

                    if(!_scrollBar.isVisible()&&displayElement.getPosition().y<checkConfusion){
                        checkConfusion = displayElement.getPosition().y;
                    }
                }
            }

            if(_contentHeight<=getSize().y&&_scrollBar.isVisible()){
                _scrollBar.setVisible(false);
            }else if(_contentHeight>getSize().y&&!_scrollBar.isVisible()){
                _scrollBar.setPosition(_containerPos);
                _scrollBar.setVisible(true);
            }

            if(checkConfusion<0.0f){
                for(UIDisplayElement displayElement:getDisplayElements()){
                    if(displayElement.canMove()){
                        displayElement.getPosition().y += (-1)*checkConfusion;
                    }
                }
            }

            _scrollBar.setStep(_contentHeight, getSize().y);
        }
        super.update();
    }
}
