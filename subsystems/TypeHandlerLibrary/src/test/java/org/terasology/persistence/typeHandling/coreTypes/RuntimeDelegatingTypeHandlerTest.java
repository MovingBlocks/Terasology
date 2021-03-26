// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.inMemory.AbstractPersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedMap;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;
import org.terasology.persistence.typeHandling.reflection.ReflectionsSandbox;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuntimeDelegatingTypeHandlerTest {
    private final TypeHandlerLibrary typeHandlerLibrary;

    private final TypeHandler<Base> baseTypeHandler;
    private final TypeHandler<Sub> subTypeHandler;
    private final Class<Sub> subType = Sub.class;
    private final Class<Base> baseType = Base.class;
    private final RuntimeDelegatingTypeHandler<Base> runtimeDelegatingTypeHandler;


    RuntimeDelegatingTypeHandlerTest(@Mock TypeHandler<Base> baseTypeHandler, @Mock TypeHandler<Sub> subTypeHandler) {
        this.baseTypeHandler = baseTypeHandler;
        this.subTypeHandler = subTypeHandler;
        configureMockSerializer(baseTypeHandler);
        configureMockSerializer(subTypeHandler);

        Reflections reflections = new Reflections(getClass().getClassLoader());
        typeHandlerLibrary = new TypeHandlerLibrary(reflections);

        typeHandlerLibrary.addTypeHandler(Base.class, baseTypeHandler);
        typeHandlerLibrary.addTypeHandler(Sub.class, subTypeHandler);

        TypeHandlerContext context = new TypeHandlerContext(typeHandlerLibrary, new ReflectionsSandbox(reflections));
        runtimeDelegatingTypeHandler = new RuntimeDelegatingTypeHandler<>(
                baseTypeHandler, TypeInfo.of(Base.class), context);
    }

    private static void configureMockSerializer(TypeHandler<?> mocked) {
        when(mocked.serialize(any(), any())).thenReturn(new AbstractPersistedData() {
            @Override
            public boolean isNull() {
                return true;
            }
        });
    }

    @Test
    void testSerializeBase() {
        PersistedDataSerializer serializer = mock(PersistedDataSerializer.class);
//        when(serializer.serialize(any(String.class)))
//                .then(invocation -> new PersistedString((String) invocation.getArguments()[0]));

        Base base = new Base();
        runtimeDelegatingTypeHandler.serialize(base, serializer);

        verify(baseTypeHandler).serialize(any(), any());
        verify(subTypeHandler, never()).serialize(any(), any());

        verify(serializer, never()).serialize(
                argThat((ArgumentMatcher<Map<String, PersistedData>>) argument -> argument.containsKey(RuntimeDelegatingTypeHandler.TYPE_FIELD))
        );
    }

    @Test
    void testSerializeSub() {
        PersistedDataSerializer serializer = mock(PersistedDataSerializer.class);
        when(serializer.serialize(any(String.class)))
                .then(invocation -> new PersistedString((String) invocation.getArguments()[0]));

        Base sub = new Sub();
        runtimeDelegatingTypeHandler.serialize(sub, serializer);

        verify(subTypeHandler).serialize(any(), any());

        verify(serializer).serialize(
                argThat((ArgumentMatcher<Map<String, PersistedData>>) argument -> argument.get(RuntimeDelegatingTypeHandler.TYPE_FIELD)
                        .getAsString()
                        .equals(subType.getName())
                        && argument.containsKey(RuntimeDelegatingTypeHandler.VALUE_FIELD))
        );
    }

    @Test
    void testDeserializeBase() {
        PersistedData persistedBase = new PersistedMap(ImmutableMap.of());

        runtimeDelegatingTypeHandler.deserialize(persistedBase);

        verify(baseTypeHandler).deserialize(any());
        verify(subTypeHandler, never()).deserialize(any());
    }

    @Test
    void testDeserializeSub() {
        PersistedData persistedSub = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(subType.getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        Optional<Base> result = runtimeDelegatingTypeHandler.deserialize(persistedSub);

        assertTrue(result.isPresent());

        verify(typeHandlerLibrary, never()).getTypeHandler(eq(baseType));
        verify(typeHandlerLibrary).getTypeHandler(eq((Type) subType));

        verify(baseTypeHandler, never()).deserialize(any());
        verify(subTypeHandler).deserialize(any());
    }

    @Test
    void testDeserializeNonSub() {
        PersistedData persistedData = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(Integer.class.getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        Optional<Base> deserialized = runtimeDelegatingTypeHandler.deserialize(persistedData);

        assertFalse(deserialized.isPresent());

        verify(subTypeHandler, never()).deserialize(any());
        // Serializes using base type handler
        verify(baseTypeHandler).deserialize(any());
    }

    private static class Base {
        @SuppressWarnings("unused")
        int x;
    }

    private static class Sub extends Base {
        @SuppressWarnings("unused")
        float y;
    }
}
