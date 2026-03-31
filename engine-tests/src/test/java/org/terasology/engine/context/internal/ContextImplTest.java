// Copyright 2026 The Terasology Foundation
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
     * A child ContextImpl created with a Context-typed parent should resolve
     * beans registered in the parent via context.get().
     */
    @Test
    public void childContextResolvesParentBeansViaGet() {
        ServiceRegistry parentRegistry = new ServiceRegistry();
        parentRegistry.with(TestService.class).lifetime(Lifetime.Singleton).use(() -> new TestService("from-parent"));
        ContextImpl parent = new ContextImpl(parentRegistry);

        ContextImpl child = new ContextImpl((Context) parent);

        TestService viaGet = child.get(TestService.class);
        assertNotNull(viaGet, "child.get() should resolve parent bean");
        assertEquals("from-parent", viaGet.name);
    }

    /**
     * A child ContextImpl created with a Context-typed parent should resolve
     * @Inject dependencies from the parent's BeanContext hierarchy.
     */
    @Test
    public void childContextResolvesParentBeansViaInject() {
        ServiceRegistry parentRegistry = new ServiceRegistry();
        parentRegistry.with(TestService.class).lifetime(Lifetime.Singleton).use(() -> new TestService("from-parent"));
        ContextImpl parent = new ContextImpl(parentRegistry);

        ContextImpl child = new ContextImpl((Context) parent);

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
