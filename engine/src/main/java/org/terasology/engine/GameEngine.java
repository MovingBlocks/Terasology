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

import org.terasology.engine.modes.GameState;

/**
 * The game engine is the core of Terasology. It maintains a stack of game states, that drive the behaviour of
 * Terasology in different modes (Main Menu, ingame, dedicated server, etc)
 *
 * @author Immortius
 */
public interface GameEngine {

    /**
     * Initialises the engine
     */
    void init();

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
     * Cleans up the engine. Can only be called after shutdown.
     */
    void dispose();

    /**
     * @return Whether the engine is running
     */
    boolean isRunning();

    /**
     * @return Whether the engine has been disposed
     */
    boolean isDisposed();

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

    // TODO: Move task system elsewhere? Need to support saving queued/unfinished tasks too, when the world
    // shuts down

    /**
     * Submits a task to be run concurrent with the main thread
     *
     * @param name
     * @param task
     */
    void submitTask(String name, Runnable task);

    boolean isHibernationAllowed();

    void setHibernationAllowed(boolean allowed);

    // TODO: This probably should be elsewhere?

    /**
     * @return Whether the game window currently has focus
     */
    boolean hasFocus();

    /**
     * @return Whether the game window controls if the mouse is captured.
     */
    boolean hasMouseFocus();

    void setFocus(boolean focused);

    void subscribeToStateChange(StateChangeSubscriber subscriber);

    void unsubscribeToStateChange(StateChangeSubscriber subscriber);


}
