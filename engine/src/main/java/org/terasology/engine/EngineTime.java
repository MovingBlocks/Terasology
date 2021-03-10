// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

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
