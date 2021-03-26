// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator;

/**
 */
public class UnresolvedWorldGeneratorException extends Exception {

    private static final long serialVersionUID = 2096504461776129337L;

    public UnresolvedWorldGeneratorException() {
    }

    public UnresolvedWorldGeneratorException(String message) {
        super(message);
    }

    public UnresolvedWorldGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
