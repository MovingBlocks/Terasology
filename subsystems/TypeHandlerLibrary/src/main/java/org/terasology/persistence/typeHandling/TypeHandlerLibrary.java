// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import org.terasology.persistence.typeHandling.coreTypes.BooleanTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteArrayTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.CharacterTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.DoubleTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.FloatTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.IntTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.LongTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.NumberTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.factories.ArrayTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.EnumTypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.factories.StringMapTypeHandlerFactory;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.metadata.ClassMetadata;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * A library of type handlers. This is used for the construction of class metadata.
 * This library should be initialised by adding a number of base type handlers, describing how to serialize each supported type.
 * It will then produce serializers for classes (through their ClassMetadata) on request.
 */
public interface TypeHandlerLibrary {
    static void populateBuildinHandlers(TypeHandlerLibrary typeHandlerLibrary) {
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
     * Creates a copy of an this serialization library. This copy is initialised with all type handlers that were added to the original, but does not retain any
     * serializers or type handlers that were generated. This can be used to override specific types handlers from another type serializer.
     */
    TypeHandlerLibrary copy();

    Serializer getSerializerFor(ClassMetadata<?, ?> type);

    void addTypeHandlerFactory(TypeHandlerFactory typeHandlerFactory);

    <T> boolean addTypeHandler(Class<T> typeClass, TypeHandler<T> typeHandler);

    <T> boolean addTypeHandler(TypeInfo<T> type, TypeHandler<T> typeHandler);

    <T> void addInstanceCreator(Class<T> typeClass, InstanceCreator<T> instanceCreator);

    <T> void addInstanceCreator(TypeInfo<T> typeInfo, InstanceCreator<T> instanceCreator);

    @SuppressWarnings({"unchecked"})
    Optional<TypeHandler<?>> getTypeHandler(Type type);

    <T> Optional<TypeHandler<T>> getTypeHandler(Class<T> typeClass);

    @SuppressWarnings("unchecked")
    <T> Optional<TypeHandler<T>> getTypeHandler(TypeInfo<T> type);

    <T> TypeHandler<T> getBaseTypeHandler(TypeInfo<T> typeInfo);
}
