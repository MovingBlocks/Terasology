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

import java.util.Map;

import com.google.common.collect.Maps;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.TerasologyEngine;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.ButtonState;
import org.terasology.rendering.gui.events.UIWindowOpenedEvent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayRenderer;
import org.terasology.rendering.gui.widgets.UIMessageBox;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.gui.windows.UIMenuConfig;
import org.terasology.rendering.gui.windows.UIMenuConfigAdvanced;
import org.terasology.rendering.gui.windows.UIMenuConfigAudio;
import org.terasology.rendering.gui.windows.UIMenuConfigControls;
import org.terasology.rendering.gui.windows.UIMenuConfigVideo;
import org.terasology.rendering.gui.windows.UIMenuMain;
import org.terasology.rendering.gui.windows.UIMenuMultiplayer;
import org.terasology.rendering.gui.windows.UIMenuPause;
import org.terasology.rendering.gui.windows.UIMenuSingleplayer;
import org.terasology.rendering.gui.windows.UIScreenItems;
import org.terasology.rendering.gui.windows.UIScreenChat;
import org.terasology.rendering.gui.windows.UIScreenContainer;
import org.terasology.rendering.gui.windows.UIScreenDeath;
import org.terasology.rendering.gui.windows.UIScreenHUD;
import org.terasology.rendering.gui.windows.UIScreenInventory;
import org.terasology.rendering.gui.windows.UIScreenLoading;
import org.terasology.rendering.gui.windows.UIScreenMetrics;

import javax.vecmath.Vector2f;

/**
 * The GUI manager handles all windows within the UI.
 *
 * @author Kireev Anton <adeon.k87@gmail.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */

public class GUIManager implements EventHandlerSystem {

    private static final Logger logger = LoggerFactory.getLogger(GUIManager.class);
    private UIDisplayRenderer renderer;
    private Map<String, Class<? extends UIWindow>> registeredWindows = Maps.newHashMap();

    public GUIManager() {
        renderer = new UIDisplayRenderer();
        renderer.setVisible(true);

        // TODO: Remove this
        registerWindows();
    }

