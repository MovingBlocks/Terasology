package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.IInputDataElement;
import org.terasology.rendering.gui.framework.UIScrollableDisplayContainer;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

public class UIList extends UIScrollableDisplayContainer implements IInputDataElement {

    private int        _selectedItemIndex   = -1;

    //List items
    private List<UIListItem> _items  = new ArrayList<UIListItem>();

    public UIList(Vector2f size) {
        setSize(size);
        setCrop(true);
        setScrollBarsPosition(getPosition(), getSize());

        setStyle("border-image-top",    "gui_menu 159/512 18/512 264/512 0 18");
        setStyle("border-image-right",  "gui_menu 9/512 63/512 423/512 18/512 9");
        setStyle("border-image-bottom", "gui_menu 159/512 9/512 264/512 81/512 9");
        setStyle("border-image-left",   "gui_menu 8/512 64/512 256/512 17/512 8");

        setStyle("border-corner-topleft",     "gui_menu 256/512 0");
        setStyle("border-corner-topright",    "gui_menu 423/512 0");
        setStyle("border-corner-bottomright", "gui_menu 423/512 81/512");
        setStyle("border-corner-bottomleft",  "gui_menu 256/512 81/512");

        setStyle("background-image","gui_menu 159/512 63/512 264/512 18/512");
    }

    public void update(){
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if (intersects(mousePos)) {
            boolean itemClicked = false;
            for (int i = (_items.size() - 1); i >= 0; i--)
            {
                UIListItem item = _items.get(i);
                if(item.isVisible()){
                    if(item.intersects(mousePos)){
                        if(_mouseDown){
                            if(item.isSelected()){
                                break;
                            }
                            if(_selectedItemIndex>=0){
                                _items.get(_selectedItemIndex).setSelected(false);
                            }
                            item.setSelected(true);
                            _selectedItemIndex = i;
                            _mouseDown         = false;
                            itemClicked = true;
                        }
                    }
                }
            }
            if(!itemClicked){
                _mouseUp   = false;
                _mouseDown = false;
            }
        }else{
           _mouseUp = false;
           _mouseDown = false;
        }
        super.update();
    }

    public void render(){
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

        if(_selectedItemIndex<0){
            return;
        }

        removeDisplayElement(_items.get(_selectedItemIndex));
        _items.remove(_selectedItemIndex);

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
            _items.get(_selectedItemIndex).setSelected(true);
        }

    }

    public UIListItem getSelectedItem(){
        return _items.get(_selectedItemIndex);
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

    public Object getValue() {
        return _items.get(_selectedItemIndex).getValue();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearData(){
        _selectedItemIndex = -1;
        _items.get(_selectedItemIndex).setSelected(false);
    }
}