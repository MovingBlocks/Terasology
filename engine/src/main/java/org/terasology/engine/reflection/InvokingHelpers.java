// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import com.google.common.base.Throwables;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InvokingHelpers {
    private InvokingHelpers() { }

    /**
     * This relies on {@link Method#setAccessible} on a private final field.
     * <p>
     * Is that bad? You might think so, but {@link Serializable} documentation says that
     * Serializable objects may have that method <em>even though</em> it is not defined in
     * the interface. So it's probably okay?
     * <p>
     * <b>Alternatives?</b>
     * <p>
     * Are there APIs to get the replaced-for-serialization object without trying to do
     * do {@link Method#setAccessible} on arbitrary objects?
     * <p>
     * We could do {@link java.io.ObjectStreamClass#lookup(Class) ObjectStreamClass.lookup},
     * that's the model that {@link ObjectOutputStream} uses. It has useful methods like
     * {@code hasWriteReplaceMethod} and {@code invokeWriteReplace}. Unfortunately, those
     * methods aren't public, so they're no help if we're trying to avoid hacking around
     * visibility restrictions.
     * <p>
     * It is also possible to run the object through an {@link ObjectOutputStream} and
     * <a href="https://gist.github.com/keturn/180a57f2f6069470556137bd06b4025d">capture the result</a>.
     * That's worth trying if this breaks, but otherwise it's a lot of extra hassle.
     */
    public static SerializedLambda getSerializedLambda(Serializable obj) throws IllegalAccessException {
        Method writeReplace;
        try {
            writeReplace = obj.getClass().getDeclaredMethod("writeReplace");
            Object replacement = AccessController.doPrivileged((PrivilegedExceptionAction<?>) () -> {
                writeReplace.setAccessible(true);
                return writeReplace.invoke(obj);
            });
            return (SerializedLambda) replacement;
        } catch (PrivilegedActionException | NoSuchMethodException e) {
            Throwables.throwIfUnchecked(e.getCause());
            Throwables.throwIfInstanceOf(e.getCause(), IllegalAccessException.class);
            throw new RuntimeException(e);
        }
    }

    public static SerializedLambda getSerializedLambdaUnchecked(Serializable obj) {
        try {
            return getSerializedLambda(obj);
        } catch (IllegalAccessException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public static List<Class<?>> getLambdaParameters(SerializedLambda serializedLambda) {
        var methodType = MethodType.fromMethodDescriptorString(
                serializedLambda.getImplMethodSignature(),
                serializedLambda.getClass().getClassLoader()
        );
        return methodType.parameterList();
    }

    public static <R> R invokeProvidingParametersByType(SerializedLambda serializedLambda, Function<Class<?>, ?> provider) {
        // For small numbers of args, we could write out `f.apply(x1, …, xN)` by hand.
        // But to generalize, we can use a MethodHandle.
        MethodHandle mh;
        try {
            mh = MethodHandleAdapters.ofLambda(serializedLambda);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        var args = Arrays.stream(mh.type().parameterArray())
                .map(provider)
                .collect(Collectors.toUnmodifiableList());

        try {
            @SuppressWarnings("unchecked") R result = (R) mh.invokeWithArguments(args);
            return result;
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    interface SerializableFunction<T, R> extends Function<T, R>, Serializable { }

    interface SerializableBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable { }

    interface SerializableFunction3<T1, T2, T3, R> extends Serializable, 
            reactor.function.Function3<T1, T2, T3, R> { }

    interface SerializableFunction4<T1, T2, T3, T4, R> extends Serializable, 
            reactor.function.Function4<T1, T2, T3, T4, R> { }

    interface SerializableFunction5<T1, T2, T3, T4, T5, R> extends Serializable,
            reactor.function.Function5<T1, T2, T3, T4, T5, R> { }

    interface SerializableFunction6<T1, T2, T3, T4, T5, T6, R> extends Serializable,
            reactor.function.Function6<T1, T2, T3, T4, T5, T6, R> { }

    interface SerializableFunction7<T1, T2, T3, T4, T5, T6, T7, R> extends Serializable,
            reactor.function.Function7<T1, T2, T3, T4, T5, T6, T7, R> { }

    interface SerializableFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> extends Serializable,
            reactor.function.Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> { }
}
