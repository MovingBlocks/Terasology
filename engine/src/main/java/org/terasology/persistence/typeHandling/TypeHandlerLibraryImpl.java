// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.AABBf;
import org.joml.AABBi;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.IntegerRange;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.naming.Name;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;
import org.terasology.nui.UITextureRegion;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.factories.CollectionTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.ObjectFieldMapTypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.ColorTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.ColorcTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.NameTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.PrefabTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.UITextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.factories.AssetTypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.factories.ComponentClassTypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.factories.TextureRegionAssetTypeHandlerFactory;
import org.terasology.persistence.typeHandling.mathTypes.AABBfTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.AABBiTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.BlockRegionTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.IntegerRangeHandler;
import org.terasology.persistence.typeHandling.mathTypes.QuaternionfTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.QuaternionfcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2icTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3icTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fcTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4icTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.factories.Rect2fTypeHandlerFactory;
import org.terasology.persistence.typeHandling.mathTypes.factories.Rect2iTypeHandlerFactory;
import org.terasology.persistence.typeHandling.mathTypes.legacy.LegacyQuat4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.legacy.LegacyVector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.legacy.LegacyVector2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.legacy.LegacyVector3fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.legacy.LegacyVector3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.legacy.LegacyVector4fTypeHandler;
import org.terasology.persistence.typeHandling.reflection.ModuleEnvironmentSandbox;
import org.terasology.persistence.typeHandling.reflection.ReflectionsSandbox;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.TypeRegistry;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.world.block.BlockRegion;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A library of type handlers. This is used for the construction of class metadata.
 * This library should be initialised by adding a number of base type handlers, describing how to serialize each supported type.
 * It will then produce serializers for classes (through their ClassMetadata) on request.
 */
public class TypeHandlerLibraryImpl implements TypeHandlerLibrary {
    private static final Logger logger = LoggerFactory.getLogger(TypeHandlerLibraryImpl.class);

    private SerializationSandbox sandbox;

    private List<TypeHandlerFactory> typeHandlerFactories = Lists.newArrayList();

    private Map<TypeInfo<?>, TypeHandler<?>> typeHandlerCache = Maps.newHashMap();

    /**
     * In certain object graphs, creating a {@link TypeHandler} for a type may recursively
     * require an {@link TypeHandler} for the same type. Without intervention, the recursive
     * lookup would stack overflow. Thus, for type handlers in the process of being created,
     * we return a delegate to the {@link TypeHandler} via {@link FutureTypeHandler} which is
     * wired after the {@link TypeHandler} has been created.
     */
    private final ThreadLocal<Map<TypeInfo<?>, FutureTypeHandler<?>>> futureTypeHandlers = new ThreadLocal<>();

    private Map<Type, InstanceCreator<?>> instanceCreators = Maps.newHashMap();
    private ConstructorLibrary constructorLibrary;

    private Map<ClassMetadata<?, ?>, Serializer> serializerMap = Maps.newHashMap();

    private TypeHandlerLibraryImpl(SerializationSandbox sandbox) {
        this.sandbox = sandbox;

        constructorLibrary = new ConstructorLibrary(instanceCreators);

        addTypeHandlerFactory(new ObjectFieldMapTypeHandlerFactory(constructorLibrary));
        
        TypeHandlerLibrary.populateBuildinHandlers(this);
        
        addTypeHandlerFactory(new CollectionTypeHandlerFactory(constructorLibrary));
        
        addTypeHandlerFactory(new ComponentClassTypeHandlerFactory());
    }

    /**
     *
     */
    public TypeHandlerLibraryImpl(Reflections reflections) {
        this(new ReflectionsSandbox(reflections));
    }

    public TypeHandlerLibraryImpl(ModuleManager moduleManager, TypeRegistry typeRegistry) {
        this(new ModuleEnvironmentSandbox(moduleManager, typeRegistry));
    }

    /**
     * Constructor for copying.
     */
    private TypeHandlerLibraryImpl(List<TypeHandlerFactory> typeHandlerFactories,
                                   Map<Type, InstanceCreator<?>> instanceCreators, SerializationSandbox sandbox) {
        this.typeHandlerFactories.addAll(typeHandlerFactories);
        this.instanceCreators.putAll(instanceCreators);
        this.sandbox = sandbox;
    }

    /**
     * Creates a copy of an this serialization library. This copy is initialised with all type handlers that were added
     * to the original, but does not retain any serializers or type handlers that were generated. This can be used to
     * override specific types handlers from another type serializer.
     */
    @Override
    public TypeHandlerLibrary copy() {
        return new TypeHandlerLibraryImpl(this.typeHandlerFactories, this.instanceCreators, sandbox);

    }

