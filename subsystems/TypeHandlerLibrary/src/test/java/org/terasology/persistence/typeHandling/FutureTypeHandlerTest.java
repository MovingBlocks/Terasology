// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reflections.Reflections;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FutureTypeHandlerTest {
    private final Reflections reflections = new Reflections(getClass().getClassLoader());

    private final TypeHandlerLibrary typeHandlerLibrary =
            Mockito.spy(new TypeHandlerLibrary(reflections));

    @Test
    public void testRecursiveType() {
        ResultCaptor<Optional<TypeHandler<RecursiveType<Integer>>>> resultCaptor = new ResultCaptor<>();

        doAnswer(resultCaptor).when(typeHandlerLibrary).getTypeHandler(
                eq(new TypeInfo<RecursiveType<Integer>>() { }.getType())
        );

        TypeHandler<RecursiveType<Integer>> typeHandler =
                typeHandlerLibrary.getTypeHandler(
                        new TypeInfo<RecursiveType<Integer>>() { }
                ).get();
        
        verify(typeHandlerLibrary, times(1)).getTypeHandler(
                eq(new TypeInfo<RecursiveType<Integer>>() { }.getType())
        );

        // Optional#get() can throw NoSuchElementException
        TypeHandler possibleFuture = assertDoesNotThrow(()-> resultCaptor.getResult().get());
        assertTrue(possibleFuture instanceof FutureTypeHandler);

        FutureTypeHandler<RecursiveType<Integer>> future =
                (FutureTypeHandler<RecursiveType<Integer>>) possibleFuture;

        assertEquals(typeHandler, future.typeHandler);
    }

    private static final class RecursiveType<T> {
        final T data;
        final List<RecursiveType<T>> children;

        @SafeVarargs
        private RecursiveType(T data, RecursiveType<T>... children) {
            this.data = data;
            this.children = Lists.newArrayList(children);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "SIC_INNER_SHOULD_BE_STATIC",
            justification = "Test code is not performance-relevant, flagged inner ResultCaptor class is a mock with dynamic behavior" +
                    " and cannot be static.")
    private class ResultCaptor<T> implements Answer {
        private T result = null;
        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }
}
