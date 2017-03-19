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

package org.terasology.engine;

import org.terasology.context.Context;
import org.terasology.engine.modes.GameState;

/**
 * The game engine is the core of Terasology. It maintains a stack of game states, that drive the behaviour of
 * Terasology in different modes (Main Menu, ingame, dedicated server, etc)
 *
 */
public interface GameEngine {

    /**
     * @return The current, fine-grained status of the engine.
     */
    EngineStatus getStatus();

    /**
     * Subscribe for notification of engine status changes
     * @param subscriber
     */
    void subscribe(EngineStatusSubscriber subscriber);

    /**
     * Unsubscribe to notifications of engine status changes.
     * @param subscriber
     */
    void unsubscribe(EngineStatusSubscriber subscriber);

    /**
     * Runs the engine, which will block the thread.
     * Invalid for a disposed engine
     */
    void run(GameState initialState);

    /**
     * Request the engine to stop running
     */
    void shutdown();

    /**
     * @return Whether the engine is running - this is true from the point run() is called to the point shutdown is complete
     */
    boolean isRunning();

    /**
     * @return The current state of the engine
     */
    GameState getState();

    /**
     * Clears all states, replacing them with newState
     *
     * @param newState
     */
    void changeState(GameState newState);

    void subscribeToStateChange(StateChangeSubscriber subscriber);

    void unsubscribeToStateChange(StateChangeSubscriber subscriber);

    boolean hasPendingState();

    /**
     * Creates a context that provides read access to the objects of the engine context and can
     * be populated with it's own private objects.
     */
    Context createChildContext();
}
