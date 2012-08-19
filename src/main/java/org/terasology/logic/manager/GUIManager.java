/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.manager;

import com.google.common.collect.Lists;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.input.KeyEvent;
import org.terasology.events.input.MouseButtonEvent;
import org.terasology.events.input.MouseWheelEvent;
import org.terasology.events.input.MouseXAxisEvent;
import org.terasology.events.input.MouseYAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.ButtonState;
import org.terasology.rendering.gui.components.UIMessageBox;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import java.util.HashMap;
import java.util.List;

/**
 * First version of simple GUI manager.
 * ToDo Init styles here
 * ToDo Add GUI manager to single player
 *
 * @author Kireev Anton <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */

public class GUIManager implements EventHandlerSystem {
    
    private static GUIManager instance;
    private HashMap<String, UIDisplayWindow> windowsById = new HashMap<String, UIDisplayWindow>();
    
    //renderer
    private UIDisplayRenderer renderer;

    public GUIManager() {
        renderer = new UIDisplayRenderer();
        renderer.setVisible(true);
    }

    public static GUIManager getInstance() {
        if (instance == null) {
            instance = new GUIManager();
            
        }
        return instance;
    }

    /**
     * Render all visible display elements an their child's.
     */
    public void render() {
        renderer.render();
    }

    /**
     * Updates all visible display elements and their child's. Will update the layout if the display was resized.
     */
    public void update() {        
        renderer.update();

        if (Display.wasResized()) {
            renderer.layout();
        }
    }

    /**
     * Add an window to the UI. Therefore it can be rendered and updated.
     * @param window The window to add.
     * @param windowId The id of the window, to access windows by id.
     * @return Returns the added window.
     */
    public <T extends UIDisplayWindow> T addWindow(T window, String windowId) {
        renderer.addtDisplayElementToPosition(0, window);
        windowsById.put(windowId, window);
        
        if (windowsById.size() == 1) {
            setFocusedWindow(windowsById.get(0));
        }
        
        return window;
    }

    /**
     * Close all windows which where added and remove them from the GUIManager. Therefore they won't be updated or rendered anymore.
     */
    public void removeAllWindows() {
        List<String> windowIds = Lists.newArrayList(windowsById.keySet());
        for (String windowId : windowIds) {
            removeWindow(windowId);
        }
    }

    /**
     * Close the given window and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     * @param window The window by reference to remove.
     */
    public void removeWindow(UIDisplayWindow window) {
        
        if (window == null) {
            return;
        }
        
        renderer.removeDisplayElement(window);

        for (String key : windowsById.keySet()) {
            if (windowsById.get(key).equals(window)) {
                windowsById.remove(key);
                break;
            }
        }
    }
    
    /**
     * Close the given window and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     * @param windowId The window by id to remove.
     */
    public void removeWindow(String windowId) {
        removeWindow(getWindowById(windowId));
    }

    /**
     * Get a window reference which was added to the GUIManager through their id.
     * @param windowId The window id.
     * @return Returns the reference of the window with the given id or null if there is none with this id.
     */
    public UIDisplayWindow getWindowById(String windowId) {
        if (windowsById.containsKey(windowId)) {
            return windowsById.get(windowId);
        } else {
            return null;
        }
    }

    /**
     * Set the focus to the given window by its reference. The focused window will be set on the top of the layer.
     * @param window The window reference.
     */
    public void setFocusedWindow(UIDisplayWindow window) {
    	renderer.setWindowFocus(window);
    	checkMouseMovement();
    }
    
    /**
     * Set the focus to the given window by its id. The focused window will be set on the top of the layer.
     * @param windowId The window id.
     */
    public void setFocusedWindow(String windowId) {
        if (windowsById == null || windowsById.size() < 1 || !windowsById.containsKey(windowId)) {
            return;
        }
        
        setFocusedWindow(windowsById.get(windowId));
    }

    /**
     * Get the focused window.
     * @return Returns the focused window.
     */
	public UIDisplayWindow getFocusedWindow() {
		return renderer.getWindowFocused();
	}

	/**
	 * Check whether the mouse of the current focused window is visible and can be moved on the display.
	 */
    public void checkMouseMovement() {
        if (isConsumingInput() || renderer.getWindowFocused() == null) {
            Mouse.setGrabbed(false);
        } else {
            Mouse.setGrabbed(true);
        }
    }
    
