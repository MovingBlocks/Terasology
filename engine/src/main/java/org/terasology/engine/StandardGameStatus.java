// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

/**
 */
public enum StandardGameStatus implements EngineStatus {
    UNSTARTED("Unstarted"),
    LOADING("Loading"),
    RUNNING("Running"),
    INITIALIZING("Initializing"),
    SHUTTING_DOWN("Shutting down..."),
    DISPOSED("Shut down");

    private final String defaultDescription;

     StandardGameStatus(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    @Override
    public String getDescription() {
        return defaultDescription;
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public boolean isProgressing() {
        return false;
    }
}
