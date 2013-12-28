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
package org.terasology.rendering.logic.manager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.rendering.logic.event.HUDElementEvent;
import org.terasology.rendering.logic.event.HUDElementInitializedEvent;
import org.terasology.rendering.logic.event.HUDElementShutdownEvent;
import org.terasology.rendering.logic.event.UnresolvedHUDElementEventException;
import org.terasology.world.WorldComponent;

import com.google.common.collect.Maps;

/**
 * The HUDElement manager handles all HUDElements within the UI.
 *
 * @author Mike Kienenberger <mkienenb@gmail.com>
 */

public class HUDElementManager implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(HUDElementManager.class);
    private Map<String, Class<? extends HUDElement>> registeredHUDElements = Maps.newHashMap();
    private HUD hud;
    
    
    // TODO: unlike GUIManager, should this be receiving events to maintain hud elements?

    public HUDElementManager(HUD hud) {
    	this.hud = hud;
    }

    /**
     * Add a HUDElement to the HUD.
     *
     * @param window The window to add.
     * @return Returns the added window.
     */
    private HUDElement addHUDElement(HUDElement hudElement) {
        if (hudElement != null) {
            logger.debug("Added HUDElement with ID \"{}\"", hudElement.getId());

            hud.addHUDElement(hudElement);
            hudElement.initialise();
            sendEventToWorldEntities(HUDElementInitializedEvent.class, hudElement);
        }

        return hudElement;
    }

	private void sendEventToWorldEntities(Class<? extends HUDElementEvent> eventClass, HUDElement hudElement) {
		EntityManager entityManager = CoreRegistry.get(EntityManager.class);
		if (entityManager != null) {
			// TODO: is the worldEntity the correct place to be sending events from?  I'm unclear on this and just copied GUIManager.
		    for (EntityRef worldEntity : CoreRegistry.get(EntityManager.class).getEntitiesWith(WorldComponent.class)) {
		    	try {
					HUDElementEvent event = createHUDElementEventInstance(eventClass, hudElement);
					worldEntity.send(event);
				} catch (UnresolvedHUDElementEventException e) {
					// TODO: maybe should handle this differently?
		            logger.error("Unable to load HUDElementEvent", e);
				}
		    }
		}
	}

    private HUDElementEvent createHUDElementEventInstance(
			Class<? extends HUDElementEvent> eventClass, HUDElement hudElement)
					throws UnresolvedHUDElementEventException {
        try {
            return (HUDElementEvent) eventClass.getConstructor(HUDElement.class).newInstance(hudElement);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new UnresolvedHUDElementEventException("Failed to instantiate HUDElementEvent '" + hudElement + "'", e);
        }
	}

	private void removeHUDElement(HUDElement hudElement) {
        if (hudElement == null) {
            logger.warn("Can't remove null HUDElement");
        } else {
            logger.debug("Removed HUDElement by reference with ID \"{}\"");

            hudElement.willShutdown();
            hud.removeHUDElement(hudElement);
            hudElement.shutdown();
            
            // TODO: not sure if this is needed, or if we should be letting the HUDElement send it.
            sendEventToWorldEntities(HUDElementShutdownEvent.class, hudElement);
        }
    }

    private void removeAllHUDElements() {
        logger.debug("Removed all windows");

        List<HUDElement> hudElementListCopy = new ArrayList<HUDElement>(hud.getHUDElements());
        for (HUDElement hudElement : hudElementListCopy) {
        	removeHUDElement(hudElement);
        }
    }

    /**
     * Close the given window by reference and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     *
     * @param window The window by reference to remove.
     */
    public void closeHUDElement(HUDElement hudElement) {
        if (hudElement == null) {
            logger.warn("Can't close null hudElement");
        } else {
            logger.debug("Closed HUDElement by reference with ID \"{}\"", hudElement.getId());

            removeHUDElement(hudElement);
        }
    }

    /**
     * Close the window by ID and remove it from the GUIManager. Therefore it won't be updated or rendered anymore.
     *
     * @param windowId The window by ID to remove.
     */
    public void closeHUDElement(String hudElementId) {
        logger.debug("Close hudElement by ID \"{}\"", hudElementId);

        closeHUDElement(getHUDElementById(hudElementId));
    }

    /**
     * Close all windows and remove them from the GUIManager. Therefore they won't be updated or rendered anymore.
     */
    public void closeAllHUDElements() {
        logger.debug("HUDElementManager: Closed all hudElements");

        removeAllHUDElements();
    }

    /**
     * Open and focus a window by reference.
     *
     * @param window The window to open and focus.
     * @return Returns the reference of the window which was opened and focused.
     */
    public HUDElement openHUDElement(HUDElement hudElement) {
        if (hudElement == null) {
            logger.warn("Can't open hudElement: null");
        } else {
            if (!hud.getHUDElements().contains(hudElement)) {
                addHUDElement(hudElement);
            }

            logger.debug("Open and focus hudElement by reference with ID \"{}\"", hudElement.getId());
        }

        return hudElement;
    }

    /**
     * Open and focus a window by ID. If the window isn't loaded, it will try to load the window.
     *
     * @param windowId The ID of the window to open and focus.
     * @return Returns the reference of the window which was opened and focused. If a window can't be loaded a null reference will be returned.
     */
    public HUDElement openHUDElement(String hudElementId) {
        logger.debug("Open and foucs window by ID \"{}\"", hudElementId);

        HUDElement hudElement = getHUDElementById(hudElementId);

        if (hudElement == null) {
        	hudElement = loadHUDElement(hudElementId);

            if (hudElement != null) {
            	hudElement.open();
            }
        } else {
        	hudElement.open();
        }

        return hudElement;
    }

    public void registerHUDElement(String windowId, Class<? extends HUDElement> windowClass) {
        registeredHUDElements.put(windowId, windowClass);
    }

    /**
     * Load a window by ID and add it to the UI.
     *
     * @param windowId The id of the window to load.
     * @return Returns the reference of the loaded window or null if the window couldn't be loaded.
     */
    public HUDElement loadHUDElement(String windowId) {
        HUDElement window = getHUDElementById(windowId);

        if (window != null) {
            logger.warn("HUDElement with ID \"{}\" already loaded.", windowId);
            return window;
        }

        Class<? extends HUDElement> windowClass = registeredHUDElements.get(windowId);
        if (windowClass != null) {
            logger.debug("Loading window with ID \"{}\".", windowId);

            try {
                return addHUDElement(windowClass.newInstance());
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
     *
     * @param windowId The window id.
     * @return Returns the reference of the window with the given id or null if there is none with this id.
     */
    public HUDElement getHUDElementById(String windowId) {
        for (HUDElement hudElement : hud.getHUDElements()) {
            if (hudElement.getId().equals(windowId)) {
                return hudElement;
            }
        }

        return null;
    }

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    public void toggleHUDElement(String id) {
        if (getHUDElementById(id) != null) {
            closeHUDElement(id);
        } else {
            openHUDElement(id);
        }
    }
}