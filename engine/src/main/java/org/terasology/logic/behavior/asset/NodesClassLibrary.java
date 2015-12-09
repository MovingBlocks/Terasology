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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.AbstractClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 */
public class NodesClassLibrary extends AbstractClassLibrary<Node> {
    private static final Logger logger = LoggerFactory.getLogger(NodesClassLibrary.class);

    public NodesClassLibrary(Context context) {
        super(context);
    }

    public void scan(ModuleEnvironment environment) {
        for (Class<? extends Node> entry : environment.getSubtypesOf(Node.class)) {
            logger.debug("Found node class {}", entry);
            Name moduleName = environment.getModuleProviding(entry);

            // can be null if the class was encountered in a
            // unit test (which is not part of the module)
            if (moduleName != null) {
                register(new SimpleUri(moduleName, entry.getSimpleName()), entry);
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
