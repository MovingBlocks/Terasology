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
package org.terasology.engine.subsystem.lwjgl;

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.internal.TimeLwjgl;
import org.terasology.engine.modes.GameState;

public class LwjglTimer extends BaseLwjglSubsystem {

    @Override
    public void preInitialise(Context context) {
        super.preInitialise(context);
        initTimer(context); // Dependent on LWJGL
    }

    @Override
    public void initialise(Context context) {

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
    public void shutdown(Config config) {
    }

    @Override
    public void dispose() {
    }

    private void initTimer(Context context) {
        EngineTime time = new TimeLwjgl();
        context.put(Time.class, time);
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
    }

}
