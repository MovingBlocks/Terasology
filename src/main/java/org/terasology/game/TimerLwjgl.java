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

import org.lwjgl.Sys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public final class TimerLwjgl implements Timer {
    private static final Logger logger = LoggerFactory.getLogger(TimerLwjgl.class);
    private static final float DECAY_RATE = 0.95f;
    private static final float ONE_MINUS_DECAY_RATE = 1.0f - DECAY_RATE;
    private static final float RESYNC_TIME_RATE = 0.1f;

    private AtomicLong last = new AtomicLong(0);
    private AtomicLong delta = new AtomicLong(0);
    private float avgDelta = 0;
    private AtomicLong serverTimeOffset = new AtomicLong(0);
    private long desynch = 0;


    public TimerLwjgl() {
    }

    @Override
    public void tick() {
        long now = getRawTimeInMs();
        long newDelta = now - last.get();
        delta.set(newDelta);
        last.set(now);
        avgDelta = avgDelta * DECAY_RATE + newDelta * ONE_MINUS_DECAY_RATE;

        if (desynch != 0) {
            long diff = (long) Math.ceil(desynch * RESYNC_TIME_RATE);
            if (diff == 0) {
                diff = (long) Math.signum(desynch);
            }
            serverTimeOffset.getAndAdd(diff);
            desynch -= diff;
        }

    }

    @Override
    public float getDelta() {
        long d = delta.get();
        return (d >= 100) ? 0.1f : d / 1000f;
    }

    @Override
    public long getDeltaInMs() {
        long d = delta.get();
        return (d >= 100) ? 100 : d;
    }

    @Override
    public double getFps() {
        return 1000.0f / avgDelta;
    }

    @Override
    public long getRawTimeInMs() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    @Override
    public long getTimeInMs() {
        return last.get() + serverTimeOffset.get();
    }

    @Override
    public void updateServerTime(long ms, boolean immediate) {
        if (immediate) {
            serverTimeOffset.set(ms - last.get());
        } else {
            desynch = ms - getTimeInMs();
        }
    }
}