    private void registerWindows() {
        //TODO parser action here! this is temporary
        registeredWindows.put("main", UIMenuMain.class);
        registeredWindows.put("singleplayer", UIMenuSingleplayer.class);
        registeredWindows.put("multiplayer", UIMenuMultiplayer.class);
        registeredWindows.put("config", UIMenuConfig.class);
        registeredWindows.put("config:video", UIMenuConfigVideo.class);
        registeredWindows.put("config:audio", UIMenuConfigAudio.class);
        registeredWindows.put("config:controls", UIMenuConfigControls.class);
        registeredWindows.put("config:advanced", UIMenuConfigAdvanced.class);
        registeredWindows.put("loading", UIScreenLoading.class);
        registeredWindows.put("container", UIScreenContainer.class);
        registeredWindows.put("metrics", UIScreenMetrics.class);
        registeredWindows.put("death", UIScreenDeath.class);
        registeredWindows.put("pause", UIMenuPause.class);
        registeredWindows.put("inventory", UIScreenInventory.class);
        registeredWindows.put("chat", UIScreenChat.class);
        registeredWindows.put("hud", UIScreenHUD.class);
        registeredWindows.put("itemList", UIScreenItems.class);
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
            renderer.setSize(new Vector2f(Display.getWidth(), Display.getHeight()));
            renderer.layout();
        }
    }

    /**
     * Updates all visible display elements and their child's. Will update the layout if force is set to true.
     */
    public void update(boolean force) {
        renderer.update();

        if (Display.wasResized() || force) {
            renderer.setSize(new Vector2f(Display.getWidth(), Display.getHeight()));
            renderer.layout();
        }
    }

    /**
     * Add an window to the UI. Therefore it can be rendered and updated.
     * @param window The window to add.
     * @return Returns the added window.
     */
    private UIWindow addWindow(UIWindow window) {
        if (window != null) {
            logger.debug("Added window with ID \"{}\"", window.getId());
            
            renderer.addDisplayElementToPosition(0, window);
            window.initialise();
            for (EntityRef worldEntity : CoreRegistry.get(EntityManager.class).iteratorEntities(WorldComponent.class)) {
                worldEntity.send(new UIWindowOpenedEvent(window));
            }
        }

        return window;
    }
    
    private void removeWindow(UIWindow window) {
        if (window == null) {
            logger.warn("Can't remove null window");
        } else {
            logger.debug("Removed window by reference with ID \"{}\"");
            
            renderer.removeDisplayElement(window);
            window.shutdown();
        }
    }
    
    private void removeAllWindows() {
        logger.debug("Removed all windows");
        
        for (UIDisplayElement window : renderer.getDisplayElements()) {
            if (window instanceof UIWindow) {
                ((UIWindow)window).shutdown();
            }
        }
        
        renderer.removeAllDisplayElements();
    }

    /**
     * Close the given window by reference and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     * @param window The window by reference to remove.
     */
    public void closeWindow(UIWindow window) {
        if (window == null) {
            logger.warn("Can't close null window");
        } else {
            logger.debug("Closed window by reference with ID \"{}\"", window.getId());
            
            removeWindow(window);
        }
    }
    
    /**
     * Close the window by ID and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     * @param windowId The window by ID to remove.
     */
    public void closeWindow(String windowId) {
        logger.debug("Closde window by ID \"{}\"", windowId);
        
        closeWindow(getWindowById(windowId));
    }

    /**
     * Close all windows and remove them from the GUIManager. Therefore they won't be updated or rendered anymore.
     */
    public void closeAllWindows() {
        logger.debug("GUIManager: Closed all windows");
        
        removeAllWindows();
    }

    /**
     * Open and focus a window by reference.
     * @param window The window to open and focus.
     * @return Returns the reference of the window which was opened and focused.
     */
    public UIWindow openWindow(UIWindow window) {
        if (window == null) {
            logger.warn("Can't open window: null");
        } else {
            if (!renderer.getDisplayElements().contains(window)) {
                addWindow(window);
            }
            
            logger.debug("Open and focus window by reference with ID \"{}\"");
            
            renderer.setWindowFocus(window);
        }
        
        return window;
    }
    
    /**
     * Open and focus a window by ID. If the window isn't loaded, it will try to load the window.
     * @param windowId The ID of the window to open and focus.
     * @return Returns the reference of the window which was opened and focused. If a window can't be loaded a null reference will be returned.
     */
    public UIWindow openWindow(String windowId) {
        logger.debug("Open and foucs window by ID \"{}\"", windowId);
        
        UIWindow window = getWindowById(windowId);
        
        if (window == null) {
            window = loadWindow(windowId);
            
            if (window != null) {
                window.open();
            }
        } else {
            window.open();
        }
        
        return window;
    }

    public void registerWindow(String windowId, Class<? extends UIWindow> windowClass) {
        registeredWindows.put(windowId, windowClass);
    }
    
    /**
     * Load a window by ID and add it to the UI.
     * @param windowId The id of the window to load.
     * @return Returns the reference of the loaded window or null if the window couldn't be loaded.
     */
    public UIWindow loadWindow(String windowId) {
        UIWindow window = getWindowById(windowId);
        
        if (window != null) {
            logger.warn("Window with ID \"{}\" already loaded.", windowId);
            return window;
        }

        Class<? extends UIWindow> windowClass = registeredWindows.get(windowId);
        if (windowClass != null) {
            logger.debug("Loading window with ID \"{}\".", windowId);

            try {
                return addWindow(windowClass.newInstance());
            } catch (InstantiationException e) {
                logger.error("Failed to load window {}, no default constructor", windowId);
            } catch (IllegalAccessException e) {
                logger.error("Failed to load window {}, no default constructor", windowId);
            }
        }
        logger.warn("Unable to load window \"{}\", unknown id", windowId);
        return null;
    }

    /**
     * Get a window reference, which was added to the GUIManager by id.
     * @param windowId The window id.
     * @return Returns the reference of the window with the given id or null if there is none with this id.
     */
    public UIWindow getWindowById(String windowId) {        
        for (UIDisplayElement window : renderer.getDisplayElements()) {
            if (window.getId().equals(windowId)) {
                return (UIWindow) window;
            }
        }
        
        return null;
    }

    /**
     * Get the focused window.
     * @return Returns the focused window.
     */
	public UIWindow getFocusedWindow() {
		return renderer.getWindowFocused();
	}

	/**
	 * Check whether the mouse of the current focused window is visible and can be moved on the display.
	 */
    public void checkMouseGrabbing() {
        if (isConsumingInput() || renderer.getWindowFocused() == null || TerasologyEngine.isEditorInFocus()) {
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
        UIWindow messageWindow = new UIMessageBox(title, text);
        messageWindow.open();
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
        	renderer.getWindowFocused().processMouseInput(button, state, wheelMoved, false, false);
        }
    }
    
    /**
     * Process the raw keyboard input.
     * @param event The event of the pressed key.
     */
    private void processKeyboardInput(KeyEvent event) {
        if (renderer.getWindowFocused() != null && renderer.getWindowFocused().isModal() && renderer.getWindowFocused().isVisible()) {
        	renderer.getWindowFocused().processKeyboardInput(event);
        }
    }

    /**
     * Process the bind buttons input.
     * @param event The event of the bind button.
     */
    private void processBindButton(BindButtonEvent event) {
        if (renderer.getWindowFocused() != null && renderer.getWindowFocused().isModal() && renderer.getWindowFocused().isVisible()) {
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
