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
package org.terasology.engine.internal;

import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.EngineTime;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public abstract class TimeBase implements EngineTime {

    private static final Logger logger = LoggerFactory.getLogger(TimeBase.class);
    private static final float DECAY_RATE = 0.95f;
    private static final float ONE_MINUS_DECAY_RATE = 1.0f - DECAY_RATE;
    private static final float RESYNC_TIME_RATE = 0.1f;

    private static final int MAX_UPDATE_CYCLE_LENGTH = 1000;
    private static final int UPDATE_CAP = 1000;

    private AtomicLong last = new AtomicLong(0);
    private AtomicLong realDelta = new AtomicLong(0);
    private AtomicLong gameDelta = new AtomicLong(0);
    private float frameDelta;
    private long desynch;
    private boolean paused;

    private AtomicDouble gameDilation = new AtomicDouble(1.0f);

    private AtomicLong gameTime = new AtomicLong(0);

    public TimeBase(long initialTime) {
        last.set(initialTime);
    }

    protected abstract long getRawTimeInMs();

    /**
     * Increments time
     *
     * @return The number of update cycles to run
     */
    @Override
    public Iterator<Float> tick() {
        long now = getRawTimeInMs();
        long newDelta = now - last.get();
        if (0 == newDelta) {
            // running too fast, slow down to avoid busy-waiting
            try {
                Thread.sleep(0, 1000);
            } catch (InterruptedException e) {
                // do nothing
                logger.debug("Tick sleep interrupted", e);
            }
            now = getRawTimeInMs();
            newDelta = now - last.get();
        }
        last.set(now);
        realDelta.set(newDelta);
        frameDelta = frameDelta * DECAY_RATE + newDelta * ONE_MINUS_DECAY_RATE;

        newDelta = (long) (newDelta * getGameTimeDilation());
        if (newDelta >= UPDATE_CAP) {
            logger.warn("Delta too great ({}), capping to {}", newDelta, UPDATE_CAP);
            newDelta = UPDATE_CAP;
        }
        int updateCycles = (int) ((newDelta - 1) / MAX_UPDATE_CYCLE_LENGTH) + 1;

        // Reduce desynch between server time
        if (desynch != 0) {
            long diff = (long) Math.ceil(desynch * RESYNC_TIME_RATE);
            if (diff == 0) {
                diff = (long) Math.signum(desynch);
            }
            gameTime.getAndAdd(diff);
            desynch -= diff;
        }

        if (paused) {
            gameDelta.set(0);
            return new TimeStepper(1, 0);
        } else {
            if (updateCycles > 0) {
                gameDelta.set(newDelta / updateCycles);
            }
            return new TimeStepper(updateCycles, newDelta / updateCycles);
        }
    }

    @Override
    public float getGameDelta() {
        return gameDelta.get() / 1000f;
    }

    @Override
    public long getGameDeltaInMs() {
        return gameDelta.get();
    }

    @Override
    public float getFps() {
        return 1000.0f / frameDelta;
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
    public float getGameTimeDilation() {
        return (float) gameDilation.get();
    }

    @Override
    public void setGameTimeDilation(float newDilation) {
        this.gameDilation.set(newDilation);
    }

    @Override
    public void setGameTime(long amount) {
        gameDelta.set(0);
        gameTime.set(amount);
    }

    @Override
    public long getRealTimeInMs() {
        return getRawTimeInMs();
    }

    @Override
    public long getRealDeltaInMs() {
        return realDelta.get();
    }

    @Override
    public float getRealDelta() {
        return realDelta.get() / 1000f;
    }

    @Override
    public void updateTimeFromServer(long targetTime) {
        desynch = targetTime - getGameTimeInMs();
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    private class TimeStepper implements Iterator<Float> {

        private int cycles;
        private long deltaPerCycle;
        private int currentCycle;

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
}
