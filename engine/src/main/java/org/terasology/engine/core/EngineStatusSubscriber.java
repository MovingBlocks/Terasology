// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

/**
 * Interface for subscribing to notification of engine status changes.
 */
@FunctionalInterface
public interface EngineStatusSubscriber {

    /**
     * Called when the status of the engine changes
     * @param newStatus The new status of the engine
     */
    void onEngineStatusChanged(EngineStatus newStatus);
}
