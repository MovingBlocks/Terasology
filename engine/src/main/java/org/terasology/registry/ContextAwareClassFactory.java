// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.registry;

import org.terasology.context.Context;
import org.terasology.util.reflection.ParameterProvider;
import org.terasology.util.reflection.SimpleClassFactory;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ClassFactory which can create with injecting beans and inject it in context.
 * <p>
 * Injects beans from {@link Context} in Constructor.
 * <p>
 * Injects beans in {@link In} fields.
 */
public class ContextAwareClassFactory extends SimpleClassFactory {
    private Context currentContext;

    public ContextAwareClassFactory(ContextParameterProvider provider, Context currentContext) {
        super(provider);
        this.currentContext = currentContext;
    }

    /**
     * Factory-method for creating {@link ContextAwareClassFactory}
     *
     * @param context initial context which will use. can be {@code null}.
     * @return new {@link ContextAwareClassFactory} with context;
     */
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

    /**
     * Creating new Instance of Class.
     * <p>
     * Injecting in constructor. Injecting in {@link In} fields.
     *
     * @param type class for creating
     * @param <T> type of class for creating
     * @return new instance of class
     */
    @Override
    public <T> Optional<T> instantiateClass(Class<? extends T> type) {
        return super.instantiateClass(type).map(inst -> {
            InjectionHelper.inject(inst, currentContext);
            return inst;
        });
    }

    /**
     * Creates instance of {@code type} and inject it in {@link Context} as {@code registerAs} parameter
     * <p>
     * Injects beans in constructor and {@link In} fields.
     *
     * @param type Class for creating
     * @param registerAs type, which be used for resolving by Context.
     * @param <T> type of instance
     * @return Instance of Type
     */
    @SafeVarargs
    public final <T> T createToContext(Class<T> type, Class<? super T>... registerAs) {
        T instance = createWithContext(type);
        if (registerAs.length == 0) {
            getCurrentContext().put(type, instance);
        } else {
            for (Class<? super T> iface : registerAs) {
                getCurrentContext().put(iface, instance);
            }
        }

        return instance;
    }

    /**
     * Injects instance by {@code creator} in {@link Context} as {@code iface} parameter
     * <p>
     * Injects beans in  {@link In} fields.
     *
     * @param iface instance will be presented in {@link Context} as this param
     * @param creator Supplier which provides instance for Injecting. e.g. Builders
     * @param <T> type of instance
     * @return Instance of Type
     */
    public <T> T createToContext(Class<? super T> iface, Supplier<T> creator) {
        T instance = creator.get();
        InjectionHelper.inject(instance, currentContext);
        getCurrentContext().put(iface, instance);
        return instance;
    }

    /**
     * Injects instance by {@code creator} in {@link Context} as {@code iface} parameter
     * <p>
     * Injects beans in  {@link In} fields.
     *
     * @param iface instance will be presented in {@link Context} as this param
     * @param creator Funtion which provides instance for Injecting. e.g. Builders. gives currenctContext.
     * @param <T> type of instance
     * @return Instance of Type
     */
    public <T> T createToContext(Class<T> iface, Function<Context, ? extends T> creator) {
        T instance = creator.apply(getCurrentContext());
        InjectionHelper.inject(instance, currentContext);
        getCurrentContext().put(iface, instance);
        return instance;
    }

    /**
     * Creates instance of {@code type} without inject it in {@link Context}
     * <p>
     * Injects beans in constructor and {@link In} fields.
     *
     * @param type Class for creating
     * @param <T> type of instance
     * @return Instance of Type
     */
    public <T> T createWithContext(Class<T> type) {
        return instantiateClass(type).orElseThrow(() -> new RuntimeException("Cannot create instance of [" + type.getCanonicalName() + "], cannot find dependency beans in context"));
    }

    /**
     * Resolver for Constructor's parameters. Instances takes from {@link ContextAwareClassFactory#getCurrentContext()}
     */
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
