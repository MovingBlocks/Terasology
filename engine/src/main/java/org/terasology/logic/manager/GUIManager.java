/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.gui.widgets.UIWindow;

public interface GUIManager extends ComponentSystem {

    /**
     * Render all visible display elements an their child's.
     */
    public void render();

    /**
     * Updates all visible display elements and their child's. Will update the layout if the display was resized.
     */
    public void update();

    /**
     * Updates all visible display elements and their child's. Will update the layout if force is set to true.
     */
    public void update(boolean force);

    /**
     * Close the given window by reference and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     *
     * @param window The window by reference to remove.
     */
    public void closeWindow(UIWindow window);

    /**
     * Close the window by ID and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     *
     * @param windowId The window by ID to remove.
     */
    public void closeWindow(String windowId);

    /**
     * Close all windows and remove them from the GUIManager. Therefore they won't be updated or rendered anymore.
     */
    public void closeAllWindows();

    /**
     * Open and focus a window by reference.
     *
     * @param window The window to open and focus.
     * @return Returns the reference of the window which was opened and focused.
     */
    public UIWindow openWindow(UIWindow window);

    /**
     * Open and focus a window by ID. If the window isn't loaded, it will try to load the window.
     *
     * @param windowId The ID of the window to open and focus.
     * @return Returns the reference of the window which was opened and focused. If a window can't be loaded a null reference will be returned.
     */
    public UIWindow openWindow(String windowId);

    public void registerWindow(String windowId, Class<? extends UIWindow> windowClass);

    /**
     * Load a window by ID and add it to the UI.
     *
     * @param windowId The id of the window to load.
     * @return Returns the reference of the loaded window or null if the window couldn't be loaded.
     */
    public UIWindow loadWindow(String windowId);

    /**
     * Get a window reference, which was added to the GUIManager by id.
     *
     * @param windowId The window id.
     * @return Returns the reference of the window with the given id or null if there is none with this id.
     */
    public UIWindow getWindowById(String windowId);

    /**
     * Get the focused window.
     *
     * @return Returns the focused window.
     */
    public UIWindow getFocusedWindow();

    /**
     * Check whether the mouse of the current focused window is visible and can be moved on the display.
     */
    public boolean isReleasingMouse();

    /**
     * Check if the GUI will consume the input events. Input events are changes of the mouse position, mouse button, mouse wheel and keyboard input.
     *
     * @return Returns true if the GUI will consume the input events.
     */
    public boolean isConsumingInput();

    /**
     * Show a message dialog.
     *
     * @param title The title of the dialog.
     * @param text  The text of the dialog.
     */
    public void showMessage(String title, String text);

    public void initialise();

    public void shutdown();

    //mouse movement events
    public void onMouseX(MouseXAxisEvent event, EntityRef entity);

    public void onMouseY(MouseYAxisEvent event, EntityRef entity);

    //mouse button events
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity);

    //mouse wheel events
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity);

    //raw input events
    public void keyEvent(KeyEvent event, EntityRef entity);

    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event hasn't consumed the event)
    public void bindEvent(BindButtonEvent event, EntityRef entity);

    public void toggleWindow(String id);

}