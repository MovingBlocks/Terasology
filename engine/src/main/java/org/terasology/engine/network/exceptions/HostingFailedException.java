// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.exceptions;

/**
 * Exception when hosting a game fails.
 *
 */
public class HostingFailedException extends Exception {

    private static final long serialVersionUID = -7936928340463418712L;

    public HostingFailedException() {
        super();
    }

    public HostingFailedException(String message) {
        super(message);
    }

    public HostingFailedException(Throwable cause) {
        super(cause);
    }

    public HostingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
