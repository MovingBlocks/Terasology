/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.modes;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.chunks.event.OnChunkLoaded;

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
