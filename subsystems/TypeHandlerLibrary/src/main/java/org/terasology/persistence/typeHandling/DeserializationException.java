// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

/**
 * This exception is thrown when deserializing a type fails
 */
public class DeserializationException extends RuntimeException {

    private static final long serialVersionUID = -6211402729551315020L;

    public DeserializationException() {
    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
