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
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;

public interface EngineSubsystem {

    /**
     * Called on each system before initialisation
     */
    void preInitialise(Context context);

    /**
     * Called to initialise the system
     */
    void initialise(Context context);

    /**
     * Called to register any core asset types this system provides. This happens after initialise and before postInitialise
     *
     * @param assetTypeManager
     */
    void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager);

    /**
     * Called to do any final initialisation after asset types are registered.
     */
    void postInitialise(Context context);

    void preUpdate(GameState currentState, float delta);

    void postUpdate(GameState currentState, float delta);

    void shutdown(Config config);

    void dispose();

    void registerSystems(ComponentSystemManager componentSystemManager);
}
