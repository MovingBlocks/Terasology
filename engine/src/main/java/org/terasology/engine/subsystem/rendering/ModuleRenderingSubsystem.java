/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.rendering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.module.rendering.RenderingModuleManager;
import org.terasology.engine.subsystem.EngineSubsystem;

public class ModuleRenderingSubsystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(ModuleRenderingSubsystem.class);

    private RenderingModuleManager renderingModuleManager;

    @Override
    public String getName() {
        return "ModuleRendering";
    }

    @Override
    public void preInitialise(Context rootContext) {

    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        this.renderingModuleManager = new RenderingModuleManager();
        rootContext.put(RenderingModuleManager.class, this.renderingModuleManager);
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {

    }

    @Override
    public void postInitialise(Context context) {

    }

    @Override
    public void preUpdate(GameState currentState, float delta) {

    }

    @Override
    public void postUpdate(GameState currentState, float delta) {

    }

    @Override
    public void preShutdown() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {

    }
}
