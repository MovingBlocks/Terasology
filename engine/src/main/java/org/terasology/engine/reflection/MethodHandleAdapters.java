// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import com.google.common.base.Throwables;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public final class MethodHandleAdapters {
    private MethodHandleAdapters() { }

    static MethodHandle ofLambda(SerializedLambda serializedLambda) throws ReflectiveOperationException {
        // SerializedLambda's implementation has a reference to the capturing class,
        // but it only exposes its name, so we'll have to look it up again.
        String capturingClassName = serializedLambda.getCapturingClass().replace('/', '.');

        MethodHandles.Lookup lookup;

        try {
            // I'm guessing about the AccessController-related stuff, but the intention
            // is that we get a Lookup instance that matches the capturing site of the lambda.
            lookup = AccessController.doPrivileged((PrivilegedExceptionAction<MethodHandles.Lookup>) () -> {
                MethodHandles.Lookup ourLookup = MethodHandles.lookup();
                Class<?> capturingClass = ourLookup.findClass(capturingClassName);
                return MethodHandles.privateLookupIn(capturingClass, ourLookup);
            });
        } catch (PrivilegedActionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            Throwables.throwIfInstanceOf(e.getCause(), ReflectiveOperationException.class);
            throw new RuntimeException(e);
        }

        Class<?> implClass = lookup.findClass(serializedLambda.getImplClass().replace('/', '.'));
        String name = serializedLambda.getImplMethodName();

        // It seems weird to be carefully using the Lookup interface to find classes,
        // and then have the MethodType back to using a ClassLoader. But I guess we
        // trust `lookup.find*` to not return anything it shouldn't in the end.
        MethodType methodType = MethodType.fromMethodDescriptorString(
                serializedLambda.getImplMethodSignature(),
                lookup.lookupClass().getClassLoader()
        );

        Object receiver = null;
        if (serializedLambda.getCapturedArgCount() > 0) {
            receiver = serializedLambda.getCapturedArg(0);
        }

        // Surely this code must exist somewhere else already.
        switch (serializedLambda.getImplMethodKind()) {
            case MethodHandleInfo.REF_getField:
                return lookup.findGetter(implClass, name, methodType.returnType()).bindTo(receiver);
            case MethodHandleInfo.REF_getStatic:
                return lookup.findStaticGetter(implClass, name, methodType.returnType());
            case MethodHandleInfo.REF_putField:
                return lookup.findSetter(implClass, name, methodType.parameterType(0)).bindTo(receiver);
            case MethodHandleInfo.REF_putStatic:
                return lookup.findStaticSetter(implClass, name, methodType.parameterType(0));
            case MethodHandleInfo.REF_invokeInterface:
            case MethodHandleInfo.REF_invokeVirtual:
                return lookup.findVirtual(implClass, name, methodType).bindTo(receiver);
            case MethodHandleInfo.REF_invokeStatic:
                return lookup.findStatic(implClass, name, methodType);
            case MethodHandleInfo.REF_invokeSpecial:
                return lookup.findSpecial(implClass, name, methodType, lookup.lookupClass())
                        .bindTo(receiver);
            case MethodHandleInfo.REF_newInvokeSpecial:
                return lookup.findConstructor(implClass, methodType);
            default:
                throw new RuntimeException("Not implemented for " +
                    MethodHandleInfo.referenceKindToString(serializedLambda.getImplMethodKind()));
        }
    }
}
