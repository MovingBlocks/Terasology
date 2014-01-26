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
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

import java.util.List;

/**
 * @author Immortius
 */
public class WorldGeneratorPluginLibrary {

    private ClassLibrary<WorldGeneratorPlugin> library;

    public WorldGeneratorPluginLibrary(ModuleManager moduleManager, ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategyLibrary) {
        library = new DefaultClassLibrary<>(reflectFactory, copyStrategyLibrary);
        for (Module module : moduleManager.getActiveCodeModules()) {
            for (Class<? extends WorldGeneratorPlugin> pluginType : module.getReflections().getSubTypesOf(WorldGeneratorPlugin.class)) {
                String pluginName = pluginType.getSimpleName();
                library.register(new SimpleUri(module.getId(), pluginName), pluginType);
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
}
