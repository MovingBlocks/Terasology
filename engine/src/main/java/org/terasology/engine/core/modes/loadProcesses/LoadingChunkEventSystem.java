// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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
    @ReceiveEvent(components = WorldComponent.class)
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        GameEngine gameEngine = context.get(GameEngine.class);

        GameState gameState = gameEngine.getState();
        gameState.onChunkLoaded(chunkAvailable, worldEntity);
    }
}
