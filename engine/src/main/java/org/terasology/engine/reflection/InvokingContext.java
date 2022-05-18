// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import org.terasology.engine.context.Context;

import java.lang.invoke.SerializedLambda;
import java.util.List;

import static com.google.common.base.Verify.verify;

@SuppressWarnings("checkstyle:MethodTypeParameterName")
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

    public <T1, T2, T3, R> R invoke(InvokingHelpers.SerializableFunction3<T1, T2, T3, R> func) {
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func), inner::getValue);
    }

    public <T1, T2, T3, T4, R> R invoke(InvokingHelpers.SerializableFunction4<T1, T2, T3, T4, R> func) {
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func), inner::getValue);
    }

    public <T1, T2, T3, T4, T5, R> R invoke(InvokingHelpers.SerializableFunction5<T1, T2, T3, T4, T5, R> func) {
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func), inner::getValue);
    }

    public <T1, T2, T3, T4, T5, T6, R> R invoke(InvokingHelpers.SerializableFunction6<T1, T2, T3, T4, T5, T6, R> func) {
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func), inner::getValue);
    }

    public <T1, T2, T3, T4, T5, T6, T7, R> R invoke(
            InvokingHelpers.SerializableFunction7<T1, T2, T3, T4, T5, T6, T7, R> func) {
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func), inner::getValue);
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, R> R invoke(
            InvokingHelpers.SerializableFunction8<T1, T2, T3, T4, T5, T6, T7, T8, R> func) {
        return InvokingHelpers.invokeProvidingParametersByType(
                InvokingHelpers.getSerializedLambdaUnchecked(func), inner::getValue);
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
