/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.engine.internal;

import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.EngineTime;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public final class TimeLwjgl implements EngineTime {
    private static final Logger logger = LoggerFactory.getLogger(TimeLwjgl.class);
    private static final float DECAY_RATE = 0.95f;
    private static final float ONE_MINUS_DECAY_RATE = 1.0f - DECAY_RATE;
    private static final float RESYNC_TIME_RATE = 0.1f;

    private static final int MAX_UPDATE_CYCLE_LENGTH = 1000;
    private static final int UPDATE_CAP = 1000;

    private AtomicLong last = new AtomicLong(0);
    private AtomicLong delta = new AtomicLong(0);
    private float avgDelta = 0;
    private long desynch = 0;

    private AtomicLong gameTime = new AtomicLong(0);

    /**
     * Increments time
     * @return The number of update cycles to run
     */
    public Iterator<Float> tick() {
        long now = getRawTimeInMs();
        long newDelta = now - last.get();
        if (newDelta >= UPDATE_CAP) {
            logger.warn("Delta too great ({}), capping to {}", newDelta, UPDATE_CAP);
            newDelta = UPDATE_CAP;
        }
        int updateCycles = (int)((newDelta - 1) / MAX_UPDATE_CYCLE_LENGTH) + 1;
        last.set(now);
        avgDelta = avgDelta * DECAY_RATE + newDelta * ONE_MINUS_DECAY_RATE;

        // Reduce desynch between server time
        if (desynch != 0) {
            long diff = (long) Math.ceil(desynch * RESYNC_TIME_RATE);
            if (diff == 0) {
                diff = (long) Math.signum(desynch);
            }
            gameTime.getAndAdd(diff);
            desynch -= diff;
        }

        if (updateCycles > 0) {
            delta.set(newDelta / updateCycles);
        }
        return new TimeStepper(updateCycles, newDelta / updateCycles);
    }

    private class TimeStepper implements Iterator<Float> {

        private int cycles;
        private long deltaPerCycle;
        private int currentCycle = 0;

        public TimeStepper(int cycles, long deltaPerCycle) {
            this.cycles = cycles;
            this.deltaPerCycle = deltaPerCycle;
        }

        @Override
        public boolean hasNext() {
            return currentCycle < cycles;
        }

        @Override
        public Float next() {
            currentCycle++;
            gameTime.addAndGet(deltaPerCycle);
            return deltaPerCycle / 1000f;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public float getDelta() {
        return delta.get() / 1000f;
    }

    @Override
    public long getDeltaInMs() {
        return delta.get();
    }

    @Override
    public float getFps() {
        return 1000.0f / avgDelta;
    }

    @Override
    public long getGameTimeInMs() {
        return gameTime.get();
    }

    @Override
    public float getGameTime() {
        return gameTime.get() / 1000f;
    }


    @Override
    public void setGameTime(long amount) {
        gameTime.set(amount);
    }

    @Override
    public long getRawTimeInMs() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    @Override
    public void updateTimeFromServer(long targetTime) {
        desynch = targetTime - getGameTimeInMs();
    }
}
