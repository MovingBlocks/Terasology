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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.reflection.TypeInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Delegates serialization of a value to a handler of its runtime type if needed. It is used in
 * cases where a subclass instance can be referred to as its supertype. As such, it is meant
 * for internal use in another {@link TypeHandler} only, and is never directly registered
 * in a {@link TypeSerializationLibrary}.
 *
 * @param <T> The base type whose instances may be delegated to a subtype's {@link TypeHandler} at runtime.
 */
public class RuntimeDelegatingTypeHandler<T> extends TypeHandler<T> {
    static final String TYPE_FIELD = "@type";
    static final String VALUE_FIELD = "@value";

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeDelegatingTypeHandler.class);

    private TypeHandler<T> delegateHandler;
    private TypeInfo<T> typeInfo;
    private TypeSerializationLibrary typeSerializationLibrary;

    public RuntimeDelegatingTypeHandler(TypeHandler<T> delegateHandler, TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        this.delegateHandler = delegateHandler;
        this.typeInfo = typeInfo;
        this.typeSerializationLibrary = typeSerializationLibrary;
    }

    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        // If primitive, don't go looking for the runtime type, serialize as is
        if (typeInfo.getRawType().isPrimitive()) {
            return delegateHandler.serialize(value, serializer);
        }

        TypeHandler<T> chosenHandler = delegateHandler;
        Class<?> runtimeClass = getRuntimeTypeIfMoreSpecific(typeInfo, value);

        if (!typeInfo.getRawType().equals(runtimeClass)) {
            TypeHandler<T> runtimeTypeHandler =
                    (TypeHandler<T>) typeSerializationLibrary.getTypeHandler(runtimeClass);

            if (delegateHandler == null) {
                chosenHandler = runtimeTypeHandler;
            } else if (runtimeTypeHandler.getClass().equals(delegateHandler.getClass())) {
                // Both handlers are of same type, use delegateHandler
                chosenHandler = delegateHandler;
            } else if (!(runtimeTypeHandler instanceof ObjectFieldMapTypeHandler)) {
                // Custom handler for runtime type
                chosenHandler = runtimeTypeHandler;
            } else if (!(delegateHandler instanceof ObjectFieldMapTypeHandler)) {
                // Custom handler for specified type
                chosenHandler = delegateHandler;
            } else {
                chosenHandler = runtimeTypeHandler;
            }
        }

        if (chosenHandler == delegateHandler) {
            return delegateHandler.serialize(value, serializer);
        }

        Map<String, PersistedData> typeValuePersistedDataMap = Maps.newLinkedHashMap();

        typeValuePersistedDataMap.put(
                TYPE_FIELD,
                serializer.serialize(runtimeClass.getName())
        );

        typeValuePersistedDataMap.put(
                VALUE_FIELD,
                chosenHandler.serialize(value, serializer)
        );

        return serializer.serialize(typeValuePersistedDataMap);
    }

    private static <T> Class<?> getRuntimeTypeIfMoreSpecific(TypeInfo<T> typeInfo, T value) {
        if (value == null) {
            return typeInfo.getRawType();
        }

        Class<?> runtimeClass = value.getClass();

        if (typeInfo.getRawType().isInterface()) {
            // Given type is interface, use runtime type which will be a class and will have data
            return runtimeClass;
        } else if (typeInfo.getType() instanceof Class) {
            // If given type is a simple class, use more specific runtime type
            return runtimeClass;
        }

        // Given type has more information than runtime type, use that
        return typeInfo.getRawType();
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (!data.isValueMap()) {
            return delegateHandler.deserialize(data);
        }

        PersistedDataMap valueMap = data.getAsValueMap();

        if (!valueMap.has(TYPE_FIELD) || !valueMap.has(VALUE_FIELD)) {
            return delegateHandler.deserialize(data);
        }

        String typeName = valueMap.getAsString(TYPE_FIELD);

        // TODO: Use context class loaders
        Class<?> typeToDeserializeAs;

        try {
            typeToDeserializeAs = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Cannot find class {}", typeName);
            return null;
        }

        TypeHandler<T> typeHandler =
                (TypeHandler<T>) typeSerializationLibrary.getTypeHandler(typeToDeserializeAs);

        PersistedData valueData = valueMap.get(VALUE_FIELD);

        return typeHandler.deserialize(valueData);

    }
}
