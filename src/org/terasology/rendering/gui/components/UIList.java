package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIScrollableDisplayContainer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class UIList extends UIScrollableDisplayContainer {
    //Background
    private Vector4f _backgroundColor = new Vector4f(1.0f,1.0f,1.0f, 0.8f);
    private boolean  _showBackground  = true;

    private UIListItem _selectedItem        = null;
    private int        _selectedItemIndex   = -1;

    //List items
    private List<UIListItem> _items  = new ArrayList<UIListItem>();

    public UIList(Vector2f size) {
        setSize(size);
        setCrop(true);
        setScrollBarsPosition(getPosition(), getSize());
        setBorderTexture("gui_menu", new Vector2f(256f/512f, 4f / 512f), new Vector2f(0f, 150f / 512f), 4f);
    }

    public void update(){
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if (intersects(mousePos)) {
            for (int i = (_items.size() - 1); i >= 0; i--)
            {
                UIListItem item = _items.get(i);
                if(item.isVisible()){
                    if(item.intersects(mousePos)){
                        //todo refactor it
                        if(_mouseDown){
                            if(_selectedItemIndex>=0){
                                _items.get(_selectedItemIndex).setSelected(false);
                            }
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

    public int size(){
        return _items.size();
    }



    public void addItem(String text, Object value){

        UIListItem newItem  = new UIListItem(new Vector2f(getSize().x, (32f)), text, value);

        newItem.setVisible(true);

        if(_items.size()>0){
            newItem.setPosition(_items.get(0).getPosition());
        }

        newItem.getPosition().y += 32f * _items.size();
        newItem.setFixed(false);

        _items.add(newItem);
        addDisplayElement(newItem);
    }

    public void removeSelectedItem(){

        if(_selectedItemIndex<0 || _selectedItem == null){
            return;
        }

        Vector2f deletedElementPosition =_items.get(_selectedItemIndex).getPosition();

        removeDisplayElement(_selectedItem);
        _items.remove(_selectedItemIndex);
        _selectedItem = null;

        for(int i=_selectedItemIndex; i<_items.size(); i++){
            _items.get(i).getPosition().y -= 32f;
        }

        if(_selectedItemIndex>_items.size()-1){
            if(_items.size()-1>=0){
                _selectedItemIndex = _items.size()-1;
                _items.get(_selectedItemIndex).setSelected(true);
            }else{
                _selectedItemIndex = -1;
            }
        }

        if(_selectedItemIndex>=0){
            _selectedItem = _items.get(_selectedItemIndex);
            _selectedItem.setSelected(true);
        }

    }

    public void removeAll(){
        for (int i = (_items.size() - 1); i >= 0; i--)
        {
            removeDisplayElement(_items.get(i));
            _items.remove(i);
        }
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