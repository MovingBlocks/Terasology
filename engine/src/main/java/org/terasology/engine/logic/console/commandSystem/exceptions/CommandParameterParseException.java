// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.exceptions;

public class CommandParameterParseException extends Exception {
    private static final long serialVersionUID = 4519046979318192019L;
    private final String parameter;

    public CommandParameterParseException(String message, Throwable cause, String parameter) {
        super(message, cause);
        this.parameter = parameter;
    }

    public CommandParameterParseException(String message, String parameter) {
        super(message);
        this.parameter = parameter;
    }

    public CommandParameterParseException(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
