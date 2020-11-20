// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.coreTypes.BooleanTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteArrayTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.CharacterTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.DoubleTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.FloatTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.IntTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.LongTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.NumberTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.factories.ArrayTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.CollectionTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.EnumTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.ObjectFieldMapTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.StringMapTypeHandlerFactory;
import org.terasology.persistence.typeHandling.reflection.ReflectionsSandbox;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A library of type handlers. This is used for the construction of class metadata. This library should be initialised
 * by adding a number of base type handlers, describing how to serialize each supported type. It will then produce
 * serializers for classes (through their ClassMetadata) on request.
 */
public class TypeHandlerLibrary {
    private static final Logger logger = LoggerFactory.getLogger(TypeHandlerLibrary.class);
    /**
     * In certain object graphs, creating a {@link TypeHandler} for a type may recursively require an {@link
     * TypeHandler} for the same type. Without intervention, the recursive lookup would stack overflow. Thus, for type
     * handlers in the process of being created, we return a delegate to the {@link TypeHandler} via {@link
     * FutureTypeHandler} which is wired after the {@link TypeHandler} has been created.
     */
    private final ThreadLocal<Map<TypeInfo<?>, FutureTypeHandler<?>>> futureTypeHandlers = new ThreadLocal<>();
    private final SerializationSandbox sandbox;
    private final List<TypeHandlerFactory> typeHandlerFactories = Lists.newArrayList();
    private final Map<Type, InstanceCreator<?>> instanceCreators = Maps.newHashMap();
    private final Map<TypeInfo<?>, TypeHandler<?>> typeHandlerCache = Maps.newHashMap();
    private final Map<ClassMetadata<?, ?>, Serializer> serializerMap = Maps.newHashMap();

    protected TypeHandlerLibrary(SerializationSandbox sandbox) {
        this.sandbox = sandbox;
        ConstructorLibrary constructorLibrary = new ConstructorLibrary(instanceCreators);
        addTypeHandlerFactory(new ObjectFieldMapTypeHandlerFactory(constructorLibrary));
        TypeHandlerLibrary.populateBuiltInHandlers(this);
        addTypeHandlerFactory(new CollectionTypeHandlerFactory(constructorLibrary));
    }


    /**
     *
     */
    public TypeHandlerLibrary(Reflections reflections) {
        this(new ReflectionsSandbox(reflections));
    }

    /**
     * Constructor for copying.
     */
    private TypeHandlerLibrary(List<TypeHandlerFactory> typeHandlerFactories,
                               Map<Type, InstanceCreator<?>> instanceCreators, SerializationSandbox sandbox) {
        this.typeHandlerFactories.addAll(typeHandlerFactories);
        this.instanceCreators.putAll(instanceCreators);
        this.sandbox = sandbox;
    }

    static void populateBuiltInHandlers(TypeHandlerLibrary typeHandlerLibrary) {
        typeHandlerLibrary.addTypeHandler(Boolean.class, new BooleanTypeHandler());
        typeHandlerLibrary.addTypeHandler(Boolean.TYPE, new BooleanTypeHandler());
        typeHandlerLibrary.addTypeHandler(Byte.class, new ByteTypeHandler());
        typeHandlerLibrary.addTypeHandler(Byte.TYPE, new ByteTypeHandler());
        typeHandlerLibrary.addTypeHandler(Character.class, new CharacterTypeHandler());
        typeHandlerLibrary.addTypeHandler(Character.TYPE, new CharacterTypeHandler());
        typeHandlerLibrary.addTypeHandler(Double.class, new DoubleTypeHandler());
        typeHandlerLibrary.addTypeHandler(Double.TYPE, new DoubleTypeHandler());
        typeHandlerLibrary.addTypeHandler(Float.class, new FloatTypeHandler());
        typeHandlerLibrary.addTypeHandler(Float.TYPE, new FloatTypeHandler());
        typeHandlerLibrary.addTypeHandler(Integer.class, new IntTypeHandler());
        typeHandlerLibrary.addTypeHandler(Integer.TYPE, new IntTypeHandler());
        typeHandlerLibrary.addTypeHandler(Long.class, new LongTypeHandler());
        typeHandlerLibrary.addTypeHandler(Long.TYPE, new LongTypeHandler());
        typeHandlerLibrary.addTypeHandler(String.class, new StringTypeHandler());
        typeHandlerLibrary.addTypeHandler(Number.class, new NumberTypeHandler());

        typeHandlerLibrary.addTypeHandlerFactory(new ArrayTypeHandlerFactory());
        typeHandlerLibrary.addTypeHandler(byte[].class, new ByteArrayTypeHandler());

        typeHandlerLibrary.addTypeHandlerFactory(new EnumTypeHandlerFactory());
        typeHandlerLibrary.addTypeHandlerFactory(new StringMapTypeHandlerFactory());
    }

