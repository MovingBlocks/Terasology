package org.terasology.rendering.gui.components;


import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import javax.vecmath.Vector2f;


public class UIScrollBarThumb extends UIDisplayContainer {
    //Graphics
    private UIGraphicsElement _header;
    private UIGraphicsElement _body;
    private UIGraphicsElement _footer;

    private UIScrollBar.ScrollType _scrollType = UIScrollBar.ScrollType.vertical;

    public UIScrollBarThumb(Vector2f size, UIScrollBar.ScrollType scrollType){
        setSize(size);
        _scrollType =  scrollType;

        _header = new UIGraphicsElement("gui_menu");
        _body   = new UIGraphicsElement("gui_menu");
        _footer = new UIGraphicsElement("gui_menu");

        _header.setVisible(true);
        _body.setVisible(true);
        _footer.setVisible(true);

        addDisplayElement(_header);
        addDisplayElement(_body);
        addDisplayElement(_footer);

        switch(scrollType){
            case vertical:
                setVerticalOptions();
                break;
            case horizontal:
                setHorizontalPositions();
                break;
        }

        _header.getTextureOrigin().set(0f, 155f / 512f);
        _body.getTextureOrigin().set(7f/512f, 155f / 512f);
        _footer.getTextureOrigin().set(0f, 155f / 512f);
    }

    private void setVerticalOptions(){
        /*SET POS FOR HEADER*/
        _header.setRotateAngle(90);
        _header.setPosition(getPosition());
        _header.getPosition().x += 15f;
        _header.setSize(new Vector2f(7f, 15f));
        _header.getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));

        /*SET POS FOR BODY*/
        _body.setRotateAngle(90);
        _body.setPosition(new Vector2f(getPosition().x, getPosition().y +  _header.getSize().x));
        _body.getPosition().x += 15f;
        _body.getTextureSize().set(new Vector2f(10f/512f, 15f / 512f));

        /*SET POS FOR FOOTER*/
        _footer.setRotateAngle(270);
        _footer.setPosition(new Vector2f(getPosition().x, getPosition().y +  2*_header.getTextureSize().y + _body.getSize().y));
        _footer.setSize(new Vector2f(7f, 15f));
        _footer.getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));
    }

    private void setHorizontalPositions(){
        /*SET POS FOR HEADER*/
        _header.setPosition(getPosition());
        _header.setSize(new Vector2f(7f, 15f));
        _header.getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));

        /*SET POS FOR BODY*/
        _body.setPosition(new Vector2f(getPosition().x + _header.getSize().x, getPosition().y));
        _body.getTextureSize().set(new Vector2f(10f/512f, 15f / 512f));

        /*SET POS FOR FOOTER*/
        _footer.setRotateAngle(180);
        _footer.setPosition(new Vector2f((getPosition().x +  2*_header.getTextureSize().x + _body.getSize().x), getPosition().y));
        _footer.setSize(new Vector2f(7f, 15f));
        _footer.getPosition().y += 15f;
        _footer.getTextureSize().set(new Vector2f(7f/512f, 15f / 512f));
    }

    public void resize(float newScrollSize){
        float newBodyScrollSize = newScrollSize - _header.getSize().x*2;

        if(_scrollType== UIScrollBar.ScrollType.vertical){
            setSize(new Vector2f(15f, newScrollSize));

            _body.setSize(new Vector2f(newBodyScrollSize, 15f));
            _footer.getPosition().y = _body.getPosition().y +
                    _header.getSize().x     +
                    _body.getSize().x;
        }else{
            setSize(new Vector2f(newScrollSize, 15f));

            _body.setSize(new Vector2f(newBodyScrollSize, 15f));
            _footer.getPosition().x = _body.getPosition().x +
                                      _header.getSize().x   +
                                      _body.getSize().x;
        }
    }

    public float getThumbPosition(){
        if(_scrollType == UIScrollBar.ScrollType.vertical){
            return getPosition().y;
        }
        return getPosition().x;
    }

    public float getThumbSize(){
        if(_scrollType == UIScrollBar.ScrollType.vertical){
            return getSize().y;
        }
        return getSize().x;
    }

    public void setThumbPosition(float newPosition){
        if(_scrollType == UIScrollBar.ScrollType.vertical){
            getPosition().y = newPosition;
        }else{
            getPosition().x = newPosition;
        }
    }

}
