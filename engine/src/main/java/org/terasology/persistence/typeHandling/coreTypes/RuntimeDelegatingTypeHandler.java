/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Maps;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.inMemory.PersistedMap;
import org.terasology.reflection.TypeInfo;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Delegates serialization of a value to a handler of its runtime type if needed. It is used in
 * cases where a subclass instance can be referred to as its supertype. As such, it is meant
 * for internal use in another {@link TypeHandler} only, and is never directly registered
 * in a {@link TypeHandlerLibrary}.
 *
 * @param <T> The base type whose instances may be delegated to a subtype's {@link TypeHandler} at runtime.
 */
public class RuntimeDelegatingTypeHandler<T> extends TypeHandler<T> {
    static final String TYPE_FIELD = "class";
    static final String VALUE_FIELD = "content";

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeDelegatingTypeHandler.class);

    private TypeHandler<T> delegateHandler;
    private TypeInfo<T> typeInfo;
    private TypeHandlerLibrary typeHandlerLibrary;
    private Reflections reflections;

    public RuntimeDelegatingTypeHandler(TypeHandler<T> delegateHandler, TypeInfo<T> typeInfo, TypeHandlerContext context) {
        this.delegateHandler = delegateHandler;
        this.typeInfo = typeInfo;
        this.typeHandlerLibrary = context.getTypeHandlerLibrary();
        this.reflections = context.getReflections();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        // If primitive, don't go looking for the runtime type, serialize as is
        if (typeInfo.getRawType().isPrimitive()) {
            if (delegateHandler != null) {
                return delegateHandler.serialize(value, serializer);
            }

            LOGGER.error("Primitive {} does not have a TypeHandler", typeInfo.getRawType().getName());
            return serializer.serializeNull();
        }

        TypeHandler<T> chosenHandler = delegateHandler;
        Type runtimeType = getRuntimeTypeIfMoreSpecific(value);

        if (!typeInfo.getRawType().equals(runtimeType)) {
            Optional<TypeHandler<?>> runtimeTypeHandler = typeHandlerLibrary.getTypeHandler(runtimeType);

            chosenHandler = (TypeHandler<T>) runtimeTypeHandler
                    .map(typeHandler -> {
                        if (delegateHandler == null) {
                            return typeHandler;
                        } else if (!(typeHandler instanceof ObjectFieldMapTypeHandler)) {
                            if (typeHandler.getClass().equals(delegateHandler.getClass())) {
                                // Both handlers are of same type, use delegateHandler
                                return delegateHandler;
                            }

                            // Custom handler for runtime type
                            return typeHandler;
                        } else if (!(delegateHandler instanceof ObjectFieldMapTypeHandler)) {
                            // Custom handler for specified type
                            return delegateHandler;
                        }

                        return typeHandler;
                    })
                    .orElse(delegateHandler);
        }

        if (chosenHandler == null) {
            LOGGER.error("Could not find appropriate TypeHandler for runtime type {}", runtimeType);
            return serializer.serializeNull();
        }

        if (chosenHandler == delegateHandler) {
            return delegateHandler.serialize(value, serializer);
        }

        Map<String, PersistedData> typeValuePersistedDataMap = Maps.newLinkedHashMap();

        typeValuePersistedDataMap.put(
                TYPE_FIELD,
                serializer.serialize(ReflectionUtil.getRawType(runtimeType).getName())
        );

        PersistedData serialized = chosenHandler.serialize(value, serializer);

        // If the serialized representation is a Map, flatten it to include the class variable
        if (serialized.isValueMap()) {
            for (Map.Entry<String, PersistedData> entry : serialized.getAsValueMap().entrySet()) {
                typeValuePersistedDataMap.put(entry.getKey(), entry.getValue());
            }
        }
        else {
            typeValuePersistedDataMap.put(
                VALUE_FIELD,
                serialized
            );
        }

        return serializer.serialize(typeValuePersistedDataMap);
    }

    private Type getRuntimeTypeIfMoreSpecific(T value) {
        if (value == null) {
            return typeInfo.getRawType();
        }

        return ReflectionUtil.parameterizeandResolveRawType(typeInfo.getType(), value.getClass());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (!data.isValueMap()) {
            return delegateHandler.deserialize(data);
        }

        PersistedDataMap valueMap = data.getAsValueMap();

        if (!valueMap.has(TYPE_FIELD)) {
            return delegateHandler.deserialize(data);
        }

        String runtimeTypeName = valueMap.getAsString(TYPE_FIELD);

        Optional<Type> typeToDeserializeAs = findSubtypeWithName(runtimeTypeName);

        if (!typeToDeserializeAs.isPresent()) {
            LOGGER.error("Cannot find class to deserialize {}", runtimeTypeName);
            return Optional.empty();
        }

        TypeHandler<T> runtimeTypeHandler = (TypeHandler<T>) typeHandlerLibrary.getTypeHandler(typeToDeserializeAs.get())
                // To avoid compile errors in the orElseGet
                .map(typeHandler -> (TypeHandler) typeHandler)
                .orElseGet(() -> {
                    LOGGER.warn("Cannot find TypeHandler for runtime class {}, " +
                                    "deserializing as base class {}",
                            runtimeTypeName, typeInfo.getRawType().getName());
                    return delegateHandler;
                });

        PersistedData valueData;

        Set<Map.Entry<String, PersistedData>> valueEntries = valueMap.entrySet();

        if (valueEntries.size() == 2 && valueMap.has(VALUE_FIELD)) {
            // The runtime value was stored in a separate field only if the two fields stored
            // are TYPE_FIELD and VALUE_FIELD

            valueData = valueMap.get(VALUE_FIELD);
        } else {
            // The value was flattened and stored, every field except TYPE_FIELD describes the
            // serialized value

            Map<String, PersistedData> valueFields = Maps.newLinkedHashMap();

            for (Map.Entry<String, PersistedData> entry : valueEntries) {
                valueFields.put(entry.getKey(), entry.getValue());
            }

            valueFields.remove(TYPE_FIELD);

            valueData = new PersistedMap(valueFields);
        }

        return runtimeTypeHandler.deserialize(valueData);

    }

    private Optional<Type> findSubtypeWithName(String runtimeTypeName) {
        return findSubclassWithName(runtimeTypeName)
                .map(runtimeClass ->
                        ReflectionUtil.parameterizeandResolveRawType(typeInfo.getType(), runtimeClass)
                );
    }

    private Optional<Class<?>> findSubclassWithName(String runtimeTypeName) {
        for (Class<?> clazz : reflections.getSubTypesOf(typeInfo.getRawType())) {
            if (runtimeTypeName.equals(clazz.getSimpleName()) ||
                    runtimeTypeName.equals(clazz.getName())) {
                return Optional.of(clazz);
            }
        }

        return Optional.empty();
    }
}
