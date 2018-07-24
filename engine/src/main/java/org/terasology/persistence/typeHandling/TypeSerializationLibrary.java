/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.math.IntegerRange;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.coreTypes.BooleanTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteArrayTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.DoubleTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.FloatTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.IntTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.LongTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.NumberTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.factories.CollectionTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.EnumTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.MappedContainerTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.StringMapTypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.ColorTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.NameTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.PrefabTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.factories.TextureRegionAssetTypeHandlerFactory;
import org.terasology.persistence.typeHandling.mathTypes.IntegerRangeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Quat4fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Rect2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Rect2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Region3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector2iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector3iTypeHandler;
import org.terasology.persistence.typeHandling.mathTypes.Vector4fTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.asset.UIElement;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A library of type handlers. This is used for the construction of class metadata.
 * This library should be initialised by adding a number of base type handlers, describing how to serialize each supported type.
 * It will then produce serializers for classes (through their ClassMetadata) on request.
 */
public class TypeSerializationLibrary {
    private static final Logger logger = LoggerFactory.getLogger(TypeSerializationLibrary.class);

    private List<TypeHandlerFactory> typeHandlerFactories = Lists.newArrayList();

    private Map<TypeInfo<?>, TypeHandler<?>> typeHandlerCache = Maps.newHashMap();

    private ReflectFactory reflectFactory;
    private CopyStrategyLibrary copyStrategies;

    private Map<ClassMetadata<?, ?>, Serializer> serializerMap = Maps.newHashMap();

    /**
     * @param factory        The factory providing reflect implementation.
     * @param copyStrategies The provider of copy strategies
     */
    public TypeSerializationLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        this.reflectFactory = factory;
        this.copyStrategies = copyStrategies;
        add(Boolean.class, new BooleanTypeHandler());
        add(Boolean.TYPE, new BooleanTypeHandler());
        add(Byte.class, new ByteTypeHandler());
        add(Byte.TYPE, new ByteTypeHandler());
        add(Double.class, new DoubleTypeHandler());
        add(Double.TYPE, new DoubleTypeHandler());
        add(Float.class, new FloatTypeHandler());
        add(Float.TYPE, new FloatTypeHandler());
        add(Integer.class, new IntTypeHandler());
        add(Integer.TYPE, new IntTypeHandler());
        add(Long.class, new LongTypeHandler());
        add(Long.TYPE, new LongTypeHandler());
        add(String.class, new StringTypeHandler());
        add(Number.class, new NumberTypeHandler());
        add(byte[].class, new ByteArrayTypeHandler());

