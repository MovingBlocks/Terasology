package org.terasology.logic.manager;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

public class GUIManager {
    private static GUIManager _instance      = null;
    private UIDisplayRenderer _renderer;
    private UIDisplayWindow   _focusedWindow = null;

    public GUIManager(){
        _renderer = new UIDisplayRenderer();
        _renderer.setVisible(true);
    }

    public static GUIManager getInstance(){
        if(_instance == null){
            _instance = new GUIManager();
        }
        return _instance;
    }

    public void render(){
        _renderer.render();
    }

    public void update(){
        if(_focusedWindow==null){
            int size = _renderer.getDisplayElements().size();
            if(size>0){
                _focusedWindow = (UIDisplayWindow)_renderer.getDisplayElements().get(size-1);
            }
        }
        _renderer.update();
    }

    public void addWindow(UIDisplayWindow window){
        if(window.isMaximized()){
            _renderer.addtDisplayElementToPosition(0,window);
        }else{
            _renderer.addDisplayElement(window);
        }
    }

    public void removeWindow(UIDisplayWindow window){
        _renderer.removeDisplayElement(window);
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    public void processKeyboardInput(int key) {
        for (UIDisplayElement screen : _renderer.getDisplayElements()) {
            if (screenCanFocus(screen)) {
                screen.processKeyboardInput(key);
            }
        }
    }


    public void processMouseInput(int button, boolean state, int wheelMoved) {

        if(button==0 && state){
            checkTopWindow();
        }

        if(_focusedWindow!=null){
            _focusedWindow.processMouseInput(button, state, wheelMoved);
        }
    }

    private void checkTopWindow(){

        if(_focusedWindow.isModal()&&_focusedWindow.isVisible()){
            return;
        }

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        int size = _renderer.getDisplayElements().size();

        for(int i = size - 1; i>=0; i--){
            UIDisplayWindow window = (UIDisplayWindow)_renderer.getDisplayElements().get(i);
            if(window.isVisible() && window.intersects(mousePos)){
                if(!window.isMaximized()){
                    _renderer.changeElementDepth(i, size-1);
                }
                _focusedWindow = window;
                break;
            };
        }
    }

    private boolean screenCanFocus(UIDisplayElement s) {
        boolean result = true;

        for (UIDisplayElement screen : _renderer.getDisplayElements()) {
            if (screen.isVisible() && !screen.isOverlay() && screen != s)
                result = false;
        }
        return result;
    }
}
