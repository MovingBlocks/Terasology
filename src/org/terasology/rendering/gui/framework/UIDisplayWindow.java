package org.terasology.rendering.gui.framework;

import org.lwjgl.opengl.Display;

import javax.vecmath.Vector2f;

public class UIDisplayWindow extends UIScrollableDisplayContainer{
    
    private boolean _maximized = false;
    private boolean _modal      = false;
    public String name         = "";


    protected void drag(Vector2f value){
        getPosition().x -=value.x;
        getPosition().y -=value.y;
    }

    public void close(boolean clearInputControls){
        setVisible(false);
        setFocus(false);
        
        if(clearInputControls){
            clearInputControls();
        }
    }

    public void show(){
        setVisible(true);
        setFocus(true);
    }

    public void clearInputControls(){
        for (UIDisplayElement element: _displayElements) {
            if(IInputDataElement.class.isInstance(element)){
                IInputDataElement inputControl = (IInputDataElement)element;
                inputControl.clearData();
            }
        }
    }

    public void maximaze(){
        setSize(new Vector2f(Display.getWidth(), Display.getHeight()));
        _maximized = true;
    }

    public boolean isMaximized(){
        return _maximized;
    }

    public boolean isModal(){
        return _modal;
    }

    public void setModal(boolean modal){
        _modal = modal;
    }


}
