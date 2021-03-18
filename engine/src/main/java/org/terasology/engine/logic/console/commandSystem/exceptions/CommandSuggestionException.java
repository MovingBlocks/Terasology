// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.exceptions;

/**
 * Thrown while suggesting a command parameter
 */
public class CommandSuggestionException extends Exception {
    private static final long serialVersionUID = -7812918530930324881L;

    public CommandSuggestionException() {
    }

    public CommandSuggestionException(String message) {
        super(message);
    }

    public CommandSuggestionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandSuggestionException(Throwable cause) {
        super(cause);
    }

    public CommandSuggestionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
