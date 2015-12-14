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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.math.IntegerRange;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.coreTypes.BooleanTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.DoubleTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.FloatTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.IntTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ListTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.LongTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.MappedContainerTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.NumberTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.SetTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.ColorTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.NameTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.PrefabTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.TextureRegionTypeHandler;
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
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.utilities.ReflectionUtil;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A library of type handlers. This is used for the construction of class metadata.
 * This library should be initialised by adding a number of base type handlers, describing how to serialize each supported type.
 * It will then produce serializers for classes (through their ClassMetadata) on request.
 *
 */
public class TypeSerializationLibrary {
    private static final Logger logger = LoggerFactory.getLogger(TypeSerializationLibrary.class);

    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();
    private Set<Class<?>> coreTypeHandlers = Sets.newHashSet();
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
        for (Class<?> type : original.coreTypeHandlers) {
            typeHandlers.put(type, original.typeHandlers.get(type));
            coreTypeHandlers.add(type);
        }
    }

    public static TypeSerializationLibrary createDefaultLibrary(ReflectFactory factory,
                                                                CopyStrategyLibrary copyStrategies) {
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(factory, copyStrategies);
        serializationLibrary.add(Color.class, new ColorTypeHandler());
        serializationLibrary.add(Quat4f.class, new Quat4fTypeHandler());
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
        serializationLibrary.add(TextureRegionAsset.class, new TextureRegionTypeHandler());
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

    /**
     * Adds a type handler that will be to serialize a specified type.
     * If a type handler was previously registered for that type, it will be replaced with the new type handler.
     * Existing serializers will not be updated.
     *
     * @param type    The type to handle.
     * @param handler The TypeHandler
     * @param <T>     The type to handle.
     */
    public <T> void add(Class<T> type, TypeHandler<? super T> handler) {
        typeHandlers.put(type, handler);
        coreTypeHandlers.add(type);
    }

    public ImmutableSet<Class<?>> getCoreTypes() {
        return ImmutableSet.copyOf(coreTypeHandlers);
    }

    public void clear() {
        typeHandlers.clear();
        coreTypeHandlers.clear();
    }

    // TODO: Refactor
    @SuppressWarnings("unchecked")
    public TypeHandler<?> getHandlerFor(Type genericType) {
        Class<?> typeClass = ReflectionUtil.getClassOfType(genericType);
        if (typeClass == null) {
            logger.error("Unabled to get class from type {}", genericType);
            return null;
        }

        if (Enum.class.isAssignableFrom(typeClass)) {
            return new EnumTypeHandler(typeClass);
        } else if (List.class.isAssignableFrom(typeClass)) {
            // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
            Type parameter = ReflectionUtil.getTypeParameter(genericType, 0);
            if (parameter != null) {
                TypeHandler<?> innerHandler = getHandlerFor(parameter);
                if (innerHandler != null) {
                    return new ListTypeHandler<>(innerHandler);
                }
            }
            logger.error("List field is not parametrized, or holds unsupported type");
            return null;

        } else if (Set.class.isAssignableFrom(typeClass)) {
            // For sets:
            Type parameter = ReflectionUtil.getTypeParameter(genericType, 0);
            if (parameter != null) {
                TypeHandler<?> innerHandler = getHandlerFor(parameter);
                if (innerHandler != null) {
                    return new SetTypeHandler<>(innerHandler);
                }
            }
            logger.error("Set field is not parametrized, or holds unsupported type");
            return null;

        } else if (Map.class.isAssignableFrom(typeClass)) {
            // For Maps, createEntityRef the handler for the value type (and maybe key too?)
            Type keyParameter = ReflectionUtil.getTypeParameter(genericType, 0);
            Type contentsParameter = ReflectionUtil.getTypeParameter(genericType, 1);
            if (keyParameter != null && contentsParameter != null && String.class == keyParameter) {
                TypeHandler<?> valueHandler = getHandlerFor(contentsParameter);
                if (valueHandler != null) {
                    return new StringMapTypeHandler<>(valueHandler);
                }
            }
            logger.error("Map field is not parametrized, does not have a String key, or holds unsupported values");

        } else if (typeHandlers.containsKey(typeClass)) {
            // For known types, just use the handler
            return typeHandlers.get(typeClass);

        } else if (typeClass.getAnnotation(MappedContainer.class) != null
                && !Modifier.isAbstract(typeClass.getModifiers())
                && !typeClass.isLocalClass()
                && !(typeClass.isMemberClass()
                && !Modifier.isStatic(typeClass.getModifiers()))) {
            try {
                ClassMetadata<?, ?> metadata = new DefaultClassMetadata<>(new SimpleUri(), typeClass, reflectFactory, copyStrategies);
                MappedContainerTypeHandler<?> mappedHandler = new MappedContainerTypeHandler(typeClass, getFieldHandlerMap(metadata));
                typeHandlers.put(typeClass, mappedHandler);
                return mappedHandler;
            } catch (NoSuchMethodException e) {
                logger.error("Unable to register field of type {}: no publicly accessible default constructor", typeClass.getSimpleName());
                return null;
            }
        } else {
            logger.error("Unable to register field of type {}: not a supported type or MappedContainer", typeClass.getSimpleName());
        }

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
