// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes;

/**
 *
 */
public interface LoadProcess {

    /**
     * @return A message describing the state of the process
     */
    String getMessage();

    /**
     * Runs a single step.
     *
     * @return Whether the overall process is finished
     */
    boolean step();

    /**
     * Begins the loading
     */
    default void begin() {
    }

    /**
     * @return The progress of the process, between 0f and 1f inclusive
     */
    float getProgress();
}
