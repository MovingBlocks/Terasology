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
package org.terasology.rendering.gui.widgets;

import org.terasology.input.events.KeyEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.IInputDataElement;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.events.WindowListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A window which can contain display elements. All windows will be managed by the GUIManager.
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 */
public class UIWindow extends UIDisplayContainerScrollable {

    //events
    private enum eWindowEvent {OPEN, CLOSE};
    private final ArrayList<WindowListener> _windowListeners = new ArrayList<WindowListener>();
    
    private final HashMap<String, UIDisplayElement> _displayElementsById = new HashMap<String, UIDisplayElement>();
    
    //close buttons
    private String[] closeBinds;
    private int[] closeKeys;
    
    //layout
    private boolean modal = false;
    
    public UIWindow() {
        
    }

    public void clearInputControls() {
        for (UIDisplayElement element : getDisplayElements()) {
            if (IInputDataElement.class.isInstance(element)) {
                IInputDataElement inputControl = (IInputDataElement) element;
                inputControl.clearData();
            }
        }
    }

    private void notifyWindowListeners(eWindowEvent event) {
        //we copy the list so the listener can remove itself within the close/open method call (see UIItemCell). Otherwise ConcurrentModificationException.
        //TODO other solution?
        ArrayList<WindowListener> listeners = (ArrayList<WindowListener>) _windowListeners.clone();
        
        if (event == eWindowEvent.OPEN) {
            for (WindowListener listener : listeners) {
                listener.open(this);
            }
        } else if (event == eWindowEvent.CLOSE) {
            for (WindowListener listener : listeners) {
                listener.close(this);
            }
        }
    }
    
    public void addWindowListener(WindowListener listener) {
        _windowListeners.add(listener);
    }

    public void removeWindowListener(WindowListener listener) {
        _windowListeners.remove(listener);
    }

    public void maximize() {
        setSize("100%", "100%");
    }

    /**
     * Check if the window is modal. A modal window will consume all input events. Input events are mouse move, mouse button, mouse wheel and keyboard input.
     * @return Returns true if the window is modal.
     */
    public boolean isModal() {
        return modal;
    }

    /**
     * Set the windows modality. A modal window will consume all input events. Input events are mouse move, mouse button, mouse wheel and keyboard input.
     * @param modal True for modal.
     */
    public void setModal(boolean modal) {
        this.modal = modal;
    }
    
    /**
     * Set the bind keys which will close the window when pressed.
     * @param binds The bind key ID. For possible bind keys see the {@link org.terasology.input.binds} package.
     */
    public void setCloseBinds(String[] binds) {
        this.closeBinds = binds;
    }
    
    /**
     * Set the keys which will close the window when pressed.
     * @param keys The keys value. For possible keys see {@link org.lwjgl.input.Keyboard}.
     */
    public void setCloseKeys(int[] keys) {
        this.closeKeys = keys;
    }

    /**
     * 
     * @param element
     * @param elementId
     * 
     * @deprecated don't use this. ID will be included in EACH UI element in the future.
     */
    public void addDisplayElement(UIDisplayElement element, String elementId) {
        addDisplayElement(element);
        _displayElementsById.put(elementId, element);
        element.setParent(this);
    }

    /**
     * 
     * @param elementId
     * @return
     * 
     * @deprecated don't use this. ID will be included in EACH UI element in the future.
     */
    public UIDisplayElement getElementById(String elementId) {
        if (!_displayElementsById.containsKey(elementId)) {
            return null;
        }

        return _displayElementsById.get(elementId);
    }
    
    @Override
    public void processKeyboardInput(KeyEvent event) {
        
        if (!isVisible() || !modal)
            return;
        
        if (closeKeys != null) {
            for (int key : closeKeys) {
                if (key == event.getKey() && event.isDown()) {
                    close();
                    event.consume();
                    
                    return;
                }
            }
        }
        
        super.processKeyboardInput(event);
    }
    
    @Override
    public void processBindButton(BindButtonEvent event) {
        
        if (!isVisible() || !modal)
            return;
        
        if (closeBinds != null) {
            for (String key : closeBinds) {
                if (key.equals(event.getId()) && event.isDown()) {
                    close();
                    event.consume();
                    
                    return;
                }
            }
        }
        
        super.processBindButton(event);
    }
    
    /**
     * Set the visibility of the window. Use the open and close methods for windows instead.
     * @param visible True to set the window visible.
     */
    public void setVisible(boolean visible) {        
        if (visible && !isVisible()) {
            notifyWindowListeners(eWindowEvent.OPEN);
            setFocus(null);
            clearInputControls();
        } else if (!visible && isVisible()) {
            notifyWindowListeners(eWindowEvent.CLOSE);
        }
        
        super.setVisible(visible);
        
        if (visible) {
            setFocus(null);
            clearInputControls();
        } else {
            setFocus(this);
        }
        
        GUIManager.getInstance().checkMouseMovement();
    }
    
    /**
     * Opens the window.
     */
    public void open() {
        setVisible(true);
    }
    
    /**
     * Closes the window.
     */
    public void close() {
        setVisible(false);
    }
}
