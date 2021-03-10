// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

/**
 * Engine Status provides the current status of the engine - what it is doing, at a higher granularity than just running. This can be used by external and internal observers
 * to report on the state of the engine, such as splash screens/loading screens.
 */
public interface EngineStatus {

    /**
     * @return The description of the status
     */
    String getDescription();

    /**
     * @return Whether this is a status that "progresses" such as loading, with a known completion point
     */
    default boolean isProgressing() {
        return false;
    }

    /**
     * @return The progress of this status, between 0 and 1 inclusive where 1 is complete and 0 is just started.
     */
    default float getProgress() {
        return 0;
    }
}
