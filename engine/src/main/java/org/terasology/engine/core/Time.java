// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import org.terasology.context.annotation.API;

/**
 * The timer manages all time in the game.
 * <ol>
 * <li>Delta time (how long is passing in the current update cycle)</li>
 * <li>Game time (how long the world has been played in real time, starting at 0)</li>
 * </ol>
 *
 */
@API
public interface Time {

    /**
     * @return Whether game time is paused.
     */
    boolean isPaused();

    /**
     * Pauses/unpaused game time. When game time is paused, game time does not advance and game deltas are zero. Real time continues to advance.
     * @param paused Whether to pause or unpause time.
     */
    void setPaused(boolean paused);

    void setGameTimeDilation(float dilation);

    float getGameTimeDilation();

    /**
     * @return The current framerate
     */
    float getFps();

    /**
     * @return The size of the time change for the current update, in seconds
     */
    float getGameDelta();

    /**
     * @return The size of the time change for the current update in ms
     */
    long getGameDeltaInMs();

    /**
     * @return Game time in milliseconds. This is synched with the server.
     */
    long getGameTimeInMs();

    /**
     * @return The current game time, in seconds.
     */
    float getGameTime();

    /**
     * @return The size of the real time change for the current update, in seconds
     */
    float getRealDelta();

    /**
     * @return The size of the real time change for the current update in ms
     */
    long getRealDeltaInMs();

    /**
     * There is no variant of this method that returns seconds because the values can become to large to represent the
     * as float with the necessary precision.
     *
     * @return Real time in milliseconds.
     */
    long getRealTimeInMs();

}