        addTypeHandlerFactory(new EnumTypeHandlerFactory());
        addTypeHandlerFactory(new CollectionTypeHandlerFactory());
        addTypeHandlerFactory(new StringMapTypeHandlerFactory());
        addTypeHandlerFactory(new MappedContainerTypeHandlerFactory(reflectFactory, this.copyStrategies));
    }

    /**
     * Creates a copy of an existing serialization library. This copy is initialised with all type handlers that were added to the original, but does not retain any
     * serializers or type handlers that were generated. This can be used to override specific types handlers from another type serializer.
     *
     * @param original The original type serialization library to copy.
     */
    public TypeSerializationLibrary(TypeSerializationLibrary original) {
        this.reflectFactory = original.reflectFactory;
        this.copyStrategies = original.copyStrategies;
        this.typeHandlerFactories.addAll(original.typeHandlerFactories);
    }

    public static TypeSerializationLibrary createDefaultLibrary(ReflectFactory factory,
                                                                CopyStrategyLibrary copyStrategies) {
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(factory, copyStrategies);

        serializationLibrary.add(Color.class, new ColorTypeHandler());
        serializationLibrary.add(Quat4f.class, new Quat4fTypeHandler());
        // TODO: Add AssetTypeHandlerFactory
        serializationLibrary.add(Texture.class, new AssetTypeHandler<>(Texture.class));
        serializationLibrary.add(UIElement.class, new AssetTypeHandler<>(UIElement.class));
        serializationLibrary.add(Mesh.class, new AssetTypeHandler<>(Mesh.class));
        serializationLibrary.add(StaticSound.class, new AssetTypeHandler<>(StaticSound.class));
        serializationLibrary.add(StreamingSound.class, new AssetTypeHandler<>(StreamingSound.class));
        serializationLibrary.add(Material.class, new AssetTypeHandler<>(Material.class));
        serializationLibrary.add(Name.class, new NameTypeHandler());
        serializationLibrary.add(SkeletalMesh.class, new AssetTypeHandler<>(SkeletalMesh.class));
        serializationLibrary.add(MeshAnimation.class, new AssetTypeHandler<>(MeshAnimation.class));
        serializationLibrary.add(TextureRegion.class, new TextureRegionTypeHandler());

        serializationLibrary.addTypeHandlerFactory(new TextureRegionAssetTypeHandlerFactory());

        serializationLibrary.add(Vector4f.class, new Vector4fTypeHandler());
        serializationLibrary.add(Vector3f.class, new Vector3fTypeHandler());
        serializationLibrary.add(Vector2f.class, new Vector2fTypeHandler());
        serializationLibrary.add(Vector3i.class, new Vector3iTypeHandler());
        serializationLibrary.add(Vector2i.class, new Vector2iTypeHandler());
        serializationLibrary.add(Rect2i.class, new Rect2iTypeHandler());
        serializationLibrary.add(Rect2f.class, new Rect2fTypeHandler());
        serializationLibrary.add(Region3i.class, new Region3iTypeHandler());
        serializationLibrary.add(Prefab.class, new PrefabTypeHandler());
        serializationLibrary.add(BehaviorTree.class, new AssetTypeHandler<>(BehaviorTree.class));
        serializationLibrary.add(IntegerRange.class, new IntegerRangeHandler());

        return serializationLibrary;
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

    public void addTypeHandlerFactory(TypeHandlerFactory typeHandlerFactory) {
        // TODO: Reverse direction of typeHandlerFactories so that later added factories override older ones
        typeHandlerFactories.add(typeHandlerFactory);
    }

    // TODO: Rename to registerTypeHandler
    public <T> void add(Class<T> typeClass, TypeHandler<T> typeHandler) {
        add(TypeInfo.of(typeClass), typeHandler);
    }

    // TODO: Rename to registerTypeHandler
    public <T> void add(TypeInfo<T> type, TypeHandler<T> typeHandler) {
        TypeHandlerFactory factory = new TypeHandlerFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <R> Optional<TypeHandler<R>> create(TypeInfo<R> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
                return typeInfo.equals(type) ? Optional.of((TypeHandler<R>) typeHandler) : Optional.empty();
            }
        };

        addTypeHandlerFactory(factory);
    }

    // TODO: Rename to getTypeHandler
    public TypeHandler<?> getHandlerFor(Type type) {
        return getHandlerFor(TypeInfo.of(type));
    }

    // TODO: Rename to getTypeHandler
    public <T> TypeHandler<T> getHandlerFor(Class<T> typeClass) {
        return getHandlerFor(TypeInfo.of(typeClass));
    }

    // TODO: Rename to getTypeHandler
    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> getHandlerFor(TypeInfo<T> type) {
        if (typeHandlerCache.containsKey(type)) {
            return (TypeHandler<T>) typeHandlerCache.get(type);
        }

        for (TypeHandlerFactory typeHandlerFactory : typeHandlerFactories) {
            Optional<TypeHandler<T>> typeHandler = typeHandlerFactory.create(type, this);

            if (typeHandler.isPresent()) {
                TypeHandler<T> handler = typeHandler.get();
                typeHandlerCache.put(type, handler);
                return handler;
            }
        }

        // TODO: Log error and/or return Optional.empty()
        return null;
    }

    private Map<FieldMetadata<?, ?>, TypeHandler> getFieldHandlerMap(ClassMetadata<?, ?> type) {
        Map<FieldMetadata<?, ?>, TypeHandler> handlerMap = Maps.newHashMap();
        for (FieldMetadata<?, ?> field : type.getFields()) {
            TypeHandler<?> handler = getHandlerFor(field.getField().getGenericType());
            if (handler != null) {
                handlerMap.put(field, handler);
            } else {
                logger.info("Unsupported field: '{}.{}'", type.getUri(), field.getName());
            }
        }
        return handlerMap;
    }
}
