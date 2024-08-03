// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.registry;

import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.gestalt.util.reflection.ParameterProvider;
import org.terasology.gestalt.util.reflection.SimpleClassFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class InjectionHelper {
    private static final Logger logger = LoggerFactory.getLogger(InjectionHelper.class);

    private InjectionHelper() {
    }

    /**
     * Inject values from this context to annotated fields.
     * <p>
     * The injector looks for fields annotated with {@link In @In} and tries to set them by
     * getting a value by looking up the field's type in the {@code context}.
     * <p>
     * If there is no matching value in the context, the field is silently skipped and left
     * with its previous value.
     *
     * @return the input {@code object}, now with fields set
     */
    public static <T> T inject(final T object, Context context) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
                Object value = context.get(field.getType());
                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                    }
                }
            }

            return null;
        });
        return object;
    }

    /**
     * Inject values from CoreRegistry to annotated fields.
     *
     * @deprecated CoreRegistry-based methods are deprecated in favor of the more thread-safe Context.
     *
     * @see #inject(Object, Context)
     */
    @Deprecated(since = "5.3.0", forRemoval = true)
    public static void inject(final Object object) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
                Object value = CoreRegistry.get(field.getType());
                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                    }
                } else {
                    logger.atWarn().log("{} wanted {} injected but CoreRegistry has none.",
                            object.getClass().getSimpleName(), field.getType().getSimpleName());
                }
            }

            return null;
        });
    }

    public static <T> void inject(final Object object, final Class<? extends Annotation> annotation, final Map<Class<? extends T>, T> source) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(annotation))) {
                Object value = source.get(field.getType());
                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        logger.error("Failed to inject value {} into field {}", value, field, e);
                    }
                } else {
                    logger.error("Failed to inject into field {}, nothing to inject", field);
                }
            }

            return null;
        });
    }

    public static void share(Object object) {
        Share share = object.getClass().getAnnotation(Share.class);
        if (share != null && share.value() != null) {
            for (Class interfaceType : share.value()) {
                CoreRegistry.put(interfaceType, object);
            }
        }
    }

    /**
     * Creates a new instance for a class using constructor injection.
     * The constructor does not need a special annotation for this.
     * Which constructor is selected depends on the following criteria:
     * <ul>
     * <li>The constructor with the most parameters is used.</li>
     * <li>All parameters have to be available in the {@link Context}.</li>
     * <li>If not all parameters can be populated from the Context, the next Constructor with less parameters is used.</li>
     * <li>If no parameters can be populated at all, the default constructor (if available) is used.</li>
     * </ul>
     * @param clazz The class to instantiate.
     * @param context The context to use for injection.
     * @return A new instance of the class to create.
     * @throws NoSuchElementException if the injection failed,
     * e.g. if no parameters were available on the context and a default constructor is missing.
     */
    public static <E> E createWithConstructorInjection(Class<? extends E> clazz, Context context) {
        return safeCreateWithConstructorInjection(clazz, context).get();
    }

    /**
     * Similar to {@link #createWithConstructorInjection(Class, Context)}, but returns
     * {@link Optional#empty()} instead of throwing an exception if the instantiation failed.
     *
     * @param clazz   The class to instantiate.
     * @param context The context to use for injection.
     * @return A new instance of the class to create.
     * @see InjectionHelper#createWithConstructorInjection(Class, Context)
     */
    public static <E> Optional<E> safeCreateWithConstructorInjection(Class<? extends E> clazz, Context context) {
        SimpleClassFactory simpleClassFactory = new SimpleClassFactory(new ParameterProvider() {
            @Override
            public <T> Optional<T> get(Class<T> x) {
                return Optional.ofNullable(context.get(x));
            }
        });
        return simpleClassFactory.instantiateClass(clazz);
    }
}