    /**
     * Check if the GUI will consume the input events. Input events are changes of the mouse position, mouse button, mouse wheel and keyboard input.
     * @return Returns true if the GUI will consume the input events.
     */
    public boolean isConsumingInput() {
        return renderer.getWindowFocused() != null && renderer.getWindowFocused().isModal() && renderer.getWindowFocused().isVisible();
    }

    /**
     * Show a message dialog.
     * @param title The title of the dialog.
     * @param text The text of the dialog.
     */
    public void showMessage(String title, String text) {
        UIDisplayWindow messageWindow = new UIMessageBox(title, text);
        messageWindow.center();
        addWindow(messageWindow, "messageBox");
        setFocusedWindow(messageWindow);
    }
    
    
    
    /*
       The following methods are responsible for receiving and processing mouse and keyboard inputs.
    */
    
    /**
     * Process the mouse input on the active window.
     * @param button The button. Left = 0, Right = 1, Middle = 2.
     * @param state The state of the button. True for pressed.
     * @param wheelMoved The mouse wheel movement. wheel = 0 for no movement. wheel > 0 for up. wheel < 0 for down.
     * 
     */
    private void processMouseInput(int button, boolean state, int wheelMoved) {
        if (renderer.getWindowFocused() != null) {
        	renderer.getWindowFocused().processMouseInput(button, state, wheelMoved);
        }
    }
    
    /**
     * Process the raw keyboard input.
     * @param event The event of the pressed key.
     */
    private void processKeyboardInput(KeyEvent event) {
        if (renderer.getWindowFocused() != null && renderer.getWindowFocused().isModal() && renderer.getWindowFocused().isVisible()) { //TODO change this
        	renderer.getWindowFocused().processKeyboardInput(event);
            return;
        }

        List<UIDisplayElement> screens = Lists.newArrayList(renderer.getDisplayElements());
        for (UIDisplayElement screen : screens) {
            if (!((UIDisplayWindow) screen).isModal()) {
                screen.processKeyboardInput(event);
            }
        }
    }

    /**
     * Process the bind buttons input.
     * @param event The event of the bind button.
     */
    private void processBindButton(BindButtonEvent event) {
        if (renderer.getWindowFocused() != null && renderer.getWindowFocused().isModal() && renderer.getWindowFocused().isVisible()) { //TODO change this
        	renderer.getWindowFocused().processBindButton(event);
        }
    }
    
    @Override
    public void initialise() {
        
    }

    @Override
    public void shutdown() {
        
    }
    
    /*
      The following events will capture the mouse and keyboard inputs. They have the highest priority so the GUI will always come first.
      If a window is "modal" it will consume all input events so no other than the GUI will handle these events.
    */
    
    //mouse movement events
    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onMouseX(MouseXAxisEvent event, EntityRef entity) {
        if (isConsumingInput()) {
            processMouseInput(-1, false, 0);
            
            if (renderer.getWindowFocused() != null) {
                if (renderer.getWindowFocused().isModal()) {
                    event.consume();
                }
            }
        }
    }
    
    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onMouseY(MouseYAxisEvent event, EntityRef entity) {
        if (isConsumingInput()) {
            processMouseInput(-1, false, 0);
            
            if (renderer.getWindowFocused() != null) {
                if (renderer.getWindowFocused().isModal()) {
                    event.consume();
                }
            }
        }
    }
    
    //mouse button events
    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
        if (isConsumingInput()) {
            processMouseInput(event.getButton(), event.getState() != ButtonState.UP, 0);
            
            if (renderer.getWindowFocused() != null) {
                if (renderer.getWindowFocused().isModal()) {
                    event.consume();
                }
            }
        }
    }

    //mouse wheel events
    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
        if (isConsumingInput()) {
            processMouseInput(-1, false, event.getWheelTurns() * 120);
            
            if (renderer.getWindowFocused() != null) {
                if (renderer.getWindowFocused().isModal()) {
                    event.consume();
                }
            }
        }
    }

    //raw input events
    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void keyEvent(KeyEvent event, EntityRef entity) {
        if (isConsumingInput()) {
            processKeyboardInput(event);
        }
    }
    
    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event hasn't consumed the event)
    @ReceiveEvent(components = LocalPlayerComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void bindEvent(BindButtonEvent event, EntityRef entity) {
        if (isConsumingInput()) {
            processBindButton(event);
            
            if (renderer.getWindowFocused() != null) {
                //if modal, consume the event so it wont get caught from others
                if (renderer.getWindowFocused().isModal()) {
                    event.consume();
                }
            }
        }
    }
}
