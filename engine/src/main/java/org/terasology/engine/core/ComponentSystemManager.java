// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.MethodCommand;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;

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
// TODO OpaqueObjectsNode needs this, is there a better way?
@API
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
                logger.error("Cannot load {}, must be a subclass of ComponentSystem", type.getSimpleName()); //NOPMD
                continue;
            }
            Name moduleId = environment.getModuleProviding(type);
            RegisterSystem registerInfo = type.getAnnotation(RegisterSystem.class);
            if (registerInfo.value().isValidFor(netMode.isAuthority(), isHeadless) && areOptionalRequirementsContained(registerInfo, environment)) {
                systemsByModule.put(moduleId, type);
            }
        }

        for (Module module : environment.getModulesOrderedByDependencies()) {
            for (Class<?> system : systemsByModule.get(module.getId())) {
                String id = module.getId() + ":" + system.getSimpleName();
                logger.debug("Registering system {}", id);
                if (checkOptionalDependenciesPresent(system)) {
                    tryToLoadSystem(system, id);
                } else {
                    logger.warn("Skip system {} for loading - possibly missing optional dependencies", id);
                }
            }
        }
    }

    /**
     * Try to load system. try to rollback changes, when fails.
     *
     * @param system system's class for creation and loading.
     * @param id id of system.
     */
    private void tryToLoadSystem(Class<?> system, String id) {
        ComponentSystem newSystem = null;
        try {
            newSystem = (ComponentSystem) system.newInstance();
            InjectionHelper.share(newSystem);
            register(newSystem, id);
            logger.debug("Loaded system {}", id);

        } catch (RuntimeException | InstantiationException | IllegalAccessException e) {
            logger.error("Failed to load system {}", id, e);
            rollbackLoading(newSystem, id);
        }
    }

    /**
     * Rollback changes which made loading before fail.
     * @param system system to rollback.
     * @param id id of system.
     */
    private void rollbackLoading(ComponentSystem system, String id) {
        if (system != null  // system was created, needs cleanup.
                && store.remove(namedLookup.remove(id))// remove system from lookup and store, if already registered.
        ) {
            //TODO unshare if system is shared by InjectionHelper#share()

            // rollback this#register
            if (system instanceof UpdateSubscriberSystem) {
                updateSubscribers.remove((UpdateSubscriberSystem) system);
            }
            if (system instanceof RenderSystem) {
                renderSubscribers.remove((RenderSystem) system);
            }
        }
    }

    /**
     * Check system for meeting optional dependencies.
     * <p/>
     * Hacky-way: check fields and methods for non-throwing {@link NoClassDefFoundError}.
     *
     * @param system system's class to check.
     * @return {@code true} if optional dependencies presents, {@code false} otherwise.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean checkOptionalDependenciesPresent(Class<?> system) {
        try {
            // Check that fields haven't absent classes.
            system.getDeclaredFields();
            // Check that method signatures haven't absent classes.
            system.getDeclaredMethods();
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }

    }

    private boolean areOptionalRequirementsContained(RegisterSystem registerSystem, ModuleEnvironment environment) {
        if (registerSystem.requiresOptional().length == 0) {
            return true;
        }
        for (String moduleName : registerSystem.requiresOptional()) {
            if (environment.get(new Name(moduleName)) == null) {
                return false;
            }
        }
        return true;
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
            logger.atWarn().log("System {} registered post-init.", object.getClass().getName());
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
            for (ComponentSystem system : getAllSystems()) {
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
            logger.debug("Initialising {}", system);
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

    public List<ComponentSystem> getAllSystems() {
        return store;
    }

    public Iterable<UpdateSubscriberSystem> iterateUpdateSubscribers() {
        return updateSubscribers;
    }

    public Iterable<RenderSystem> iterateRenderSubscribers() {
        return renderSubscribers;
    }

    public void shutdown() {
        for (ComponentSystem system : getAllSystems()) {
            system.shutdown();
        }
        updateSubscribers.clear();
        renderSubscribers.clear();
    }
}
