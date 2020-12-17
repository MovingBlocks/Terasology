/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.world.generator.plugin;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.world.generation.Facet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class WorldGeneratorListenerLibrary {

    private static final Logger logger = LoggerFactory.getLogger(WorldGeneratorListenerLibrary.class);

    private ClassLibrary<WorldGeneratorListener> library;

    public WorldGeneratorListenerLibrary(ModuleEnvironment moduleEnvironment, Context context) {
        library = new DefaultClassLibrary<>(moduleEnvironment, context.get(ReflectFactory.class), context.get(CopyStrategyLibrary.class));
        for (Class<?> entry : moduleEnvironment.getTypesAnnotatedWith(RegisterFacetListener.class)) {
            if (WorldGeneratorListener.class.isAssignableFrom(entry)) {
                library.register(new ResourceUrn(moduleEnvironment.getModuleProviding(entry).toString(),
                        entry.getSimpleName()), entry.asSubclass(WorldGeneratorListener.class));
            }
        }
    }

    public <U extends WorldGeneratorListener> Map<Class<?>, List<U>> instantiateAllOfType(Class<U> ofType) {
        Map<Class<?>, List<U>> listenerMap = Maps.newLinkedHashMap();
        for (ClassMetadata classMetadata : library) {
            if (ofType.isAssignableFrom(classMetadata.getType())
                    && classMetadata.isConstructable()
                    && classMetadata.getType().getAnnotation(RegisterFacetListener.class) != null) {

                U item = ofType.cast(classMetadata.newInstance());
                if (item != null) {
                    RegisterFacetListener annotation =
                            (RegisterFacetListener) classMetadata.getType().getAnnotation(RegisterFacetListener.class);
                    String listenerName = item.getClass().getSimpleName();
                    logger.info("Registering FacetListener '{}'", listenerName);
                    Facet[] facets = annotation.value();
                    if (facets.length == 0) {
                        logger.error("FacetListener '{}' does not specify any Facets", listenerName);
                        continue;
                    }
                    for (Facet facet : facets) {
                        logger.info("{} listens to {}", listenerName, facet.value());
                        List<U> listeners = listenerMap.computeIfAbsent(facet.value(), k -> new LinkedList<>());
                        listeners.add(item);
                    }
                }
            }
        }
        return listenerMap;
    }
}
