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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.RegisterComponentSystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.logic.mod.Mod;

/**
 * Simple manager for component systems.
 * This is an initial, rough implementation to be improved later.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class ComponentSystemManager {

    private Logger logger = Logger.getLogger(getClass().getName());

    private Map<String, ComponentSystem> namedLookup = Maps.newHashMap();
    private List<UpdateSubscriberSystem> updateSubscribers = Lists.newArrayList();
    private List<RenderSystem> renderSubscribers = Lists.newArrayList();
    private List<ComponentSystem> store = Lists.newArrayList();

    public ComponentSystemManager() {
    }

    public void loadSystems(String packageName, Reflections reflections) {
        Set<Class<?>> systems = reflections.getTypesAnnotatedWith(RegisterComponentSystem.class);
        for (Class<?> system : systems) {
            if (!ComponentSystem.class.isAssignableFrom(system)) {
                logger.log(Level.WARNING, String.format("Cannot load %s, must be a subclass of ComponentSystem", system.getSimpleName()));
                continue;
            }

            RegisterComponentSystem registerInfo = system.getAnnotation(RegisterComponentSystem.class);
            // TODO: filter registrations
            String id = packageName + ":" + system.getSimpleName();
            try {
                ComponentSystem newSystem = (ComponentSystem) system.newInstance();
                register(newSystem, id);
                logger.log(Level.INFO, "Loaded " + id);
            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Failed to load system " + id, e);
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Failed to load system " + id, e);
            }
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
        if (object instanceof EventHandlerSystem) {
            CoreRegistry.get(EntityManager.class).getEventSystem().registerEventHandler((EventHandlerSystem) object);
        }
        namedLookup.put(name, object);
    }

    // TODO: unregister?

    public ComponentSystem get(String name) {
        return namedLookup.get(name);
    }

    public void clear() {
        namedLookup.clear();
        store.clear();
        updateSubscribers.clear();
        renderSubscribers.clear();
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
}
