// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.systems;

public interface ComponentSystem {
    /**
     * Called to initialise the system. This occurs after injection, but before other systems are necessarily initialised, so they should not be interacted with.
     */
    void initialise();

    /**
     * Called after all systems are initialised, but before the game is loaded.
     */
    void preBegin();

    /**
     * Called after the game is loaded, right before first frame.
     */
    void postBegin();

    /**
     * Called before the game is auto-saved.
     * TODO: Implemented as default method to avoid violating API. May want to review / revise when retrofitting gestalt-entity v6+
     */
    default void preAutoSave() {

    }

    /**
     * Called after the game is auto-saved.
     * TODO: Implemented as default method to avoid violating API. May want to review / revise when retrofitting gestalt-entity v6+
     */
    default void postAutoSave() {

    }

    /**
     * Called before the game is saved (this may be after shutdown).
     */
    void preSave();

    /**
     * Called after the game is saved.
     */
    void postSave();

    /**
     * Called right before the game is shut down.
     */
    void shutdown();
}
