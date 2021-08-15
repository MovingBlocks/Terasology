// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

/**
 * An enum for describing the status of the engine, to be used in addition to the StandardGameStatuses
 */
public enum TerasologyEngineStatus implements EngineStatus {

    PREPARING_SUBSYSTEMS("Preparing Subsystems..."),
    INITIALIZING_ASSET_MANAGEMENT("Initializing Asset Management..."),
    INITIALIZING_SUBSYSTEMS("Initializing Subsystems..."),
    INITIALIZING_MODULE_MANAGER("Initializing Module Management..."),
    INITIALIZING_LOWLEVEL_OBJECT_MANIPULATION("Initializing low-level object manipulators..."),
    INITIALIZING_ASSET_TYPES("Initializing asset types...");

    private final String defaultDescription;

     TerasologyEngineStatus(String defaultDescription) {
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
