// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.context.internal;

import org.junit.jupiter.api.Test;
import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link ContextImpl}, focusing on parent-child bean resolution.
 */
public class ContextImplTest {

    /**
     * A child ContextImpl created with a Context-typed parent should still
     * be able to resolve beans registered in the parent via the BeanContext.
     * <p>
     * Before the fix, the ContextImpl(Context) constructor created a standalone
     * DefaultBeanContext with no parent, so @Inject resolution could not see
     * parent beans. Only context.get() worked (via the fallback to parent.get()).
     */
    @Test
    public void childContextResolvesParentBeansViaGet() {
        // Register a bean in the parent context
        ServiceRegistry parentRegistry = new ServiceRegistry();
        parentRegistry.with(TestService.class).lifetime(Lifetime.Singleton).use(() -> new TestService("from-parent"));
        ContextImpl parent = new ContextImpl(parentRegistry);

        // Create child using the Context-typed constructor (the one that was broken)
        Context parentAsContext = parent; // upcast to Context to use the problematic constructor
        ContextImpl child = new ContextImpl(parentAsContext);

        // context.get() should resolve from parent (this always worked via fallback)
        TestService viaGet = child.get(TestService.class);
        assertNotNull(viaGet, "child.get() should resolve parent bean");
        assertEquals("from-parent", viaGet.name);
    }

    @Test
    public void childContextResolvesParentBeansViaInject() {
        // Register a bean in the parent context
        ServiceRegistry parentRegistry = new ServiceRegistry();
        parentRegistry.with(TestService.class).lifetime(Lifetime.Singleton).use(() -> new TestService("from-parent"));
        ContextImpl parent = new ContextImpl(parentRegistry);

        // Create child using the Context-typed constructor
        Context parentAsContext = parent;
        ContextImpl child = new ContextImpl(parentAsContext);

        // inject() should resolve @Inject fields from the parent BeanContext
        // Before the fix, this would leave the field null because the child's
        // DefaultBeanContext had no parent to search.
        TestConsumer consumer = new TestConsumer();
        child.inject(consumer);
        assertNotNull(consumer.service, "inject() should resolve parent bean via BeanContext hierarchy");
        assertEquals("from-parent", consumer.service.name);
    }

    @Test
    public void childContextOverridesParentBean() {
        ServiceRegistry parentRegistry = new ServiceRegistry();
        parentRegistry.with(TestService.class).lifetime(Lifetime.Singleton).use(() -> new TestService("from-parent"));
        ContextImpl parent = new ContextImpl(parentRegistry);

        ServiceRegistry childRegistry = new ServiceRegistry();
        childRegistry.with(TestService.class).lifetime(Lifetime.Singleton).use(() -> new TestService("from-child"));
        ContextImpl child = new ContextImpl((Context) parent, childRegistry);

        // Child's own bean should take priority
        TestService viaGet = child.get(TestService.class);
        assertNotNull(viaGet);
        assertEquals("from-child", viaGet.name);
    }

    @Test
    public void childContextWithNullParentDoesNotFail() {
        ContextImpl child = new ContextImpl((Context) null);

        // Should return null, not throw
        assertNull(child.get(TestService.class));
    }

    public static class TestService {
        public final String name;

        public TestService(String name) {
            this.name = name;
        }
    }

    public static class TestConsumer {
        @Inject
        public TestService service;
    }
}
