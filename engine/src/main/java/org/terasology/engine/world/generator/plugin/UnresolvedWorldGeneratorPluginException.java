// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generator.plugin;

import java.io.Serial;

public class UnresolvedWorldGeneratorPluginException extends Exception {

    @Serial
    private static final long serialVersionUID = 2096504461776129337L;

    public UnresolvedWorldGeneratorPluginException() {
    }

    public UnresolvedWorldGeneratorPluginException(String message) {
        super(message);
    }

    public UnresolvedWorldGeneratorPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
