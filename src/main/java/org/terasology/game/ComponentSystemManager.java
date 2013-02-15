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
package org.terasology.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.Share;
import org.terasology.network.NetworkMode;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple manager for component systems.
 * The manager takes care of registering systems with the Core Registry (if they are marked as shared), initialising them
 * and unloading them.
 * The ComponentSystemManager has two states:
 * <ul>
 *     <li>Inactive: In this state the registered systems are created, but not initialised</li>
 *     <li>Active: In this state all the registered systems are initialised</li>
 * </ul>
 * It becomes active when initialise() is called, and inactive when shutdown() is called.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class ComponentSystemManager {

    private static final Logger logger = LoggerFactory.getLogger(ComponentSystemManager.class);

    private Map<String, ComponentSystem> namedLookup = Maps.newHashMap();
    private List<UpdateSubscriberSystem> updateSubscribers = Lists.newArrayList();
    private List<RenderSystem> renderSubscribers = Lists.newArrayList();
    private List<ComponentSystem> store = Lists.newArrayList();
    private List<Class<?>> sharedSystems = Lists.newArrayList();

    public boolean initialised = false;

    public ComponentSystemManager() {
    }

    public void loadSystems(String packageName, Reflections reflections, NetworkMode netMode) {
        Set<Class<?>> systems = reflections.getTypesAnnotatedWith(RegisterSystem.class);
        for (Class<?> system : systems) {
            if (!ComponentSystem.class.isAssignableFrom(system)) {
                logger.error("Cannot load {}, must be a subclass of ComponentSystem", system.getSimpleName());
                continue;
            }

            RegisterSystem registerInfo = system.getAnnotation(RegisterSystem.class);
            if (shouldRegister(registerInfo, netMode)) {
                String id = packageName + ":" + system.getSimpleName();
                logger.debug("Registering system {}", id);
                try {
                    ComponentSystem newSystem = (ComponentSystem) system.newInstance();
                    Share share = system.getAnnotation(Share.class);
                    if (share != null && share.value() != null) {
                        for (Class<?> interfaceType : share.value()) {
                            sharedSystems.add(interfaceType);
                            CoreRegistry.put((Class<Object>)interfaceType, newSystem);
                        }
                    }
                    register(newSystem, id);
                    logger.debug("Loaded system {}", id);
                } catch (InstantiationException e) {
                    logger.error("Failed to load system {}", id, e);
                } catch (IllegalAccessException e) {
                    logger.error("Failed to load system {}", id, e);
                }
            }
        }

    }

    private boolean shouldRegister(RegisterSystem registerInfo, NetworkMode netMode) {
        switch (registerInfo.value()) {
            case AUTHORITY:
                return netMode.isAuthority();
            case SERVER:
                return netMode == NetworkMode.SERVER;
            case CLIENT:
                return netMode == NetworkMode.CLIENT;
            default:
                return true;
        }
    }

    public <T extends ComponentSystem> void register(ComponentSystem object, String name) {
        store.add(object);
        if (object instanceof UpdateSubscriberSystem) {
            updateSubscribers.add((UpdateSubscriberSystem) object);
        }
        if (object instanceof RenderSystem) {
            renderSubscribers.add((RenderSystem) object);
        }
        CoreRegistry.get(EntityManager.class).getEventSystem().registerEventHandler(object);
        namedLookup.put(name, object);

        if (initialised) {
            initialiseSystem(object);
        }
    }

    public void initialise() {
        if (!initialised) {
            for (ComponentSystem system : iterateAll()) {
                initialiseSystem(system);
            }
            initialised = true;
        }
    }

    private void initialiseSystem(ComponentSystem system) {
        for (Field field : Reflections.getAllFields(system.getClass(), Reflections.withAnnotation(In.class))) {
            Object value = CoreRegistry.get(field.getType());
            if (value != null) {
                try {
                    field.setAccessible(true);
                    field.set(system, value);
                } catch (IllegalAccessException e) {
                    logger.error("Failed to inject value {} into field {} of system {}", value, field, system, e);
                }
            }
        }
        system.initialise();
    }

    public boolean isActive() {
        return initialised;
    }

    public ComponentSystem get(String name) {
        return namedLookup.get(name);
    }

    private void clear() {
        for (Class<?> sharedSystem : sharedSystems) {
            CoreRegistry.remove(sharedSystem);
        }
        sharedSystems.clear();
        namedLookup.clear();
        store.clear();
        updateSubscribers.clear();
        renderSubscribers.clear();
        initialised = false;
    }

    public Iterable<ComponentSystem> iterateAll() {
        return store;
    }

    public Iterable<UpdateSubscriberSystem> iterateUpdateSubscribers() {
        return updateSubscribers;
    }

    public Iterable<RenderSystem> iterateRenderSubscribers() {
        return renderSubscribers;
    }

    public void shutdown() {
        for (ComponentSystem system : iterateAll()) {
            system.shutdown();
        }
        clear();
    }
}
