// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.exceptions;

public class CommandInitializationException extends IllegalArgumentException {
    private static final long serialVersionUID = 5345663512766407880L;

    public CommandInitializationException() {
    }

    public CommandInitializationException(String s) {
        super(s);
    }
}
