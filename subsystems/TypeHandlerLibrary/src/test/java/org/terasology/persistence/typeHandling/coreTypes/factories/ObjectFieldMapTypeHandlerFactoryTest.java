// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ObjectFieldMapTypeHandlerFactoryTest {
    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);

    private final ConstructorLibrary constructorLibrary = new ConstructorLibrary(Maps.newHashMap());
    private final ObjectFieldMapTypeHandlerFactory typeHandlerFactory = new ObjectFieldMapTypeHandlerFactory(
            constructorLibrary);

    private final TypeHandlerContext context =
            new TypeHandlerContext(typeHandlerLibrary, mock(SerializationSandbox.class));

    @Test
    @DisplayName("Test that type handler is correctly created via type handler factory")
    public void testSingleTypeClassTypeHandlerCreationViaFactory() {
        Optional<TypeHandler<SingleTypeClass<Integer>>> typeHandler =
                typeHandlerFactory.create(new TypeInfo<SingleTypeClass<Integer>>() { }, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ObjectFieldMapTypeHandler);
    }

    @Test
    @DisplayName("Test that type handler is correctly created via type handler factory")
    public void testMultiTypeClassTypeHandlerCreationViaFactory() {
        Optional<TypeHandler<MultiTypeClass<Integer, List<Integer>>>> typeHandler =
                typeHandlerFactory.create(new TypeInfo<MultiTypeClass<Integer, List<Integer>>>() { }, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ObjectFieldMapTypeHandler);
    }

    @Test
    @DisplayName("Test that type handler is correctly created via type handler factory")
    public void testMixedVisibilityTypeClassTypeHandlerCreationViaFactory() {
        Optional<TypeHandler<PublicPrivateMixedClass<Integer, List<Integer>>>> typeHandler =
                typeHandlerFactory.create(new TypeInfo<PublicPrivateMixedClass<Integer, List<Integer>>>() { }, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ObjectFieldMapTypeHandler);
    }

    @Test
    @DisplayName("Test implicit type handler loading for plain old java objects (pojo)")
    public void testPojo() {
        typeHandlerFactory.create(new TypeInfo<SingleTypeClass<Integer>>() { }, context);

        // Verify that the Integer TypeHandler loading was called on the TypeHandlerLibrary
        verify(typeHandlerLibrary, times(1)).getTypeHandler((Type) any());
        verify(typeHandlerLibrary, times(1)).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
        verify(typeHandlerLibrary, never()).getTypeHandler((Class<Object>) any());
        verify(typeHandlerLibrary, never()).getTypeHandler((TypeInfo<Object>) any());
    }

    @Test
    @DisplayName("Test implicit type handler loading for list collection objects")
    public void testListCollection() {
        typeHandlerFactory.create(new TypeInfo<SingleTypeClass<List<Integer>>>() { }, context);

        // Verify that the List<Integer> TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary, times(1)).getTypeHandler((Type) any());
        verify(typeHandlerLibrary, times(1)).getTypeHandler(eq(new TypeInfo<List<Integer>>() { }.getType()));
        verify(typeHandlerLibrary, never()).getTypeHandler((Class<Object>) any());
        verify(typeHandlerLibrary, never()).getTypeHandler((TypeInfo<Object>) any());
    }

    @Test
    @DisplayName("Test implicit type handler loading for multiple objects of same type")
    public void testMultipleObjectsOfSameType() {
        typeHandlerFactory.create(new TypeInfo<MultiTypeClass<Integer, Integer>>() { }, context);

        // Verify that the Integer TypeHandler loading was called on the TypeHandlerLibrary
        verify(typeHandlerLibrary, times(2)).getTypeHandler((Type) any());
        verify(typeHandlerLibrary, times(2)).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
        verify(typeHandlerLibrary, never()).getTypeHandler((Class<Object>) any());
        verify(typeHandlerLibrary, never()).getTypeHandler((TypeInfo<Object>) any());
    }

    @Test
    @DisplayName("Test implicit type handler loading for multiple objects of same type but differing visibility")
    public void testMixedVisibilityMultipleObjectsOfSameType() {
        typeHandlerFactory.create(new TypeInfo<PublicPrivateMixedClass<Integer, Integer>>() { }, context);

        // Verify that the Integer TypeHandler loading was called on the TypeHandlerLibrary
        verify(typeHandlerLibrary, times(1)).getTypeHandler((Type) any());
        verify(typeHandlerLibrary, times(1)).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
        verify(typeHandlerLibrary, never()).getTypeHandler((Class<Object>) any());
        verify(typeHandlerLibrary, never()).getTypeHandler((TypeInfo<Object>) any());
    }

    @Test
    @DisplayName("Test implicit type handler loading for multiple objects of different type")
    public void testMultipleObjectsOfDifferentType() {
        typeHandlerFactory.create(new TypeInfo<MultiTypeClass<Integer, List<Integer>>>() { }, context);

        // Verify that the Integer TypeHandler loading was called on the TypeHandlerLibrary
        verify(typeHandlerLibrary, times(2)).getTypeHandler((Type) any());
        verify(typeHandlerLibrary, times(1)).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
        verify(typeHandlerLibrary, times(1)).getTypeHandler(eq(new TypeInfo<List<Integer>>() { }.getType()));
        verify(typeHandlerLibrary, never()).getTypeHandler((Class<Object>) any());
        verify(typeHandlerLibrary, never()).getTypeHandler((TypeInfo<Object>) any());
    }

    private static class SingleTypeClass<T> {
        public T t;
    }

    private static class MultiTypeClass<T, U> {
        public T t;
        public U u;
    }

    private static class PublicPrivateMixedClass<T, U> {
        public T t;
        private U u;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "UUF_UNUSED_FIELD",
            justification = "Direct member access is not expected. TypeHandler factory dynamically loads type handlers on type handler" +
                    " creation based on member types of input class TypeInfo. ")
    @SuppressWarnings("PMD.UnusedPrivateField")
    private static class SomeClass<T> {
        public T t;
        public List<T> list;
    }
}
