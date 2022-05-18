// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import org.terasology.engine.context.Context;

import java.lang.invoke.SerializedLambda;
import java.util.List;

import static com.google.common.base.Verify.verify;

public class InvokingContext implements Context {

    private final Context inner;

    public InvokingContext(Context inner) {
        this.inner = inner;
    }

    public <T, R> R invoke(InvokingHelpers.SerializableFunction<T, R> func) {
        SerializedLambda serializedLambda = InvokingHelpers.getSerializedLambdaUnchecked(func);

        List<Class<?>> params = InvokingHelpers.getLambdaParameters(serializedLambda);
        verify(params.size() == 1, "Expected exactly one parameter, found %s", params);

        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) params.get(0);
        return func.apply(inner.getValue(clazz));
    }

    public <T, U, R> R invoke(InvokingHelpers.SerializableBiFunction<T, U, R> func) {
        // For small numbers of args, we could write out `f.apply(x1, …, xN)` by hand.
        // But to generalize, we can use a MethodHandle.
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func),
                inner::getValue);
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
