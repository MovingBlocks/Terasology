// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem;

import org.terasology.context.annotation.IndexInherited;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;

@IndexInherited
public interface EngineSubsystem {

    /**
     * @return The name of the subsystem
     */
    String getName();

    /**
     * Called on each system before initialisation.
     * This is an opportunity to add anything into the root context that will carry across the entire rune of the engine,
     * and may be used by other systems
     *
     * @param rootContext The root context, that will survive the entire run of the engine
     */
    default void preInitialise(Context rootContext) {
    }

    /**
     * Called to initialise the system
     *
     * @param engine      The game engine
     * @param rootContext The root context, that will survive the entire run of the engine
     */
    default void initialise(GameEngine engine, Context rootContext) {
    }

    /**
     * Called to register any core asset types this system provides. This happens after initialise and before postInitialise
     *
     * @param assetTypeManager The asset type manager to register asset types to
     */
    default void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
    }

    /**
     * Called to do any final initialisation after asset types are registered.
     */
    default void postInitialise(Context context) {
    }

    /**
     * Called before the main game logic update, once a frame/full update cycle
     * @param currentState The current state
     * @param delta The total time this frame/update cycle
     */
    default void preUpdate(GameState currentState, float delta) {
    }

    /**
     * Called after the main game logic update, once a frame/full update cycle
     * @param currentState The current state
     * @param delta The total time this frame/update cycle
     */
    default void postUpdate(GameState currentState, float delta) {
    }

    /**
     * Called just prior to shutdown.
     */
    default void preShutdown() {
    }

    default void shutdown() {
    }

    default void registerSystems(ComponentSystemManager componentSystemManager) {
    }
}
