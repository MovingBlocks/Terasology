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

import org.terasology.module.sandbox.API;

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
