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

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.events.WindowListener;

public class UIDisplayWindow extends UIScrollableDisplayContainer {

    private enum eWindowEvent {OPEN, CLOSE};
    private final ArrayList<WindowListener> _windowListeners = new ArrayList<WindowListener>();
    private final HashMap<String, UIDisplayElement> _displayElementsById = new HashMap<String, UIDisplayElement>();
    private boolean _maximized = false;
    private boolean _modal = false;

    protected void drag(Vector2f value) {
        getPosition().x -= value.x;
        getPosition().y -= value.y;
    }

    public void close(boolean clearInputControls) {
        setVisible(false);
        setFocus(null);
        GUIManager.getInstance().setFocusedFromLast();
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

    public boolean isModal() {
        return _modal;
    }

    public void setModal(boolean modal) {
        _modal = modal;
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
    public void setSize(Vector2f scale) {
        super.setSize(scale);
        
        layout();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if (visible) {
            notifyWindowListeners(eWindowEvent.OPEN);
        } else {
            notifyWindowListeners(eWindowEvent.CLOSE);
        }
    }
}
