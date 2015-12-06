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
package org.terasology.engine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.MethodCommand;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.network.NetworkMode;
import org.terasology.registry.InjectionHelper;

import java.util.List;
import java.util.Map;

/**
 * Simple manager for component systems.
 * The manager takes care of registering systems with the Core Registry (if they are marked as shared), initialising them
 * and unloading them.
 * The ComponentSystemManager has two states:
 * <ul>
 * <li>Inactive: In this state the registered systems are created, but not initialised</li>
 * <li>Active: In this state all the registered systems are initialised</li>
 * </ul>
 * It starts inactive and becomes active when initialise() is called.
 *
 * After a call of shutdown it should not be used anymore.
 *
 */
public class ComponentSystemManager {

    private static final Logger logger = LoggerFactory.getLogger(ComponentSystemManager.class);

    private Map<String, ComponentSystem> namedLookup = Maps.newHashMap();
    private List<UpdateSubscriberSystem> updateSubscribers = Lists.newArrayList();
    private List<RenderSystem> renderSubscribers = Lists.newArrayList();
    private List<ComponentSystem> store = Lists.newArrayList();

    private Console console;
    private Context context;

    private boolean initialised;

    public ComponentSystemManager(Context context) {
        this.context = context;
    }

    public void loadSystems(ModuleEnvironment environment, NetworkMode netMode) {
        DisplayDevice display = context.get(DisplayDevice.class);
        boolean isHeadless = display.isHeadless();

        ListMultimap<Name, Class<?>> systemsByModule = ArrayListMultimap.create();
        for (Class<?> type : environment.getTypesAnnotatedWith(RegisterSystem.class)) {
            if (!ComponentSystem.class.isAssignableFrom(type)) {
                logger.error("Cannot load {}, must be a subclass of ComponentSystem", type.getSimpleName());
                continue;
            }
            Name moduleId = environment.getModuleProviding(type);
            RegisterSystem registerInfo = type.getAnnotation(RegisterSystem.class);
            if (registerInfo.value().isValidFor(netMode, isHeadless)) {
                systemsByModule.put(moduleId, type);
            }
        }

        for (Module module : environment.getModulesOrderedByDependencies()) {
            for (Class<?> system : systemsByModule.get(module.getId())) {
                String id = module.getId() + ":" + system.getSimpleName();
                logger.debug("Registering system {}", id);
                try {
                    ComponentSystem newSystem = (ComponentSystem) system.newInstance();
                    InjectionHelper.share(newSystem);
                    register(newSystem, id);
                    logger.debug("Loaded system {}", id);
                } catch (RuntimeException | IllegalAccessException | InstantiationException
                        | NoClassDefFoundError e) {
                    logger.error("Failed to load system {}", id, e);
                }
            }
        }
    }

    public void register(ComponentSystem object) {
        store.add(object);
        if (object instanceof UpdateSubscriberSystem) {
            updateSubscribers.add((UpdateSubscriberSystem) object);
        }
        if (object instanceof RenderSystem) {
            renderSubscribers.add((RenderSystem) object);
        }
        context.get(EntityManager.class).getEventSystem().registerEventHandler(object);

        if (initialised) {
            initialiseSystem(object);
        }
    }

    public void register(ComponentSystem object, String name) {
        namedLookup.put(name, object);
        register(object);
    }

    public void initialise() {
        if (!initialised) {
            console = context.get(Console.class);
            for (ComponentSystem system : iterateAll()) {
                initialiseSystem(system);
            }
            initialised = true;
        } else {
            logger.error("ComponentSystemManager got initialized twice");
        }
    }

    private void initialiseSystem(ComponentSystem system) {
        InjectionHelper.inject(system);

        if (console != null) {
            MethodCommand.registerAvailable(system, console, context);
        }

        try {
            system.initialise();
        } catch (RuntimeException e) {
            logger.error("Failed to initialise system {}", system, e);
        }
    }

    public boolean isActive() {
        return initialised;
    }

    public ComponentSystem get(String name) {
        return namedLookup.get(name);
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
    }
}
