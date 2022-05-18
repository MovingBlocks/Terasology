// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;

/**
 * This technique, obtuse as it is, does not require overriding anything marked private.
 * <p>
 * Does require {@link java.io.SerializablePermission SerializablePermission("enableSubstitution")},
 * as per {@link ObjectOutputStream#enableReplaceObject}.
 */
@SuppressWarnings("JavadocReference")
public class CaptureSerializedOutput implements ExtractSerializedLambda {
    static SerializedLambda getSerializedLambdaFromOutputStream(Serializable obj) {
        List<Object> outputObjects;
        try (var output = new SpyingOutputObjectStream(new ByteArrayOutputStream())) {
            try {
                output.writeObject(obj);
            } finally {
                outputObjects = output.getEmittedObjects();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return (SerializedLambda) outputObjects.get(0);
    }

    @Override
    public SerializedLambda of(Serializable lambda) {
        return getSerializedLambdaFromOutputStream(lambda);
    }

    static class SpyingOutputObjectStream extends ObjectOutputStream {
        private final List<Object> emittedObjects = new ArrayList<>();

        SpyingOutputObjectStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) {
            emittedObjects.add(obj);
            // The user of this class only cares about the rewritten form of the object,
            // not the bytestream. Returning null saves the serializer work and avoids
            // potential serialization errors.
            return null;
        }

        public List<Object> getEmittedObjects() {
            return List.copyOf(emittedObjects);
        }
    }
}
