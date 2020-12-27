// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.ReflectionUtil;
import org.terasology.reflection.TypeInfo;

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
    private SerializationSandbox sandbox;

    public RuntimeDelegatingTypeHandler(TypeHandler<T> delegateHandler, TypeInfo<T> typeInfo, TypeHandlerContext context) {
        this.delegateHandler = delegateHandler;
        this.typeInfo = typeInfo;
        this.typeHandlerLibrary = context.getTypeHandlerLibrary();
        this.sandbox = context.getSandbox();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        // If primitive, don't go looking for the runtime type, serialize as is
        if (typeInfo.getRawType().isPrimitive() || Number.class.isAssignableFrom(typeInfo.getRawType())) {
            if (delegateHandler != null) {
                return delegateHandler.serialize(value, serializer);
            }

            LOGGER.error("Primitive '{}' does not have a TypeHandler", typeInfo);
            return serializer.serializeNull();
        }

        TypeHandler<T> chosenHandler = delegateHandler;
        Type runtimeType = getRuntimeTypeIfMoreSpecific(value);

        if (!typeInfo.getType().equals(runtimeType)) {
            Optional<TypeHandler<?>> runtimeTypeHandler = typeHandlerLibrary.getTypeHandler(runtimeType);

            chosenHandler =
                (TypeHandler<T>)
                    runtimeTypeHandler
                        .map(typeHandler -> {
                            if (delegateHandler == null) {
                                return typeHandler;
                            }

                            if (!(typeHandler instanceof ObjectFieldMapTypeHandler) &&
                                    typeHandler.getClass().equals(delegateHandler.getClass())) {
                                // Both handlers are of same type and will do the same thing,
                                // use delegateHandler which might have more info
                                return delegateHandler;
                            }

                            if (!isDefaultTypeHandler(typeHandler)) {
                                // Custom handler for runtime type
                                return typeHandler;
                            }

                            if (!isDefaultTypeHandler(delegateHandler)) {
                                // Custom handler for specified type
                                return delegateHandler;
                            }

                            return typeHandler;
                        })
                        .orElse(delegateHandler);
        }

        if (chosenHandler == null) {
            LOGGER.warn("Could not find appropriate TypeHandler for runtime type '{}', " +
                            "serializing as base type '{}'", runtimeType, typeInfo);
            return serializeViaDelegate(value, serializer);
        }

        if (chosenHandler == delegateHandler) {
            return serializeViaDelegate(value, serializer);
        }

        Map<String, PersistedData> typeValuePersistedDataMap = Maps.newLinkedHashMap();

        Class<? extends T> subType = (Class<? extends T>) ReflectionUtil.getRawType(runtimeType);
        String subTypeIdentifier = sandbox.getSubTypeIdentifier(subType, typeInfo.getRawType());

        typeValuePersistedDataMap.put(
            TYPE_FIELD,
            serializer.serialize(subTypeIdentifier)
        );

        PersistedData serialized = chosenHandler.serialize(value, serializer);

        // If the serialized representation is a Map, flatten it to include the class variable
        if (serialized.isValueMap()) {
            for (Map.Entry<String, PersistedData> entry : serialized.getAsValueMap().entrySet()) {
                typeValuePersistedDataMap.put(entry.getKey(), entry.getValue());
            }
        } else {
            typeValuePersistedDataMap.put(
                VALUE_FIELD,
                serialized
            );
        }

        return serializer.serialize(typeValuePersistedDataMap);
    }

    private boolean isDefaultTypeHandler(TypeHandler<?> typeHandler) {
        return typeHandler instanceof ObjectFieldMapTypeHandler ||
                   typeHandler instanceof EnumTypeHandler ||
                   typeHandler instanceof CollectionTypeHandler ||
                   typeHandler instanceof StringMapTypeHandler ||
                   typeHandler instanceof ArrayTypeHandler;
    }

    private PersistedData serializeViaDelegate(T value, PersistedDataSerializer serializer) {
        if (delegateHandler == null) {
            LOGGER.error("Base type '{}' does not have a handler", typeInfo);
            return serializer.serializeNull();
        }

        return delegateHandler.serialize(value, serializer);
    }

    private Type getRuntimeTypeIfMoreSpecific(T value) {
        if (value == null) {
            return typeInfo.getType();
        }

        return ReflectionUtil.parameterizeandResolveRawType(typeInfo.getType(), value.getClass());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (!data.isValueMap()) {
            return deserializeViaDelegate(data);
        }

        PersistedDataMap valueMap = data.getAsValueMap();

        if (!valueMap.has(TYPE_FIELD)) {
            return deserializeViaDelegate(data);
        }

        String runtimeTypeName = valueMap.getAsString(TYPE_FIELD);

        Optional<Type> typeToDeserializeAs = findSubtypeWithName(runtimeTypeName);

        if (!typeToDeserializeAs.isPresent()) {
            LOGGER.warn("Cannot find subtype '{}' to deserialize as, " +
                            "deserializing as base type '{}'",
                runtimeTypeName,
                typeInfo
            );
            return deserializeViaDelegate(data);
        }

        TypeHandler<T> runtimeTypeHandler =
            (TypeHandler<T>) typeHandlerLibrary.getTypeHandler(typeToDeserializeAs.get())
                                 // To avoid compile errors in the orElseGet
                                 .map(typeHandler -> (TypeHandler) typeHandler)
                                 .orElseGet(() -> {
                                     LOGGER.warn("Cannot find TypeHandler for runtime type '{}', " +
                                                     "deserializing as base type '{}'",
                                         runtimeTypeName, typeInfo);
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

            valueData = PersistedDataMap.of(valueFields);
        }

        return runtimeTypeHandler.deserialize(valueData);

    }

    private Optional<T> deserializeViaDelegate(PersistedData data) {
        if (delegateHandler == null) {
            LOGGER.error("Base type '{}' does not have a handler and no \"{}\" field " +
                             "was found in the serialized form {}",
                typeInfo,
                TYPE_FIELD,
                data);
            return Optional.empty();
        }

        return delegateHandler.deserialize(data);
    }

    private Optional<Type> findSubtypeWithName(String runtimeTypeName) {
        return sandbox.findSubTypeOf(runtimeTypeName, typeInfo.getRawType())
                   .map(runtimeClass ->
                            ReflectionUtil.parameterizeandResolveRawType(typeInfo.getType(), runtimeClass)
                   );
    }

}
