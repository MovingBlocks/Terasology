// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.GameState;

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
     *
     * Same as initializeRun + runMain
     */
    void run(GameState initialState);

    /**
     * Performs engine initialization only and
     * Invalid for a disposed engine
     *
     * @param initialState initial game state
     */
    void initializeRun(GameState initialState);

    /**
     * Runs the main loop of the engine.
     */
    void runMain();

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
