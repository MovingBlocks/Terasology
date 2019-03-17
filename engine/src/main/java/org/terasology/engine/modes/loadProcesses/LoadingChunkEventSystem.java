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
package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.GameState;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.event.OnChunkLoaded;

@RegisterSystem
public class LoadingChunkEventSystem extends BaseComponentSystem {

    @In
    private Context context;

    @ReceiveEvent(components = {WorldComponent.class})
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        GameEngine gameEngine = context.get(GameEngine.class);

        GameState gameState = gameEngine.getState();
        gameState.onChunkLoaded(chunkAvailable, worldEntity);
    }
}
