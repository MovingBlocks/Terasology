/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.generator.plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.asset.AssetManager;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.DependencyInfo;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;

import java.util.List;
import java.util.Set;

/**
 * @author Immortius
 */
public class WorldGeneratorPluginLibrary {

    private ClassLibrary<WorldGeneratorPlugin> library;

    public WorldGeneratorPluginLibrary(ModuleEnvironment moduleEnvironment, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        library = new DefaultClassLibrary<>(reflectFactory, copyStrategyLibrary);
        for (Class entry : moduleEnvironment.getTypesAnnotatedWith(RegisterPlugin.class)) {
            if (WorldGeneratorPlugin.class.isAssignableFrom(entry)) {
                library.register(new SimpleUri(moduleEnvironment.getModuleProviding(entry), entry.getSimpleName()), entry);
            }
        }
    }

    public <U extends WorldGeneratorPlugin> List<U> instantiateAllOfType(Class<U> ofType) {
        List<U> result = Lists.newArrayList();
        for (ClassMetadata classMetadata : library) {
            if (ofType.isAssignableFrom(classMetadata.getType()) && classMetadata.isConstructable() && classMetadata.getType().getAnnotation(RegisterPlugin.class) != null) {
                U item = ofType.cast(classMetadata.newInstance());
                if (item != null) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * Create a fake environment so that plugins can be loaded for configuration
     */
    public static void setupTempEnvironmentForPlugins() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        Config config = CoreRegistry.get(Config.class);

        Set<Module> selectedModules = Sets.newHashSet();
        for (Name moduleName : config.getDefaultModSelection().listModules()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleName);
            selectedModules.add(module);
            for (DependencyInfo dependencyInfo : module.getMetadata().getDependencies()) {
                selectedModules.add(moduleManager.getRegistry().getLatestModuleVersion(dependencyInfo.getId()));
            }
        }
        ModuleEnvironment environment = moduleManager.loadEnvironment(selectedModules, false);
        assetManager.setEnvironment(environment);
        CoreRegistry.put(WorldGeneratorPluginLibrary.class, new WorldGeneratorPluginLibrary(environment,
                CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(CopyStrategyLibrary.class)));
    }

}
