// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.registry;

import org.terasology.context.Context;
import org.terasology.util.reflection.ParameterProvider;
import org.terasology.util.reflection.SimpleClassFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class ContextAwareClassFactory extends SimpleClassFactory {
    private Context currentContext;

    public ContextAwareClassFactory(ContextParameterProvider provider, Context currentContext) {
        super(provider);
        this.currentContext = currentContext;
    }

    public static ContextAwareClassFactory create(Context context) {
        ContextParameterProvider provider = new ContextParameterProvider();
        ContextAwareClassFactory classFactory = new ContextAwareClassFactory(provider, context);
        provider.setFactory(classFactory);
        return classFactory;
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    @Override
    public <T> Optional<T> instantiateClass(Class<? extends T> type) {
        return super.instantiateClass(type).map(inst -> {
            InjectionHelper.inject(inst, currentContext);
            return inst;
        });
    }

    public <T> T createInjectableInstance(Class<T> type) {
        return createInjectableInstance(type, type);
    }

    public <T, R extends T> T createInjectableInstance(Class<T> iface, Class<R> type) {
        T instance = createWithContext(type);
        getCurrentContext().put(iface, instance);
        return instance;
    }

    public <T, R extends T> T createInjectable(Class<T> iface, Supplier<R> creator) {
        T instance = creator.get();
        InjectionHelper.inject(instance, currentContext);
        getCurrentContext().put(iface, instance);
        return instance;
    }

    public <T> T createWithContext(Class<T> type) {
        return instantiateClass(type).get();
    }

    private static class ContextParameterProvider implements ParameterProvider {
        private ContextAwareClassFactory factory;

        public ContextAwareClassFactory getFactory() {
            return factory;
        }

        public void setFactory(ContextAwareClassFactory factory) {
            this.factory = factory;
        }

        @Override
        public <T> Optional<T> get(Class<T> type) {
            return Optional.ofNullable(factory.getCurrentContext().get(type));
        }
    }
}
