// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generator.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;

import java.lang.reflect.InvocationTargetException;

public class WorldGeneratorPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(WorldGeneratorPluginManager.class);

    private final Context context;

    public WorldGeneratorPluginManager(Context context) {
        this.context = context;
    }

    /**
     * @param urn urn of the world generator to create.
     * @param context objects from this context will be injected into the world generator plugin
     * @return The instantiated world generator plugin.
     */
    public static WorldGeneratorPlugin createGeneratorPlugin(ResourceUrn urn, Context context) throws UnresolvedWorldGeneratorPluginException {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        Module module = moduleManager.getEnvironment().get(urn.getModuleName());
        if (module == null) {
            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult result = resolver.resolve(urn.getModuleName());
            if (!result.isSuccess()) {
                if (moduleManager.getRegistry().getLatestModuleVersion(urn.getModuleName()) == null) {
                    throw new UnresolvedWorldGeneratorPluginException("Unable to resolve world generator plugin'"
                            + urn + "' - not found");
                } else {
                    throw new UnresolvedWorldGeneratorPluginException("Unable to resolve world generator plugin'"
                            + urn + "' - unable to resolve module dependencies");
                }
            }
            try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                return createWorldGeneratorPlugin(urn, context, environment);
            }
        } else {
            return createWorldGeneratorPlugin(urn, context, moduleManager.getEnvironment());
        }
    }

    /**
     * @param urn urn of the world generator plugin to create.
     * @param context that will be used to inject the world generator plugin.
     * @param environment to be searched for the world generator plugin class.
     * @return a new world generator plugin with the specified urn.
     */
    public static WorldGeneratorPlugin createWorldGeneratorPlugin(ResourceUrn urn, Context context, ModuleEnvironment environment)
            throws UnresolvedWorldGeneratorPluginException {
        for (Class<?> entry : environment.getTypesAnnotatedWith(RegisterPlugin.class)) {
            ResourceUrn generatorUrn = new ResourceUrn(environment.getModuleProviding(entry).toString(), entry.getSimpleName());
            if (generatorUrn.equals(urn)) {
                WorldGeneratorPlugin worldGeneratorPlugin = loadGeneratorPlugin(entry, generatorUrn);
                InjectionHelper.inject(worldGeneratorPlugin, context);
                return worldGeneratorPlugin;
            }
        }
        throw new UnresolvedWorldGeneratorPluginException("Unable to resolve world generator '" + urn + "' - not found");
    }

    private static WorldGeneratorPlugin loadGeneratorPlugin(Class<?> generatorClass, ResourceUrn urn) throws UnresolvedWorldGeneratorPluginException {
        if (WorldGeneratorPlugin.class.isAssignableFrom(generatorClass)) {
            try {
                return (WorldGeneratorPlugin) generatorClass.getConstructor(ResourceUrn.class).newInstance(urn);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new UnresolvedWorldGeneratorPluginException("Failed to instantiate world generator plugin '" + urn + "'", e);
            }
        } else {
            throw new UnresolvedWorldGeneratorPluginException(urn + " is not a valid world generator plugin");
        }
    }
}