    public static TypeHandlerLibrary withReflections(Reflections reflections) {
        TypeHandlerLibrary library = new TypeHandlerLibraryImpl(reflections);

        populateWithDefaultHandlers(library);

        return library;
    }

    public static TypeHandlerLibrary forModuleEnvironment(ModuleManager moduleManager, TypeRegistry typeRegistry) {
        TypeHandlerLibrary library = new TypeHandlerLibraryImpl(moduleManager, typeRegistry);

        populateWithDefaultHandlers(library);

        return library;
    }

    private static void populateWithDefaultHandlers(TypeHandlerLibrary serializationLibrary) {
        // LEGACY
        serializationLibrary.addTypeHandler(Vector4f.class, new LegacyVector4fTypeHandler());
        serializationLibrary.addTypeHandler(Vector3f.class, new LegacyVector3fTypeHandler());
        serializationLibrary.addTypeHandler(Vector2f.class, new LegacyVector2fTypeHandler());
        serializationLibrary.addTypeHandler(Vector3i.class, new LegacyVector3iTypeHandler());
        serializationLibrary.addTypeHandler(Vector2i.class, new LegacyVector2iTypeHandler());
        serializationLibrary.addTypeHandler(Quat4f.class, new LegacyQuat4fTypeHandler());

        // Current Supported
        serializationLibrary.addTypeHandlerFactory(new AssetTypeHandlerFactory());

        serializationLibrary.addTypeHandler(Name.class, new NameTypeHandler());
        serializationLibrary.addTypeHandler(TextureRegion.class, new TextureRegionTypeHandler());
        serializationLibrary.addTypeHandler(UITextureRegion.class, new UITextureRegionTypeHandler());

        serializationLibrary.addTypeHandlerFactory(new TextureRegionAssetTypeHandlerFactory());

        serializationLibrary.addTypeHandler(Color.class, new ColorTypeHandler());
        serializationLibrary.addTypeHandler(Colorc.class, new ColorcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector4f.class, new Vector4fTypeHandler());
        serializationLibrary.addTypeHandler(Vector4fc.class, new Vector4fcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector3f.class, new Vector3fTypeHandler());
        serializationLibrary.addTypeHandler(Vector3fc.class, new Vector3fcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector2f.class, new Vector2fTypeHandler());
        serializationLibrary.addTypeHandler(Vector2fc.class, new Vector2fcTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector3i.class, new Vector3iTypeHandler());
        serializationLibrary.addTypeHandler(Vector3ic.class, new Vector3icTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector2i.class, new Vector2iTypeHandler());
        serializationLibrary.addTypeHandler(Vector2ic.class, new Vector2icTypeHandler());

        serializationLibrary.addTypeHandler(org.joml.Vector4i.class, new Vector4iTypeHandler());
        serializationLibrary.addTypeHandler(Vector4ic.class, new Vector4icTypeHandler());

        serializationLibrary.addTypeHandler(AABBi.class, new AABBiTypeHandler());
        serializationLibrary.addTypeHandler(AABBf.class, new AABBfTypeHandler());
        serializationLibrary.addTypeHandler(BlockRegion.class, new BlockRegionTypeHandler());

        serializationLibrary.addTypeHandler(Quaternionf.class, new QuaternionfTypeHandler());
        serializationLibrary.addTypeHandler(Quaternionfc.class, new QuaternionfcTypeHandler());

        serializationLibrary.addTypeHandlerFactory(new Rect2iTypeHandlerFactory());
        serializationLibrary.addTypeHandlerFactory(new Rect2fTypeHandlerFactory());
        serializationLibrary.addTypeHandler(Prefab.class, new PrefabTypeHandler());
        serializationLibrary.addTypeHandler(IntegerRange.class, new IntegerRangeHandler());
    }

    /**
     * Obtains a serializer for the given type
     *
     * @param type The ClassMetadata for the type of interest
     * @return A serializer for serializing/deserializing the type
     */
    @Override
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
     * Adds a new {@link TypeHandlerFactory} to the {@link TypeHandlerLibraryImpl}. Factories
     * added later are given a higher priority during {@link TypeHandler} generation.
     */
    @Override
    public void addTypeHandlerFactory(TypeHandlerFactory typeHandlerFactory) {
        typeHandlerFactories.add(typeHandlerFactory);
    }

