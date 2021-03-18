// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.ui;

/**
 */
public interface TabCompletionEngine {

    /**
     * @param command The currently entered command
     * @return The command with a completed argument or the command provided.
     */
    String complete(String command);

    /**
     * Resets the completion engine
     */
    void reset();
}
