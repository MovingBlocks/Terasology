package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.UIScrollableDisplayContainer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class UIList extends UIScrollableDisplayContainer {
  ///  private final UIText _text;

    //Borders
    private final UIGraphicsElement _borderTop;
    private final UIGraphicsElement _borderRight;
    private final UIGraphicsElement _borderBottom;
    private final UIGraphicsElement _borderLeft;

    //Background
    private Vector4f _backgroundColor = new Vector4f(1.0f,1.0f,1.0f, 0.8f);
    private boolean  _showBackground  = true;

    private UIListItem _selectedItem        = null;
    private int        _selectedItemIndex   = 0;

    //List items
    private List<UIListItem> _items  = new ArrayList<UIListItem>();

    public UIList(Vector2f size) {
        setSize(size);
        setCrop(true);
        setCropMargin(new Vector4f(0.0f, 25f, 0.0f, -5f));
        setScrollBarPosition(new Vector2f(getPosition().x + size.x, getPosition().y));

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

        addDisplayElement(_borderTop);
        addDisplayElement(_borderRight);
        addDisplayElement(_borderBottom);
        addDisplayElement(_borderLeft);
    }

    public void update(){
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if (intersects(mousePos)) {
            for (int i = (_items.size() - 1); i >= 0; i--)
            {
                UIListItem item = _items.get(i);
                if(item.isVisible()){
                    if(item.intersects(mousePos)){
                        //@todo remake it
                        if(_mouseDown){
                            _items.get(_selectedItemIndex).setSelected(false);
                            item.setSelected(true);
                            _selectedItem = item;
                            _selectedItemIndex = i;
                            _mouseDown = false;
                        }
                    }
                }
            }
        }else{
           _mouseUp = false;
           _mouseDown = false;
        }

        updateBorders();
        super.update();
    }

    public void render(){
        if(_showBackground){
            glPushMatrix();
            glLoadIdentity();
            glColor4f(_backgroundColor.x, _backgroundColor.y,_backgroundColor.z, _backgroundColor.w);
            glBegin(GL_QUADS);
            glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y);
            glVertex2f(getPosition().x, getPosition().y + getSize().y);
            glVertex2f(getPosition().x, getPosition().y);
            glVertex2f(getPosition().x + getSize().x, getPosition().y);
            glEnd();
            glPopMatrix();
        }
        super.render();
    }

    private void clicked(){

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

    public void addItem(String text, Object value){
        UIListItem newItem = new UIListItem(new Vector2f(getSize().x, (32f)), text, value);

        newItem.setVisible(true);
        newItem.setPosition(new Vector2f(getPosition().x, getPosition().y + 32f * _items.size()));
        newItem.setIsMoveable(true);
        _items.add(newItem);
        addDisplayElement(_items.get(_items.size() - 1));
    }

    public void removeItem(int index){
        removeDisplayElement(_items.get(index));
        _items.remove(index);
    }

    public void showBackground(boolean show){
        _showBackground = show;
    }
    
    public void setBackgroundColor(Vector4f backgroundColor){
        _backgroundColor = backgroundColor;
    }

}