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
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.AbstractClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

import java.util.Map;

/**
 * @author synopia
 */
public class NodesClassLibrary extends AbstractClassLibrary<Node> {
    private static final Logger logger = LoggerFactory.getLogger(NodesClassLibrary.class);

    public NodesClassLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        super(factory, copyStrategies);
    }

    public void scan(ModuleManager moduleManager) {
        for (Map.Entry<String, Class<? extends Node>> entry : moduleManager.findAllSubclassesOf(Node.class).entries()) {
            logger.debug("Found node class {}", entry.getValue());
            register(new SimpleUri(entry.getKey(), entry.getValue().getSimpleName()), entry.getValue());
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
