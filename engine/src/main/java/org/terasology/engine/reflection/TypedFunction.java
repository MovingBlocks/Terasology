// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class TypedFunction<T, R> implements Function<T, R> {
    private final Class<T> clazz;
    private final Function<T, R> func;

    TypedFunction(Class<T> clazz, Function<T, R> func) {
        this.clazz = clazz;
        this.func = func;
    }

    @SafeVarargs
    static <T, R> TypedFunction<T, R> of(Function<T, R> func, T... ts) {
        checkNotNull((Object) ts, "did not received typed varargs array");
        checkArgument(ts.length == 0, "Args to ts are ignored, do not supply them.");
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) ts.getClass().getComponentType();
        if (clazz.equals(Object.class)) {
            //noinspection ImplicitArrayToString
            throw new RuntimeException(String.format(
                    "Looks like Object, which is more generic than you want. %s of %s", clazz, ts));
        }
        return new TypedFunction<>(clazz, func);
    }

    @Override
    public R apply(T t) {
        return func.apply(t);
    }

    public Class<T> getInputClass() {
        return clazz;
    }
}
