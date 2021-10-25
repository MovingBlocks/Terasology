// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;

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
        Assertions.assertNotEquals(CoreRegistry.get(Context.class), context);
    }

    /**
     * Check if CoreRegistry returns null on its methods when the context is not defined.
     */
    @Test
    public void testNullReturnOnMissingContext() {
        CoreRegistry.setContext(null);

        Assertions.assertEquals(CoreRegistry.put(Integer.class, 10), null);
        Assertions.assertEquals(CoreRegistry.get(Integer.class), null);
    }

    /**
     * Test if the CoreRegistry context is being returned by the get method when the argument is Context.class
     * independently of the Context implementation.
     */
    @Test
    public void testContextGetIndependenceFromContextInterfaceImplementation() {
        Assertions.assertEquals(CoreRegistry.get(Context.class), context);

        Assertions.assertEquals(context.get(Context.class), null);
    }

    /**
     * Check if the CoreRegistry is calling the methods of its Context
     */
    @Test
    public void testContextMethodsCalled() {
        // Load value in context
        Integer value = 10;
        CoreRegistry.put(Integer.class, value);
        Assertions.assertEquals(value, context.get(Integer.class));
        Assertions.assertEquals(context.get(Integer.class), CoreRegistry.get(Integer.class));

        // Change context
        CoreRegistry.setContext(new ContextImplementation());
        Assertions.assertNotEquals(CoreRegistry.get(Context.class), context);
        Assertions.assertEquals(CoreRegistry.get(Integer.class), null);

        // Restore first context
        CoreRegistry.setContext(context);
        Assertions.assertEquals(CoreRegistry.get(Integer.class), value);
    }


}
