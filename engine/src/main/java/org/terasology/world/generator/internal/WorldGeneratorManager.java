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
package org.terasology.world.generator.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.registry.InjectionHelper;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 */
public class WorldGeneratorManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldGeneratorManager.class);

    private Context context;

    private ImmutableList<WorldGeneratorInfo> generatorInfo;

    public WorldGeneratorManager(Context context) {
        this.context = context;
        refresh();
    }

    public void refresh() {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        List<WorldGeneratorInfo> infos = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
                ResolutionResult resolutionResult = resolver.resolve(module.getId());
                if (resolutionResult.isSuccess()) {
                    try (ModuleEnvironment tempEnvironment = moduleManager.loadEnvironment(resolutionResult.getModules(), false)) {
                        for (Class<?> generatorClass : tempEnvironment.getTypesAnnotatedWith(RegisterWorldGenerator.class)) {
                            if (tempEnvironment.getModuleProviding(generatorClass).equals(module.getId())) {
                                RegisterWorldGenerator annotation = generatorClass.getAnnotation(RegisterWorldGenerator.class);
                                if (isValidWorldGenerator(generatorClass)) {
                                    SimpleUri uri = new SimpleUri(moduleId, annotation.id());
                                    infos.add(new WorldGeneratorInfo(uri, annotation.displayName(), annotation.description()));
                                } else {
                                    logger.error("{} marked to be registered as a World Generator, but is not a subclass of WorldGenerator or lacks the correct constructor",
                                            generatorClass);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error loading world generator in module {}, skipping", module.getId(), e);
                    }
                } else {
                    logger.warn("Could not resolve dependencies for module: {}", module);
                }
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
    public static WorldGenerator createGenerator(SimpleUri uri, Context context) throws UnresolvedWorldGeneratorException {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        Module module = moduleManager.getEnvironment().get(uri.getModuleName());
        if (module == null) {
            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult result = resolver.resolve(uri.getModuleName());
            if (!result.isSuccess()) {
                if (moduleManager.getRegistry().getLatestModuleVersion(uri.getModuleName()) == null) {
                    throw new UnresolvedWorldGeneratorException("Unable to resolve world generator '" + uri + "' - not found");
                } else {
                    throw new UnresolvedWorldGeneratorException("Unable to resolve world generator '" + uri + "' - unable to resolve module dependencies");
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
    public static WorldGenerator createWorldGenerator(SimpleUri uri, Context context, ModuleEnvironment environment) throws UnresolvedWorldGeneratorException {
        for (Class<?> generatorClass : environment.getTypesAnnotatedWith(RegisterWorldGenerator.class)) {
            RegisterWorldGenerator annotation = generatorClass.getAnnotation(RegisterWorldGenerator.class);
            SimpleUri generatorUri = new SimpleUri(environment.getModuleProviding(generatorClass), annotation.id());
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
            if (WorldGenerator.class.isAssignableFrom(generatorClass)) {
                if (generatorClass.getConstructor(SimpleUri.class) != null) {
                    return true;
                }
            }
            return false;
            // Being generous in catching here, because if the module is broken due to code changes or missing classes the world generator is invalid
        } catch (NoSuchMethodException | RuntimeException e) {
            return false;
        }
    }

}
