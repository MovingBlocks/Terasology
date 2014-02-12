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
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * @author Immortius
 */
public class WorldGeneratorManager {
    private static final Logger logger = LoggerFactory.getLogger(WorldGeneratorManager.class);

    private ImmutableList<WorldGeneratorInfo> generatorInfo;

    public WorldGeneratorManager() {
        refresh();
    }

    public void refresh() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        CoreRegistry.get(ModuleManager.class).loadInactiveReflections();
        List<WorldGeneratorInfo> infos = Lists.newArrayList();
        for (Module module : moduleManager.getCodeModules()) {
            if (module == null) {
                logger.error("Null module in code modules?");
                continue;
            }
            if (module.getReflections() == null) {
                logger.error("Module has no reflections: {}", module.getId());
            }
            try {
                for (Class<?> generatorClass : module.getReflections().getTypesAnnotatedWith(RegisterWorldGenerator.class)) {
                    RegisterWorldGenerator annotation = generatorClass.getAnnotation(RegisterWorldGenerator.class);
                    if (isValidWorldGenerator(generatorClass)) {
                        SimpleUri uri = new SimpleUri(module.getId(), annotation.id());
                        infos.add(new WorldGeneratorInfo(uri, annotation.displayName(), annotation.description()));
                    } else {
                        logger.error("{} marked to be registered as a World Generator, but is not a subclass of WorldGenerator or lacks the correct constructor",
                                generatorClass);
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading world generator in module {}, skipping", module.getId(), e);
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
     * @param uri
     * @return The instantiated world generator.
     */
    public WorldGenerator createGenerator(SimpleUri uri) throws UnresolvedWorldGeneratorException {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        Module module = moduleManager.getActiveModule(uri.getModuleName());
        if (module == null) {
            throw new UnresolvedWorldGeneratorException("Unknown module '" + uri.getModuleName() + "' for world generator '" + uri + "'");
        }
        if (moduleManager.isEnabled(module)) {
            for (Class<?> generatorClass : module.getReflections().getTypesAnnotatedWith(RegisterWorldGenerator.class)) {
                RegisterWorldGenerator annotation = generatorClass.getAnnotation(RegisterWorldGenerator.class);
                SimpleUri generatorUri = new SimpleUri(module.getId(), annotation.id());
                if (generatorUri.equals(uri)) {
                    return loadGenerator(generatorClass, generatorUri);
                }
            }
            throw new UnresolvedWorldGeneratorException("Unable to resolve world generator '" + uri + "' - not found");
        } else {
            throw new UnresolvedWorldGeneratorException("Cannot load generator '" + uri + "', as the module it resides in is not enabled");
        }
    }

    private WorldGenerator loadGenerator(Class<?> generatorClass, SimpleUri uri) throws UnresolvedWorldGeneratorException {
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

    private boolean isValidWorldGenerator(Class<?> generatorClass) {
        try {
            return WorldGenerator.class.isAssignableFrom(generatorClass) && generatorClass.getConstructor(SimpleUri.class) != null;
            // Being generous in catching here, because if the module is broken due to code changes or missing classes the world generator is invalid
        } catch (Throwable e) {
            return false;
        }
    }

}
