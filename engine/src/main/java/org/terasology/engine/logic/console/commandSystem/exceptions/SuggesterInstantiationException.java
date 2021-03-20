// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.exceptions;

/**
 * Thrown when a suggester fails instantiating via the newInstance command
 *
 */
public class SuggesterInstantiationException extends RuntimeException {
    private static final long serialVersionUID = 3151467068962337565L;

    public SuggesterInstantiationException() {
    }

    public SuggesterInstantiationException(String message) {
        super(message);
    }

    public SuggesterInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SuggesterInstantiationException(Throwable cause) {
        super(cause);
    }

    public SuggesterInstantiationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
