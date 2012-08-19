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
package org.terasology.rendering.gui.framework;

import org.lwjgl.opengl.Display;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.events.WindowListener;

import javax.vecmath.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class UIDisplayWindow extends UIScrollableDisplayContainer {

    private enum eWindowEvent {OPEN, CLOSE};
    private final ArrayList<WindowListener> _windowListeners = new ArrayList<WindowListener>();
    private final HashMap<String, UIDisplayElement> _displayElementsById = new HashMap<String, UIDisplayElement>();
    private boolean _maximized = false;
    private boolean _modal = false;
    private String[] closeBinds;
    private int[] closeKeys;

    protected void drag(Vector2f value) {
        getPosition().x -= value.x;
        getPosition().y -= value.y;
    }

    /**
     * Close a window. This will just make the window invisible, it will not be removed from the GUIManager.
     * @param clearInputControls
     */
    public void close(boolean clearInputControls) {
        setVisible(false);
        setFocus(null);
        if (clearInputControls) {
            clearInputControls();
        }
    }

    public void show() {
        setVisible(true);
        setFocus(this);
    }

    public void clearInputControls() {
        for (UIDisplayElement element : getDisplayElements()) {
            if (IInputDataElement.class.isInstance(element)) {
                IInputDataElement inputControl = (IInputDataElement) element;
                inputControl.clearData();
            }
        }
    }

    public void render() {
        if (isModal()) {
            renderOverlay();
        }
        super.render();
    }

    public void renderOverlay() {
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
        setSize(new Vector2f(Display.getWidth(), Display.getHeight()));
        _maximized = true;
    }

    public boolean isMaximized() {
        return _maximized;
    }

    /**
     * Check if the window is modal. A modal window will consume all input events. Input events are mouse move, mouse button, mouse wheel and keyboard input.
     * @return Returns true if the window is modal.
     */
    public boolean isModal() {
        return _modal;
    }

    /**
     * Set the windows modality. A modal window will consume all input events. Input events are mouse move, mouse button, mouse wheel and keyboard input.
     * @param modal True for modal.
     */
    public void setModal(boolean modal) {
        _modal = modal;
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

    public void addDisplayElement(UIDisplayElement element, String elementId) {
        addDisplayElement(element);
        _displayElementsById.put(elementId, element);
        element.setParent(this);
    }

    public UIDisplayElement getElementById(String elementId) {
        if (!_displayElementsById.containsKey(elementId)) {
            return null;
        }

        return _displayElementsById.get(elementId);
    }
    
    @Override
    public void processKeyboardInput(KeyEvent event) {
        
        if (!isVisible() || !_modal)
            return;
        
        if (closeKeys != null) {
            for (int key : closeKeys) {
                if (key == event.getKey() && event.isDown()) {
                    close(true);
                    event.consume();
                    
                    return;
                }
            }
        }
        
        super.processKeyboardInput(event);
    }
    
    @Override
    public void processBindButton(BindButtonEvent event) {
        
        if (!isVisible() || !_modal)
            return;
        
        if (closeBinds != null) {
            for (String key : closeBinds) {
                if (key.equals(event.getId()) && event.isDown()) {
                    close(true);
                    event.consume();
                    
                    return;
                }
            }
        }
        
        super.processBindButton(event);
    }
    
    @Override
    public void setVisible(boolean visible) {        
        if (visible && !isVisible()) {
            notifyWindowListeners(eWindowEvent.OPEN);
        } else if (!visible && isVisible()) {
            notifyWindowListeners(eWindowEvent.CLOSE);
        }
        
        super.setVisible(visible);
        
        GUIManager.getInstance().checkMouseMovement();
    }
}
