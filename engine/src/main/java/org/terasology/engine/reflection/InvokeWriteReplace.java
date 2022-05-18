// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import com.google.common.base.Throwables;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
 */
public class InvokeWriteReplace implements ExtractSerializedLambda {
    static SerializedLambda getSerializedLambda(Serializable obj) throws IllegalAccessException {
        Method writeReplace;
        try {
            writeReplace = obj.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            Object replacement = AccessController.doPrivileged((PrivilegedExceptionAction<?>) () -> {
                writeReplace.setAccessible(true);
                return writeReplace.invoke(obj);
            });
            return (SerializedLambda) replacement;
        } catch (PrivilegedActionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            Throwables.throwIfInstanceOf(e.getCause(), IllegalAccessException.class);
            throw new RuntimeException(e);
        }
    }

    static SerializedLambda getSerializedLambdaUnchecked(Serializable obj) {
        try {
            return getSerializedLambda(obj);
        } catch (IllegalAccessException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public SerializedLambda of(Serializable lambda) {
        return null;
    }
}
