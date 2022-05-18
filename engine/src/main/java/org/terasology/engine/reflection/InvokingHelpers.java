// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class InvokingHelpers {
    private InvokingHelpers() { }

    /**
     * The Class of the function's first argument.
     * <p>
     * The trick here is that even if you pass no {@code ts}, the method still receives a zero-length
     * array object, and we can read the class of that.
     */
    @SafeVarargs
    static <T, R> Class<T> argType(Function<T, R> func, T... ts) {
        return TypedFunction.of(func, ts).getInputClass();
    }

    static List<Class<?>> getLambdaParameters(SerializedLambda serializedLambda) {
        var methodType = MethodType.fromMethodDescriptorString(
                serializedLambda.getImplMethodSignature(),
                serializedLambda.getClass().getClassLoader()
        );
        return methodType.parameterList();
    }

    interface SerializableFunction<T, R> extends Function<T, R>, Serializable { }

    interface SerializableBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable { }

}