    /**
     * Adds a {@link TypeHandler} for the specified type to this {@link TypeHandlerLibraryImpl} by
     * adding to the library a new {@link TypeHandlerFactory} that returns the {@link TypeHandler}
     * whenever the {@link TypeHandler} for the specified type is requested.
     * <p>
     * If the specified {@link SerializationSandbox} does not allow the addition of the given
     * {@link TypeHandler} for the given type, the {@link TypeHandler} is not added to the
     * library and false is returned.
     *
     * @param typeClass   The {@link Class} of the type handled by the {@link TypeHandler}.
     * @param typeHandler The {@link TypeHandler} to add to the library.
     * @param <T>         The type handled by the {@link TypeHandler}.
     * @return True if the {@link TypeHandler} was successfully added, false otherwise.
     */
    @Override
    public <T> boolean addTypeHandler(Class<T> typeClass, TypeHandler<T> typeHandler) {
        return addTypeHandler(TypeInfo.of(typeClass), typeHandler);
    }

    /**
     * Adds a {@link TypeHandler} for the specified type to this {@link TypeHandlerLibraryImpl} by
     * adding to the library a new {@link TypeHandlerFactory} that returns the {@link TypeHandler}
     * whenever the {@link TypeHandler} for the specified type is requested.
     * <p>
     * If the specified {@link SerializationSandbox} does not allow the addition of the given
     * {@link TypeHandler} for the given type, the {@link TypeHandler} is not added to the
     * library and false is returned.
     *
     * @param <T>         The type handled by the {@link TypeHandler}.
     * @param type        The {@link TypeInfo} of the type handled by the {@link TypeHandler}.
     * @param typeHandler The {@link TypeHandler} to add to the library.
     * @return True if the {@link TypeHandler} was successfully added, false otherwise.
     */
    @Override
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
     * Adds an {@link InstanceCreator} to the {@link TypeHandlerLibraryImpl} for the specified type.
     */
    @Override
    public <T> void addInstanceCreator(Class<T> typeClass, InstanceCreator<T> instanceCreator) {
        addInstanceCreator(TypeInfo.of(typeClass), instanceCreator);
    }

    /**
     * Adds an {@link InstanceCreator} to the {@link TypeHandlerLibraryImpl} for the specified type.
     */
    @Override
    public <T> void addInstanceCreator(TypeInfo<T> typeInfo, InstanceCreator<T> instanceCreator) {
        instanceCreators.put(typeInfo.getType(), instanceCreator);
    }

    /**
     * Retrieves the {@link TypeHandler} for the specified type, if available.
     * <p>
     * Each {@link TypeHandlerFactory} added to this {@link TypeHandlerLibraryImpl} is requested
     * to generate a {@link TypeHandler} for the given type. Most recently added factories are
     * requested first, hence a {@link TypeHandlerFactory} can override one that was added
     * before it.
     *
     * @param type The {@link Type} describing the type for which to
     *             retrieve the {@link TypeHandler}.
     * @return The {@link TypeHandler} for the specified type, if available.
     */
    @Override
    @SuppressWarnings({"unchecked"})
    public Optional<TypeHandler<?>> getTypeHandler(Type type) {
        TypeInfo typeInfo = TypeInfo.of(type);
        return (Optional<TypeHandler<?>>) getTypeHandler(typeInfo);
    }

    /**
     * Retrieves the {@link TypeHandler} for the specified type, if available.
     * <p>
     * Each {@link TypeHandlerFactory} added to this {@link TypeHandlerLibraryImpl} is requested
     * to generate a {@link TypeHandler} for the given type. Most recently added factories are
     * requested first, hence a {@link TypeHandlerFactory} can override one that was added
     * before it.
     *
     * @param typeClass The {@link Class} of the type for which to
     *                  retrieve the {@link TypeHandler}.
     * @param <T>       The type for which to retrieve the {@link TypeHandler}.
     * @return The {@link TypeHandler} for the specified type, if available.
     */
    @Override
    public <T> Optional<TypeHandler<T>> getTypeHandler(Class<T> typeClass) {
        return getTypeHandler(TypeInfo.of(typeClass));
    }

    /**
     * Retrieves the {@link TypeHandler} for the specified type, if available.
     * <p>
     * Each {@link TypeHandlerFactory} added to this {@link TypeHandlerLibraryImpl} is requested
     * to generate a {@link TypeHandler} for the given type. Most recently added factories are
     * requested first, hence a {@link TypeHandlerFactory} can override one that was added
     * before it.
     *
     * @param type The {@link TypeInfo} describing the type for which to
     *             retrieve the {@link TypeHandler}.
     * @param <T>  The type for which to retrieve the {@link TypeHandler}.
     * @return The {@link TypeHandler} for the specified type, if available.
     */
    @Override
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
     * @param typeInfo The {@link TypeInfo} describing the base type for which to return a
     *                 {@link TypeHandler}.
     * @param <T>      The base type for which to return a {@link TypeHandler}.
     */
    @Override
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
