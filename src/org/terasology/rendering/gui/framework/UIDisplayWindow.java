package org.terasology.rendering.gui.framework;

import javax.vecmath.Vector2f;

public class UIDisplayWindow extends UIScrollableDisplayContainer implements Comparable<UIDisplayWindow> {
    
    private Integer _zIndex = 0;

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
    
    public Integer getZindex(){
        return _zIndex;
    }
    
    public void setZindex(int index){
        _zIndex = index;
    }

    public int compareTo(UIDisplayWindow ts) {
        return _zIndex.compareTo(ts.getZindex());
    }

}
