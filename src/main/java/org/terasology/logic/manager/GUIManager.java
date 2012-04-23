package org.terasology.logic.manager;

import com.google.common.collect.Lists;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.components.UIMessageBox;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.List;

/**
 * First version of simple GUI manager.
 * ToDo Init styles here
 * ToDo Add GUI manager to single player
 *
 * @author Kireev Anton <adeon.k87@gmail.com>
 */

public class GUIManager {
    private static GUIManager _instance;
    private UIDisplayRenderer _renderer;
    private UIDisplayWindow   _focusedWindow;
    private UIDisplayWindow   _lastFocused;
    private HashMap<String, UIDisplayWindow> _windowsById = new HashMap<String, UIDisplayWindow>();

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

    public void addWindow(UIDisplayWindow window, String windowId){
        if(window.isMaximized()){
            _renderer.addtDisplayElementToPosition(0,window);
        }else{
            _renderer.addDisplayElement(window);
        }

        _windowsById.put(windowId, window);
    }

    public void closeWindows() {
        List<String> windowIds = Lists.newArrayList(_windowsById.keySet());
        for (String windowId : windowIds) {
            removeWindow(windowId);
        }
    }

    public void removeWindow(UIDisplayWindow window){
        _renderer.removeDisplayElement(window);

        if(_windowsById.containsValue(window)){
            for(String key : _windowsById.keySet()){
                if( _windowsById.get(key).equals(window) ){
                    _windowsById.remove(key);
                    break;
                }
            }
        }

        if (_focusedWindow == window) {
            _focusedWindow = null;
        }
        if (_lastFocused == window) {
            _lastFocused = null;
        } else {
            _focusedWindow = _lastFocused;
        }
    }

    public void removeWindow(String windowId){
        UIDisplayWindow window = getWindowById(windowId);

        _renderer.removeDisplayElement(window);

        if(_windowsById.containsValue(window)){
            for(String key : _windowsById.keySet()){
                if( _windowsById.get(key).equals(window) ){
                    _windowsById.remove(key);
                    break;
                }
            }
        }

        if (_focusedWindow == window) {
            _focusedWindow = null;
        }
        if (_lastFocused == window) {
            _lastFocused = null;
        } else {
            _focusedWindow = _lastFocused;
        }
    }

    public UIDisplayWindow getWindowById(String windowId){
        if( _windowsById.containsKey(windowId) ){
            return _windowsById.get(windowId);
        }else{
            return null;
        }
    }

    public UIDisplayWindow getFocusedWindow() {
        return _focusedWindow;
    }

    /**
     * Process keyboard input - first look for "system" like events, then otherwise pass to the Player object
     */
    public void processKeyboardInput(int key) {
        List<UIDisplayElement> screens = Lists.newArrayList(_renderer.getDisplayElements());
        for (UIDisplayElement screen : screens) {
            if (screen.isVisible() && !screen.isOverlay()) {
                screen.processKeyboardInput(key);
            }
        }
    }


    public void processMouseInput(int button, boolean state, int wheelMoved) {

        if(button==0 && state){
            checkTopWindow();
        }

        if(_focusedWindow != null){
            _focusedWindow.processMouseInput(button, state, wheelMoved);
        }
    }

    public void setFocusedWindow(UIDisplayWindow window){
        int size = _renderer.getDisplayElements().size();

        for(int i = 0; i < size; i++){
            if( window.equals( _renderer.getDisplayElements().get(i) ) ){
                setTopWindow(i);
                break;
            }
        }
    }
    
    public void setFocusedFromLast(){

        if(_lastFocused == null || _lastFocused.equals(_focusedWindow)){
            return;
        }

        if( _focusedWindow.isMaximized() && _lastFocused.isMaximized() ){
            _focusedWindow.setVisible(false);
        }

        _focusedWindow = _lastFocused;

        if(!_focusedWindow.isVisible()){
            _focusedWindow.setVisible(true);
        }
    }

    public void setFocusedWindow(String windowId){
        if(_windowsById == null || _windowsById.size()<1 || !_windowsById.containsKey(windowId)){
            return;
        }
        setFocusedWindow(_windowsById.get(windowId));
    }

    private void setTopWindow(int windowPosition){

        if(_renderer.getDisplayElements().size()-1 < windowPosition || windowPosition < 0){
            return;
        }

        UIDisplayWindow setTopWindow = (UIDisplayWindow)_renderer.getDisplayElements().get(windowPosition);

        if(setTopWindow == null){
            return;
        }

        if(_lastFocused!=null&&_lastFocused.equals(setTopWindow)){
            return;
        }

        _lastFocused = _focusedWindow;

        if(_lastFocused!=null && _lastFocused.isMaximized() && setTopWindow.isMaximized()){
            _lastFocused.setVisible(false);
        }

        _focusedWindow = setTopWindow;

        if( !_focusedWindow.isMaximized() ){
            _renderer.changeElementDepth(windowPosition, _renderer.getDisplayElements().size()-1);
        }

        if(!_focusedWindow.isVisible()){
            _focusedWindow.setVisible(true);
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
                setTopWindow(i);
                break;
            };
        }
    }

    public void showMessage(String title, String text){
        UIDisplayWindow messageWindow = new UIMessageBox(title, text);
        messageWindow.setVisible(true);
        messageWindow.center();
        addWindow(messageWindow, "messageBox");
        setFocusedWindow(messageWindow);
    }

    public void setLasFocused(){
        _focusedWindow = _lastFocused;
    }

    /*private boolean screenCanFocus(UIDisplayElement s) {
        boolean result = true;

        for (UIDisplayElement screen : _renderer.getDisplayElements()) {
            if (screen.isVisible() && !screen.isOverlay() && screen != s)
                result = false;
        }
        return result;
    } */
}
