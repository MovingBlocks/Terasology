package org.terasology.rendering.gui.framework;

import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.GUIManager;

import javax.vecmath.Vector2f;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class UIDisplayWindow extends UIScrollableDisplayContainer{
    
    private boolean _maximized = false;
    private boolean _modal     = false;
    private HashMap<String, UIDisplayElement> _displayElementsById = new HashMap<String, UIDisplayElement>();


    protected void drag(Vector2f value){
        getPosition().x -=value.x;
        getPosition().y -=value.y;
    }

    public void close(boolean clearInputControls){
        setVisible(false);
        setFocus(false);
        GUIManager.getInstance().setFocusedFromLast();
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

    public void render(){
       if(isModal()){
           renderOverlay();
       }
       super.render();
    }

    public void renderOverlay(){
        glPushMatrix();
        glLoadIdentity();
        glColor4f(0, 0, 0, 0.75f);
        glBegin(GL_QUADS);
        glVertex2f(0f, 0f);
        glVertex2f((float) Display.getWidth(), 0f);
        glVertex2f((float) Display.getWidth(), (float) Display.getHeight());
        glVertex2f(0f, (float) Display.getHeight());
        glEnd();
        glPopMatrix();
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

    public void addDisplayElement(UIDisplayElement element, String elementId) {
        _displayElements.add(element);
        _displayElementsById.put(elementId, element);
        element.setParent(this);
    }

    public UIDisplayElement getElementById(String elementId){
        if(!_displayElementsById.containsKey(elementId)){
            return null;
        }

        return _displayElementsById.get(elementId);
    }
}
