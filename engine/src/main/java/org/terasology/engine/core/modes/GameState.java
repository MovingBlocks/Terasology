// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;

/**
 * @version 0.1
 *
 * A GameState encapsulates a different set of systems and managers being initialized
 * on state change and updated every iteration of the main loop (every frame). Existing
 * GameState implementations do not necessarily represent a state of play.
 * I.e. interacting with the Main Menu is handled through a GameState.
 *
 */
public interface GameState {

    void init(GameEngine engine);

    void dispose(boolean shuttingDown);

    default void dispose() {
        dispose(false);
    }

    void handleInput(float delta);

    void update(float delta);

    void render();

    /**
     * @return Whether the game should hibernate when it loses focus
     */
    boolean isHibernationAllowed();

    /**
     * @return identifies the target for logging events
     */
    String getLoggingPhase();

    Context getContext();

    default void onChunkLoaded(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        
    }
}