    /**
     * Creates a copy of an this serialization library. This copy is initialised with all type handlers that were added
     * to the original, but does not retain any serializers or type handlers that were generated. This can be used to
     * override specific types handlers from another type serializer.
     */
    public TypeHandlerLibrary copy() {
        return new TypeHandlerLibrary(this.typeHandlerFactories, this.instanceCreators, sandbox);
    }

    /**
     * Obtains a serializer for the given type
     *
     * @param type The ClassMetadata for the type of interest
     * @return A serializer for serializing/deserializing the type
     */
    public Serializer getSerializerFor(ClassMetadata<?, ?> type) {
        Serializer serializer = serializerMap.get(type);
        if (serializer == null) {
            Map<FieldMetadata<?, ?>, TypeHandler> fieldHandlerMap = getFieldHandlerMap(type);
            serializer = new Serializer(type, fieldHandlerMap);
            serializerMap.put(type, serializer);
        }
        return serializer;
    }

    /**
     * Adds a new {@link TypeHandlerFactory} to the {@link TypeHandlerLibrary}. Factories added later are given a higher
     * priority during {@link TypeHandler} generation.
     */
    public void addTypeHandlerFactory(TypeHandlerFactory typeHandlerFactory) {
        typeHandlerFactories.add(typeHandlerFactory);
    }

    /**
     * Adds a {@link TypeHandler} for the specified type to this {@link TypeHandlerLibrary} by adding to the library a
     * new {@link TypeHandlerFactory} that returns the {@link TypeHandler} whenever the {@link TypeHandler} for the
     * specified type is requested.
     * <p>
     * If the specified {@link SerializationSandbox} does not allow the addition of the given {@link TypeHandler} for
     * the given type, the {@link TypeHandler} is not added to the library and false is returned.
     *
     * @param typeClass The {@link Class} of the type handled by the {@link TypeHandler}.
     * @param typeHandler The {@link TypeHandler} to add to the library.
     * @param <T> The type handled by the {@link TypeHandler}.
     * @return True if the {@link TypeHandler} was successfully added, false otherwise.
     */
    public <T> boolean addTypeHandler(Class<T> typeClass, TypeHandler<T> typeHandler) {
        return addTypeHandler(TypeInfo.of(typeClass), typeHandler);
    }

    /**
     * Adds a {@link TypeHandler} for the specified type to this {@link TypeHandlerLibrary} by adding to the library a
     * new {@link TypeHandlerFactory} that returns the {@link TypeHandler} whenever the {@link TypeHandler} for the
     * specified type is requested.
     * <p>
     * If the specified {@link SerializationSandbox} does not allow the addition of the given {@link TypeHandler} for
     * the given type, the {@link TypeHandler} is not added to the library and false is returned.
     *
     * @param <T> The type handled by the {@link TypeHandler}.
     * @param type The {@link TypeInfo} of the type handled by the {@link TypeHandler}.
     * @param typeHandler The {@link TypeHandler} to add to the library.
     * @return True if the {@link TypeHandler} was successfully added, false otherwise.
     */
    public <T> boolean addTypeHandler(TypeInfo<T> type, TypeHandler<T> typeHandler) {
        if (!sandbox.isValidTypeHandlerDeclaration(type, typeHandler)) {
            return false;
        }

        addTypeHandlerFactory(new SpecificTypeHandlerFactory<T>(type) {
            @Override
            protected TypeHandler<T> createHandler(TypeHandlerContext context) {
                return typeHandler;
            }
        });

        return true;
    }

    /**
     * Adds an {@link InstanceCreator} to the {@link TypeHandlerLibrary} for the specified type.
     */
    public <T> void addInstanceCreator(Class<T> typeClass, InstanceCreator<T> instanceCreator) {
        addInstanceCreator(TypeInfo.of(typeClass), instanceCreator);
    }

    /**
     * Adds an {@link InstanceCreator} to the {@link TypeHandlerLibrary} for the specified type.
     */
    public <T> void addInstanceCreator(TypeInfo<T> typeInfo, InstanceCreator<T> instanceCreator) {
        instanceCreators.put(typeInfo.getType(), instanceCreator);
    }

    /**
     * Retrieves the {@link TypeHandler} for the specified type, if available.
     * <p>
     * Each {@link TypeHandlerFactory} added to this {@link TypeHandlerLibrary} is requested to generate a {@link
     * TypeHandler} for the given type. Most recently added factories are requested first, hence a {@link
     * TypeHandlerFactory} can override one that was added before it.
     *
     * @param type The {@link Type} describing the type for which to retrieve the {@link TypeHandler}.
     * @return The {@link TypeHandler} for the specified type, if available.
     */
    @SuppressWarnings({"unchecked"})
    public Optional<TypeHandler<?>> getTypeHandler(Type type) {
        TypeInfo typeInfo = TypeInfo.of(type);
        return (Optional<TypeHandler<?>>) getTypeHandler(typeInfo);
    }

