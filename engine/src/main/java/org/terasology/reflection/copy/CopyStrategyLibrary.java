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
package org.terasology.reflection.copy;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.copy.strategy.ListCopyStrategy;
import org.terasology.reflection.copy.strategy.MapCopyStrategy;
import org.terasology.reflection.copy.strategy.MappedContainerCopyStrategy;
import org.terasology.reflection.copy.strategy.SetCopyStrategy;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A library of copy strategies.
 * <br><br>
 * This library is should be initialised by registering strategies for a number of core types.  Then as strategies are requested for unknown types,
 * new strategies are generated for those types.
 * The library knows how to generate strategies for Lists, Sets, Maps and types marked with the MappedContainer annotation.
 * If there is any trouble generating a strategy for a type, or it is unknown and generation is not appropriate for the type, a default strategy of returning the value
 * to be copied unaltered is returned.
 *
 */
public class CopyStrategyLibrary {
    private static final Logger logger = LoggerFactory.getLogger(CopyStrategyLibrary.class);

    private Map<Class<?>, CopyStrategy<?>> strategies = Maps.newHashMap();
    private CopyStrategy<?> defaultStrategy = new ReturnAsIsStrategy<>();
    private ReflectFactory reflectFactory;

    public CopyStrategyLibrary(ReflectFactory reflectFactory) {
        this.reflectFactory = reflectFactory;
    }


    /**
     * This constructor is not public, as it allows the direct setting of an internal field without a save copy.
     * @param strategies must not be used after it has been passed to this constructor.
     */
    private CopyStrategyLibrary(Map<Class<?>, CopyStrategy<?>> strategies, ReflectFactory reflectFactory) {
        this.strategies = strategies;
        this.reflectFactory = reflectFactory;
    }


    /**
     * Registers a copy strategy for a base type.
     *
     * @param type     The type
     * @param strategy The strategy for copying the type
     * @param <T>      The type
     */
    public <T> void register(Class<T> type, CopyStrategy<T> strategy) {
        strategies.put(type, strategy);
    }

    public void clear() {
        strategies.clear();
    }

    // TODO: Consider CopyStrategyFactory system for Collections and similar
    public CopyStrategy<?> getStrategy(Type genericType) {
        Class<?> typeClass = ReflectionUtil.getClassOfType(genericType);
        if (typeClass == null) {
            logger.error("Cannot obtain class for type {}, using default strategy", genericType);
            return defaultStrategy;
        }

        if (List.class.isAssignableFrom(typeClass)) {
            // For lists, create the handler for the contained type and wrap in a list type handler
            Type parameter = ReflectionUtil.getTypeParameter(genericType, 0);
            if (parameter != null) {
                CopyStrategy<?> contentStrategy = getStrategy(parameter);
                return new ListCopyStrategy<>(contentStrategy);
            }
            logger.error("List field is not parametrized - using default strategy");
            return new ListCopyStrategy<>(defaultStrategy);

        } else if (Set.class.isAssignableFrom(typeClass)) {
            // For sets:
            Type parameter = ReflectionUtil.getTypeParameter(genericType, 0);
            if (parameter != null) {
                CopyStrategy<?> contentStrategy = getStrategy(parameter);
                return new SetCopyStrategy<>(contentStrategy);
            }
            logger.error("Set field is not parametrized - using default strategy");
            return new SetCopyStrategy<>(defaultStrategy);

        } else if (Map.class.isAssignableFrom(typeClass)) {
            // For Maps, create the handler for the value type
            Type keyParameter = ReflectionUtil.getTypeParameter(genericType, 0);
            CopyStrategy<?> keyStrategy;
            if (keyParameter != null) {
                keyStrategy = getStrategy(keyParameter);
            } else {
                logger.error("Map field is missing key parameter - using default strategy");
                keyStrategy = defaultStrategy;
            }

            Type valueParameter = ReflectionUtil.getTypeParameter(genericType, 1);
            CopyStrategy<?> valueStrategy;
            if (valueParameter != null) {
                valueStrategy = getStrategy(valueParameter);
            } else {
                logger.error("Map field is missing value parameter - using default strategy");
                valueStrategy = defaultStrategy;
            }
            return new MapCopyStrategy<>(keyStrategy, valueStrategy);

        } else if (strategies.containsKey(typeClass)) {
            // For known types, just use the handler
            return strategies.get(typeClass);

        } else if (typeClass.getAnnotation(MappedContainer.class) != null) {
            if (Modifier.isAbstract(typeClass.getModifiers())
                    || typeClass.isLocalClass()
                    || (typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
                logger.error("Type {} is not a valid mapped class", typeClass);
                return defaultStrategy;
            }

            try {
                ClassMetadata<?, ?> classMetadata = new DefaultClassMetadata<>(new SimpleUri(), typeClass, reflectFactory, this);
                return new MappedContainerCopyStrategy<>(classMetadata);
            } catch (NoSuchMethodException e) {
                logger.error("Unable to create copy strategy for field of type {}: no publicly accessible default constructor", typeClass.getSimpleName());
                return defaultStrategy;
            }
        } else {
            logger.debug("Using default copy strategy for {}", typeClass);
            strategies.put(typeClass, defaultStrategy);
            return defaultStrategy;
        }
    }

    /**
     * @return a copy of the this library that uses the specified stategy for the specified type.
     */
    public <T>  CopyStrategyLibrary createCopyOfLibraryWithStrategy(Class<T> type, CopyStrategy<T> strategy) {
        Map<Class<?>, CopyStrategy<?>> newStrategies = Maps.newHashMap(strategies);
        newStrategies.put(type, strategy);
        return new CopyStrategyLibrary(newStrategies, this.reflectFactory);
    }

    /**
     * The default copy strategy - returns the original value.
     *
     * @param <T>
     */
    private static class ReturnAsIsStrategy<T> implements CopyStrategy<T> {

        @Override
        public T copy(T value) {
            return value;
        }
    }
}
