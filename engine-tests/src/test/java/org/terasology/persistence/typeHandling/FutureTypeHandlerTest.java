/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FutureTypeHandlerTest {
    private final Reflections reflections = new Reflections(getClass().getClassLoader());

    private final TypeHandlerLibrary typeHandlerLibrary =
            spy(TypeHandlerLibrary.withReflections(reflections));

    private static class RecursiveType<T> {
        final T data;
        final List<RecursiveType<T>> children;

        @SafeVarargs
        private RecursiveType(T data, RecursiveType<T>... children) {
            this.data = data;
            this.children = Lists.newArrayList(children);
        }
    }

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

    @Test
    public void testRecursiveType() {
        ResultCaptor<Optional<TypeHandler<RecursiveType<Integer>>>> resultCaptor = new ResultCaptor<>();

        doAnswer(resultCaptor).when(typeHandlerLibrary).getTypeHandler(
                eq(new TypeInfo<RecursiveType<Integer>>() {}.getType())
        );

        TypeHandler<RecursiveType<Integer>> typeHandler =
                typeHandlerLibrary.getTypeHandler(
                        new TypeInfo<RecursiveType<Integer>>() {}
                ).get();
        
        verify(typeHandlerLibrary, times(1)).getTypeHandler(
                eq(new TypeInfo<RecursiveType<Integer>>() {}.getType())
        );

        // Optional#get() can throw NoSuchElementException
        TypeHandler possibleFuture = assertDoesNotThrow(()-> resultCaptor.getResult().get());
        assertTrue(possibleFuture instanceof FutureTypeHandler);

        FutureTypeHandler<RecursiveType<Integer>> future =
                (FutureTypeHandler<RecursiveType<Integer>>) possibleFuture;

        assertEquals(typeHandler, future.typeHandler);
    }
}