    /**
     * Retrieves the {@link TypeHandler} for the specified type, if available.
     * <p>
     * Each {@link TypeHandlerFactory} added to this {@link TypeHandlerLibrary} is requested to generate a {@link
     * TypeHandler} for the given type. Most recently added factories are requested first, hence a {@link
     * TypeHandlerFactory} can override one that was added before it.
     *
     * @param typeClass The {@link Class} of the type for which to retrieve the {@link TypeHandler}.
     * @param <T> The type for which to retrieve the {@link TypeHandler}.
     * @return The {@link TypeHandler} for the specified type, if available.
     */
    public <T> Optional<TypeHandler<T>> getTypeHandler(Class<T> typeClass) {
        return getTypeHandler(TypeInfo.of(typeClass));
    }

    /**
     * Retrieves the {@link TypeHandler} for the specified type, if available.
     * <p>
     * Each {@link TypeHandlerFactory} added to this {@link TypeHandlerLibrary} is requested to generate a {@link
     * TypeHandler} for the given type. Most recently added factories are requested first, hence a {@link
     * TypeHandlerFactory} can override one that was added before it.
     *
     * @param type The {@link TypeInfo} describing the type for which to retrieve the {@link TypeHandler}.
     * @param <T> The type for which to retrieve the {@link TypeHandler}.
     * @return The {@link TypeHandler} for the specified type, if available.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<TypeHandler<T>> getTypeHandler(TypeInfo<T> type) {
        TypeHandlerContext context = new TypeHandlerContext(this, sandbox);

        if (typeHandlerCache.containsKey(type)) {
            return Optional.of((TypeHandler<T>) typeHandlerCache.get(type));
        }

        Map<TypeInfo<?>, FutureTypeHandler<?>> futures = futureTypeHandlers.get();
        boolean cleanupFutureTypeHandlers = false;

        if (futures == null) {
            cleanupFutureTypeHandlers = true;
            futures = new HashMap<>();
            futureTypeHandlers.set(futures);
        }

        FutureTypeHandler<T> future = (FutureTypeHandler<T>) futures.get(type);

        if (future != null) {
            return Optional.of(future);
        }

        try {
            future = new FutureTypeHandler<>();
            futures.put(type, future);

            // TODO: Explore reversing typeHandlerFactories itself before building object
            for (int i = typeHandlerFactories.size() - 1; i >= 0; i--) {
                TypeHandlerFactory typeHandlerFactory = typeHandlerFactories.get(i);
                Optional<TypeHandler<T>> typeHandler = typeHandlerFactory.create(type, context);

                if (typeHandler.isPresent()) {
                    TypeHandler<T> handler = typeHandler.get();

                    if (!sandbox.isValidTypeHandlerDeclaration(type, handler)) {
                        continue;
                    }

                    typeHandlerCache.put(type, handler);
                    future.typeHandler = handler;

                    return Optional.of(handler);
                }
            }

            return Optional.empty();
        } finally {
            futures.remove(type);

            if (cleanupFutureTypeHandlers) {
                futureTypeHandlers.remove();
            }
        }
    }

    /**
     * Returns a {@link TypeHandler} that can handle all types deriving from {@link T}.
     *
     * @param typeInfo The {@link TypeInfo} describing the base type for which to return a {@link TypeHandler}.
     * @param <T> The base type for which to return a {@link TypeHandler}.
     */
    public <T> TypeHandler<T> getBaseTypeHandler(TypeInfo<T> typeInfo) {
        TypeHandler<T> delegateHandler = getTypeHandler(typeInfo).orElse(null);

        TypeHandlerContext context = new TypeHandlerContext(this, sandbox);
        return new RuntimeDelegatingTypeHandler<>(delegateHandler, typeInfo, context);
    }

    private Map<FieldMetadata<?, ?>, TypeHandler> getFieldHandlerMap(ClassMetadata<?, ?> type) {
        Map<FieldMetadata<?, ?>, TypeHandler> handlerMap = Maps.newHashMap();
        for (FieldMetadata<?, ?> field : type.getFields()) {
            Optional<TypeHandler<?>> handler = getTypeHandler(field.getField().getGenericType());

            if (handler.isPresent()) {
                handlerMap.put(field, handler.get());
            } else {
                logger.error("Unsupported field: '{}.{}'", type.getUri(), field.getName());
            }
        }
        return handlerMap;
    }
}
