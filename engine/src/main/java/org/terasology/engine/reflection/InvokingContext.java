// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import com.google.common.base.Throwables;
import org.terasology.engine.context.Context;
import reactor.core.publisher.Flux;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.SerializedLambda;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Verify.verify;

public class InvokingContext implements Context {

    private final Context inner;

    public InvokingContext(Context inner) {
        this.inner = inner;
    }

    @SafeVarargs  // SafeVarargs requires `final`
    final <T, R> R invoke(Function<T, R> func, T... ts) {
        var clazz = InvokingHelpers.argType(func, ts);
        return func.apply(inner.getValue(clazz));
    }

    <T, R> R invokeS(InvokingHelpers.SerializableFunction<T, R> func) {
        SerializedLambda serializedLambda = InvokeWriteReplace.getSerializedLambdaUnchecked(func);

        List<Class<?>> params = InvokingHelpers.getLambdaParameters(serializedLambda);
        verify(params.size() == 1, "Expected exactly one parameter, found %s", params);

        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) params.get(0);
        return func.apply(inner.getValue(clazz));
    }

    <T, U, R> R invokeS(InvokingHelpers.SerializableBiFunction<T, U, R> func) {
        return invokeSerializedLambda(InvokeWriteReplace.getSerializedLambdaUnchecked(func));
    }

    <R> R invokeSerializedLambda(SerializedLambda serializedLambda) {
        // For small numbers of args, we could write out `f.apply(x1, …, xN)` by hand.
        // But to generalize, we can use a MethodHandle.
        MethodHandle mh;
        try {
            mh = MethodHandleAdapters.ofLambda(serializedLambda);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        var args = Flux.fromArray(mh.type().parameterArray())
                .map(inner::getValue)
                .collectList().block();

        try {
            @SuppressWarnings("unchecked") R result = (R) mh.invokeWithArguments(args);
            return result;
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    /* *** Delegate to wrapped Context *** */

    @Override
    public <T> T get(Class<T> type) {
        return inner.get(type);
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object) {
        inner.put(type, object);
    }
}
