/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game;

import org.terasology.game.modes.GameState;

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
     * Cleans up the engine. Can only be
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
     * Clears all states, replacing them with newState
     *
     * @param newState
     */
    void changeState(GameState newState);

    /**
     * Deactivates the current state, and activates newState (putting it on top of the stack
     *
     * @param newState
     */
    void pushState(GameState newState);

    /**
     * Disposes the current state, re-activating the previous state if any
     */
    void popState();

    // TODO: Move task system elsewhere? Need to support saving queued/unfinished tasks too, when the world
    // shuts down

    /**
     * Submits a task to be run concurrent with the main thread
     *
     * @param name
     * @param task
     */
    void submitTask(String name, Runnable task);

    /**
     * @return Count of currently active tasks
     */
    int getActiveTaskCount();


}
