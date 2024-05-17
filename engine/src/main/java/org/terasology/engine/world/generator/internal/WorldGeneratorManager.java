// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.exceptions.UnresolvedDependencyException;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.naming.Name;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Verify.verifyNotNull;

public class WorldGeneratorManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldGeneratorManager.class);

    private final Context context;

    private ImmutableList<WorldGeneratorInfo> generatorInfo;

    public WorldGeneratorManager(Context context) {
        this.context = context;
        refresh();
    }

    public void refresh() {
        ModuleManager moduleManager = verifyNotNull(context.get(ModuleManager.class), "no ModuleManager");
        List<WorldGeneratorInfo> infos = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);

            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult resolutionResult = resolver.resolve(module.getId());
            if (resolutionResult.isSuccess()) {
                try (ModuleEnvironment tempEnvironment = moduleManager.loadEnvironment(resolutionResult.getModules(), false)) {
                    for (Class<?> generatorClass : tempEnvironment.getTypesAnnotatedWith(RegisterWorldGenerator.class)) {
                        Name providedBy = tempEnvironment.getModuleProviding(generatorClass);
                        if (providedBy == null) {  // These tend to be engine-module-is-weird cases.
                            String s = "{} found while inspecting {} but is not provided by any module.";
                            if (!ModuleManager.isLoadingClasspathModules()) {
                                logger.warn(s, generatorClass, moduleId);  // Deserves WARNING level in production.
                            } else {
                                // …but happens a *lot* when loading modules from classpath, such as MTE.
                                logger.debug(s, generatorClass, moduleId);
                            }
                        } else if (providedBy.equals(module.getId())) {
                            RegisterWorldGenerator annotation = generatorClass.getAnnotation(RegisterWorldGenerator.class);
                            if (isValidWorldGenerator(generatorClass)) {
                                SimpleUri uri = new SimpleUri(moduleId, annotation.id());
                                infos.add(new WorldGeneratorInfo(uri, annotation.displayName(), annotation.description()));
                                logger.debug("{} added from {}", uri, generatorClass);
                            } else {
                                logger.error("{} marked to be registered as a World Generator, "
                                        + "but is not a subclass of WorldGenerator or lacks the correct constructor", generatorClass);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error loading world generator in module {}, skipping", module.getId(), e); //NOPMD
                }
            } else {
                logger.warn("Could not resolve dependencies for module: {}", module);
            }
        }
        Collections.sort(infos);
        generatorInfo = ImmutableList.copyOf(infos);
    }

    public List<WorldGeneratorInfo> getWorldGenerators() {
        return generatorInfo;
    }

    public WorldGeneratorInfo getWorldGeneratorInfo(SimpleUri uri) {
        for (WorldGeneratorInfo info : generatorInfo) {
            if (info.getUri().equals(uri)) {
                return info;
            }
        }
        return null;
    }

    /**
     * @param uri uri of the world generator to create.
     * @param context objects from this context will be injected into the
     * @return The instantiated world generator.
     */
    public static WorldGenerator createGenerator(SimpleUri uri, Context context)
            throws UnresolvedWorldGeneratorException, UnresolvedDependencyException {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        Module module = moduleManager.getEnvironment().get(uri.getModuleName());
        if (module == null) {
            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult result = resolver.resolve(uri.getModuleName());
            if (!result.isSuccess()) {
                if (moduleManager.getRegistry().getLatestModuleVersion(uri.getModuleName()) == null) {
                    throw new UnresolvedWorldGeneratorException("Unable to resolve world generator '"
                            + uri + "' - not found");
                } else {
                    throw new UnresolvedWorldGeneratorException("Unable to resolve world generator '"
                            + uri + "' - unable to resolve module dependencies");
                }
            }
            try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                return createWorldGenerator(uri, context, environment);
            }
        } else {
            return createWorldGenerator(uri, context, moduleManager.getEnvironment());
        }
    }

    /**
     * @param uri uri of the world generator to create.
     * @param context that will be used to inject teh world generator.
     * @param environment to be searched for the world generator class.
     * @return a new world generator with the specified uri.
     */
    public static WorldGenerator createWorldGenerator(SimpleUri uri, Context context, ModuleEnvironment environment)
            throws UnresolvedWorldGeneratorException, UnresolvedDependencyException {
        for (Class<?> generatorClass : environment.getTypesAnnotatedWith(RegisterWorldGenerator.class)) {
            RegisterWorldGenerator annotation = generatorClass.getAnnotation(RegisterWorldGenerator.class);
            Name moduleName = environment.getModuleProviding(generatorClass);
            if (moduleName == null) {
                throw new UnresolvedDependencyException("Cannot find module for world generator " + generatorClass);
            }
            SimpleUri generatorUri = new SimpleUri(moduleName, annotation.id());
            if (generatorUri.equals(uri)) {
                WorldGenerator worldGenerator = loadGenerator(generatorClass, generatorUri);
                InjectionHelper.inject(worldGenerator, context);
                return worldGenerator;
            }
        }
        throw new UnresolvedWorldGeneratorException("Unable to resolve world generator '" + uri + "' - not found");
    }

    private static WorldGenerator loadGenerator(Class<?> generatorClass, SimpleUri uri) throws UnresolvedWorldGeneratorException {
        if (isValidWorldGenerator(generatorClass)) {
            try {
                return (WorldGenerator) generatorClass.getConstructor(SimpleUri.class).newInstance(uri);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new UnresolvedWorldGeneratorException("Failed to instantiate world generator '" + uri + "'", e);
            }
        } else {
            throw new UnresolvedWorldGeneratorException(uri + " is not a valid world generator");
        }
    }

    private static boolean isValidWorldGenerator(Class<?> generatorClass) {
        try {
            return WorldGenerator.class.isAssignableFrom(generatorClass) && generatorClass.getConstructor(SimpleUri.class) != null;
            // Being generous in catching here, because if the module is broken due to code changes or missing classes
            // the world generator is invalid
        } catch (NoSuchMethodException | RuntimeException e) {
            return false;
        }
    }

}
