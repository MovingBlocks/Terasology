package org.terasology.rendering.gui.components;

import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

public class UIListItem extends UIDisplayContainer {
    private Object  _value;
    private String  _text;
    private boolean _isSelected;
    private Vector2f _padding = new Vector2f(5f, 15f);

    //UI
    private final UIText _label;

    public UIListItem(Vector2f size, String text, Object value){
        setSize(size);
        _text  = text;
        _value = value;

        _label = new UIText();
        _label.setVisible(true);
        _label.setColor(Color.red);
        _label.setPosition(new Vector2f((getPosition().x + _padding.x), (getPosition().y + _padding.y)));
        _label.setText(_text);

        addDisplayElement(_label);

    }

    public Object getValue(){
        return _value;
    }

    public void setValue(Object value){
        _value = value;
    }

    public String getText(){
        return _text;
    }

    public void setText(String text){
        _label.setText(_text);
       _text = text;
    }

    public boolean isSelected(){
        return _isSelected;
    }

    public void setSelected(boolean selected){
        _isSelected = selected;
        if(_isSelected){
            _label.setColor(Color.orange);
        }else{
            _label.setColor(Color.red);
        }
    }

    public void render(){
        super.render();
        /*Vector2f currentPosition = getPosition();
        glPushMatrix();
        //glLoadIdentity();
        glLineWidth(2f);
        glBegin(GL_LINE);
        glColor3f(0.0f, 0.0f, 0.0f);
        glVertex2f(currentPosition.x,  currentPosition.y);
        glVertex2f(currentPosition.x + getSize().x-5f, currentPosition.y);
        glEnd();
        glPopMatrix();*/
    }

    /*public void update(){
        return;
    } */
}
