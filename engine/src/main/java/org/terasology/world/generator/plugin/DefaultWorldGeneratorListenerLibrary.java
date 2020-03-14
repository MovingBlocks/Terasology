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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassLibrary;

import java.util.List;

/**
 */
public class DefaultWorldGeneratorListenerLibrary implements WorldGeneratorListenerLibrary {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWorldGeneratorListenerLibrary.class);

    private ClassLibrary<WorldGeneratorListener> library;

    public DefaultWorldGeneratorListenerLibrary(ModuleEnvironment moduleEnvironment, Context context) {
        library = new DefaultClassLibrary<>(context);
        for (Class entry : moduleEnvironment.getTypesAnnotatedWith(RegisterListener.class)) {
            if (WorldGeneratorListener.class.isAssignableFrom(entry)) {
                library.register(
                        new SimpleUri(moduleEnvironment.getModuleProviding(entry), entry.getSimpleName()), entry);
            }
        }
    }

    @Override
    public <U extends WorldGeneratorListener> List<U> instantiateAllOfType(Class<U> ofType) {
        logger.info("Instantiating {}", ofType.getSimpleName());
        List<U> result = Lists.newArrayList();
        for (ClassMetadata classMetadata : library) {
            if (ofType.isAssignableFrom(classMetadata.getType())
                    && classMetadata.isConstructable()
                    && classMetadata.getType().getAnnotation(RegisterListener.class) != null) {

                U item = ofType.cast(classMetadata.newInstance());
                if (item != null) {
                    logger.info("Adding listener {}", ofType.getSimpleName());
                    result.add(item);
                }
            }
        }
        return result;
    }
}
