/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem;

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;

public interface EngineSubsystem {

    /**
     * @return The name of the subsystem
     */
    String getName();

    /**
     * Called on each system before initialisation. This is an opportunity to add anything into the root context that will carry across the entire rune
     * of the engine, and may be used by other systems
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
