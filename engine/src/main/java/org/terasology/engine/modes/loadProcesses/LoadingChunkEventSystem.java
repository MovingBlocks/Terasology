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
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;

/**
 * A system to send new chunk events to the current game state, which can be
 * useful to identify when chunk generation fails on initial load
 */
@RegisterSystem
public class LoadingChunkEventSystem extends BaseComponentSystem {

    @In
    private Context context;

    /**
     * Event handler which waits for new chunk events, then sends those
     * events to the current game state
     * @param chunkAvailable an event which includes the position of the new chunk
     * @param worldEntity the world entity that this event was sent to
     */
    @ReceiveEvent(components = {WorldComponent.class})
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        GameEngine gameEngine = context.get(GameEngine.class);

        GameState gameState = gameEngine.getState();
        gameState.onChunkLoaded(chunkAvailable, worldEntity);
    }
}
