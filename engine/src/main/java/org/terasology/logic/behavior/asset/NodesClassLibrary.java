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
package org.terasology.logic.behavior.asset;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.AbstractClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;

import java.util.Set;

/**
 * @author synopia
 */
public class NodesClassLibrary extends AbstractClassLibrary<Node> {
    private static final Logger logger = LoggerFactory.getLogger(NodesClassLibrary.class);

    public NodesClassLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        super(factory, copyStrategies);
    }

    public void scan() {
        Set<Class<?>> parentTypes = Sets.newHashSet();

        for (Class<? extends Node> type : CoreRegistry.get(ModuleManager.class).getActiveModuleReflections().getSubTypesOf(Node.class)) {
            parentTypes.add(type.getSuperclass());
        }
        for (Module module : CoreRegistry.get(ModuleManager.class).getActiveCodeModules()) {
            for (Class<?> parentType : parentTypes) {
                for (Class<?> type : module.getReflections().getSubTypesOf(parentType)) {
                    logger.info("Found node class " + type);
                    register(new SimpleUri(module.getId(), type.getSimpleName()), (Class<? extends Node>) type);
                }

            }
        }
    }

    @Override
    protected <N extends Node> ClassMetadata<N, ?> createMetadata(Class<N> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, SimpleUri uri) {
        try {
            return new DefaultClassMetadata<>(uri, type, factory, copyStrategies);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;
        }
    }
}
