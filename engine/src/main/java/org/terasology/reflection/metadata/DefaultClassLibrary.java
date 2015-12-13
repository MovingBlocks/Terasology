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
package org.terasology.reflection.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

/**
 * A simple implementation of ClassLibrary. It provides ClassMetadata for a type of class. These classes are identified through their simple name.
 *
 * @param <T> The base type of classes that can be registered in this library
 */
public final class DefaultClassLibrary<T> extends AbstractClassLibrary<T> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultClassLibrary.class);

    public DefaultClassLibrary(Context context) {
        super(context);
    }

    @Override
    protected <C extends T> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, SimpleUri uri) {
        try {
            return new DefaultClassMetadata<>(uri, type, factory, copyStrategies);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", type.getSimpleName(), e);
            return null;

        }
    }
}
