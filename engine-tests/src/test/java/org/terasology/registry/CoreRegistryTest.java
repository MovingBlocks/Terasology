// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.registry;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.terasology.engine.TestResourceLocks;
import org.terasology.engine.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ResourceLock(TestResourceLocks.CORE_REGISTRY)
public class CoreRegistryTest {
    private Context context;

    /**
     * Create a Context implementation instance an assign it to CoreRegistry before testing.
     */
    @BeforeEach
    public void setup() {
        context = new ContextImplementation();
        CoreRegistry.setContext(context);
    }

    /**
     * Check if the context is changed with setContext method.
     */
    @Test
    public void testContextChange() {
        CoreRegistry.setContext(new ContextImplementation());
        assertNotEquals(CoreRegistry.get(Context.class), context);
    }

    /**
     * Check if CoreRegistry returns null on its methods when the context is not defined.
     */
    @Test
    public void testNullReturnOnMissingContext() {
        CoreRegistry.setContext(null);

        assertEquals(CoreRegistry.put(Integer.class, 10), null);
        assertEquals(CoreRegistry.get(Integer.class), null);
    }

    /**
     * Test if the CoreRegistry context is being returned by the get method when the argument is Context.class
     * independently of the Context implementation.
     */
    @Test
    public void testContextGetIndependenceFromContextInterfaceImplementation() {
        assertEquals(CoreRegistry.get(Context.class), context);

        assertEquals(context.get(Context.class), null);
    }

    /**
     * Check if the CoreRegistry is calling the methods of its Context
     */
    @Test
    public void testContextMethodsCalled() {
        // Load value in context
        Integer value = 10;
        CoreRegistry.put(Integer.class, value);
        assertEquals(value, context.get(Integer.class));
        assertEquals(context.get(Integer.class), CoreRegistry.get(Integer.class));

        // Change context
        CoreRegistry.setContext(new ContextImplementation());
        assertNotEquals(CoreRegistry.get(Context.class), context);
        assertEquals(CoreRegistry.get(Integer.class), null);

        // Restore first context
        CoreRegistry.setContext(context);
        assertEquals(CoreRegistry.get(Integer.class), value);
    }


    private static class ContextImplementation implements Context {
        private final Map<Class<?>, Object> map = Maps.newConcurrentMap();

        @Override
        public <T> T get(Class<? extends T> type) {
            T result = type.cast(map.get(type));
            if (result != null) {
                return result;
            }
            return null;
        }

        @Override
        public <T, U extends T> void put(Class<T> type, U object) {
            map.put(type, object);
        }
    }
}
