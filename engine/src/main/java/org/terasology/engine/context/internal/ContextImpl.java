// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.context.internal;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import org.terasology.context.Lifetime;
import org.terasology.context.exception.BeanNotFoundException;
import org.terasology.engine.context.Context;
import org.terasology.gestalt.di.BeanContext;
import org.terasology.gestalt.di.DefaultBeanContext;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.di.exceptions.BeanResolutionException;

import java.util.Map;

/**
 * Implements the {@link Context} interface.
 */
public class ContextImpl implements Context {
    private final DefaultBeanContext beanContext;
    private final Context parent;

    private final Map<Class<?>, Object> map = Maps.newConcurrentMap();

    public ContextImpl(Context parent, ServiceRegistry... registries) {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        for (ServiceRegistry registry : registries) {
            thisServiceRegistry.includeRegistry(registry);
        }
        this.beanContext = new DefaultBeanContext(
                parent instanceof ContextImpl ? ((ContextImpl) parent).beanContext : null, thisServiceRegistry);
        this.parent = parent;
    }

    public ContextImpl(ContextImpl parent, ServiceRegistry... registries) {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        for (ServiceRegistry registry : registries) {
            thisServiceRegistry.includeRegistry(registry);
        }
        this.beanContext = new DefaultBeanContext(parent.beanContext, thisServiceRegistry);
        this.parent = parent;
    }

    public ContextImpl(Context parent, BeanContext parentBeanContext) {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        this.beanContext = new DefaultBeanContext(parentBeanContext, thisServiceRegistry);
        this.parent = parent;
    }

    public ContextImpl(ServiceRegistry... registries) {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        for (ServiceRegistry registry : registries) {
            thisServiceRegistry.includeRegistry(registry);
        }
        this.beanContext = new DefaultBeanContext(thisServiceRegistry);
        this.parent = null;
    }

    /**
     *
     * @param parent can be null. If not null it will be used as a fallback if this context does not contain a
     *               requested object.
     */
    public ContextImpl(Context parent) {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        this.beanContext = new DefaultBeanContext(
                parent instanceof ContextImpl ? ((ContextImpl) parent).beanContext : null, thisServiceRegistry);
        this.parent = parent;
    }

    /**
     *
     * @param parent can be null. If not null it will be used as a fallback if this context does not contain a
     *               requested object.
     */
    public ContextImpl(ContextImpl parent) {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        this.beanContext = new DefaultBeanContext(parent.beanContext, thisServiceRegistry);
        this.parent = parent;
    }

    public ContextImpl() {
        ServiceRegistry thisServiceRegistry = new ServiceRegistry();
        thisServiceRegistry.with(Context.class).lifetime(Lifetime.Singleton).use(() -> this);
        thisServiceRegistry.with(ContextImpl.class).lifetime(Lifetime.Singleton).use(() -> this);
        this.beanContext = new DefaultBeanContext(thisServiceRegistry);
        this.parent = null;
    }

    @Override
    public <T> T get(Class<T> type) {
        if (type == Context.class) {
            return type.cast(this);
        }
        T result = type.cast(map.get(type));
        if (result != null) {
            return result;
        }
        try {
            return beanContext.getBean(type);
        } catch (BeanResolutionException | BeanNotFoundException ignore) {
            // Ignored - fallback to parent if present.
            if (parent != null) {
                return parent.get(type);
            } else {
                return null;
            }
        }
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object)  {
        map.put(type, object);
    }

    public <T> T inject(T instance) {
        return beanContext.inject(instance);
    }

    public boolean isDirectDescendantOf(Context other) {
        return parent == other;
    }

    public DefaultBeanContext getBeanContext() {
        return beanContext;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("#", Integer.toHexString(hashCode()))
                .add("parent", parent)
                .toString();
    }
}
