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
    private static enum EWindowEvent {OPEN, CLOSE};
    private final ArrayList<WindowListener> windowListeners = new ArrayList<WindowListener>();
    
    //elements by id
    private final HashMap<String, UIDisplayElement> displayElementsById = new HashMap<String, UIDisplayElement>();
    
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

    private void notifyWindowListeners(EWindowEvent event) {
        //we copy the list so the listener can remove itself within the close/open method call (see UIItemCell). Otherwise ConcurrentModificationException.
        //TODO other solution?
        ArrayList<WindowListener> listeners = (ArrayList<WindowListener>) windowListeners.clone();
        
        if (event == EWindowEvent.OPEN) {
            for (WindowListener listener : listeners) {
                listener.open(this);
            }
        } else if (event == EWindowEvent.CLOSE) {
            for (WindowListener listener : listeners) {
                listener.close(this);
            }
        }
    }
    
    public void addWindowListener(WindowListener listener) {
        windowListeners.add(listener);
    }

    public void removeWindowListener(WindowListener listener) {
        windowListeners.remove(listener);
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
     * Opens the window. This will focus the window.
     */
    public void open() {
        if (!isVisible()) {
            notifyWindowListeners(EWindowEvent.OPEN);
            setFocus(null);
            clearInputControls();
        }
        
        setVisible(true);
        
        GUIManager.getInstance().checkMouseGrabbing();
    }
    
    /**
     * Closes the window. This will remove the window from the GUIManager.
     */
    public void close() {
        setFocus(null);
        clearInputControls();
        
        notifyWindowListeners(EWindowEvent.CLOSE);
        
        GUIManager.getInstance().closeWindow(this);
        GUIManager.getInstance().checkMouseGrabbing();
    }
}
