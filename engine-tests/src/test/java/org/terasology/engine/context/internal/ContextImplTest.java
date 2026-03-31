// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.context.internal;

import org.junit.jupiter.api.Test;
import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

import org.terasology.gestalt.di.exceptions.DependencyResolutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    /**
     * When the parent is not a ContextImpl, the child cannot inherit the parent's
     * BeanContext (instanceof check). context.get() should still resolve via the
     * parent fallback, but @Inject resolution should not see parent beans.
     */
    @Test
    public void childContextWithNonContextImplParentFallsBackToGetOnly() {
        Context plainParent = new Context() {
            private final TestService service = new TestService("from-plain-parent");

            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(Class<T> type) {
                if (type == TestService.class) {
                    return (T) service;
                }
                return null;
            }

            @Override
            public <T, U extends T> void put(Class<T> type, U object) {
            }
        };

        ContextImpl child = new ContextImpl(plainParent);

        // get() works via parent fallback
        TestService viaGet = child.get(TestService.class);
        assertNotNull(viaGet, "child.get() should resolve from non-ContextImpl parent via fallback");
        assertEquals("from-plain-parent", viaGet.name);

        // @Inject fails because there is no parent BeanContext to search
        TestConsumer consumer = new TestConsumer();
        assertThrows(DependencyResolutionException.class, () -> child.inject(consumer),
                "inject() should fail for non-ContextImpl parent with no BeanContext hierarchy");
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
