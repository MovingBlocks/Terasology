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

import java.util.Iterator;

/**
 */
public interface EngineTime extends Time {

    /**
     * Updates time.  This returns an iterator that will return a float for each time step to be processed this
     * frame - as the steps are iterated over gameTime will be updated.
     *
     * @return An iterator over separate time cycles this tick.
     */
    Iterator<Float> tick();

    /**
     * Sets the game time.
     *
     * @param time
     */
    void setGameTime(long time);

    /**
     * Updates the timer with the desired time from the server. The game time won't immediately be updated - instead
     * the update will be applied over a number of ticks to smooth the resynchronization.
     *
     * @param targetTime
     */
    void updateTimeFromServer(long targetTime);

}
