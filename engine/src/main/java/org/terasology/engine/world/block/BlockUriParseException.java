// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

/**
 */
public class BlockUriParseException extends RuntimeException {

    private static final long serialVersionUID = 5571599578432666018L;

    public BlockUriParseException() {
    }

    public BlockUriParseException(String message) {
        super(message);
    }

    public BlockUriParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockUriParseException(Throwable cause) {
        super(cause);
    }

    public BlockUriParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
