package org.terasology.rendering.gui.components;

/**
 * Created by IntelliJ IDEA.
 * User: kireev
 * Date: 28.02.12
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class UIListItem {
    private Object  _value;
    private String  _text;
    private boolean _isSelected;

    public UIListItem(){

    }

    public Object getValue(){
        return _value;
    }

    public void setValue(Object value){
        _value= value;
    }

    public String getText(){
        return _text;
    }

    public void setText(String text){
       _text = text;
    }

    public boolean isSelected(){
        return _isSelected;
    }

    public void setSelected(boolean selected){
        _isSelected = selected;
    }
}
