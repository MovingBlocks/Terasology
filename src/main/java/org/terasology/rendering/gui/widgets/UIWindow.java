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
import org.terasology.rendering.gui.animation.AnimateOpacity;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.events.BindKeyListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.WindowListener;

import java.util.ArrayList;

/**
 * A window which can contain display elements. All windows will be managed by the GUIManager.
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 * TODO closeBinds/closeKeys needs to be handled in another way.. (its not really a close -> setVisible(false))
 */
public class UIWindow extends UIDisplayContainerScrollable {

    //events
    private static enum EWindowEvent {OPEN, CLOSE};
    private final ArrayList<WindowListener> windowListeners = new ArrayList<WindowListener>();
    
    //close buttons
    private String[] closeBinds;
    private int[] closeKeys;
    
    //layout
    private boolean modal = false;
    
    public UIWindow() {
        addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                setFocus(UIWindow.this);
            }
        });
        
        addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                if (closeKeys != null) {
                    for (int key : closeKeys) {
                        if (key == event.getKey() && event.isDown()) {
                            setVisible(false);
                            event.consume();
                            
                            return;
                        }
                    }
                }
            }
        });
        
        addBindKeyListener(new BindKeyListener() {
            @Override
            public void key(UIDisplayElement element, BindButtonEvent event) {
                if (closeBinds != null) {
                    for (String key : closeBinds) {
                        if (key.equals(event.getId()) && event.isDown()) {
                            setVisible(false);
                            event.consume();
                            
                            return;
                        }
                    }
                }
            }
        });
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
    public void setVisible(boolean visible) {
        if (!visible && isVisible()) {
            setFocus(null);
        }
        
        super.setVisible(visible);
        
        GUIManager.getInstance().checkMouseGrabbing();
    }
    
    /**
     * Opens the window. This will focus the window.
     */
    public void open() {
        if (!isVisible()) {
            notifyWindowListeners(EWindowEvent.OPEN);
            setFocus(null);
        }
        setVisible(true);
        setAnimation(new AnimateOpacity(0f, 1f, 5f));
        getAnimation(AnimateOpacity.class).start();
        GUIManager.getInstance().checkMouseGrabbing();
    }
    
    /**
     * Closes the window. This will remove the window from the GUIManager.
     */
    public void close() {
        setFocus(null);
        notifyWindowListeners(EWindowEvent.CLOSE);
        GUIManager.getInstance().closeWindow(this);
        GUIManager.getInstance().checkMouseGrabbing();
    }
}
