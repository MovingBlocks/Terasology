// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity;

/**
 * This exception indicates an issue during decryption c
 */
public class BadEncryptedDataException extends Exception {

    private static final long serialVersionUID = 7220484295134905387L;

    public BadEncryptedDataException() {
    }

    public BadEncryptedDataException(String message) {
        super(message);
    }

    public BadEncryptedDataException(Throwable cause) {
        super(cause);
    }

    public BadEncryptedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
