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

public final class TimerLwjgl implements Timer {
    private final float decayRate = 0.95f;
    private final float oneMinusDecayRate = 1.0f - decayRate;
    private long last = 0;
    private long delta = 0;
    private float avgDelta = 0;

    public TimerLwjgl() {
    }

    @Override
    public void tick() {
        long now = getTimeInMs();
        delta = now - last;
        last = now;
        avgDelta = avgDelta * decayRate + delta * oneMinusDecayRate;
    }

    @Override
    public float getDelta() {
        return (delta >= 100) ? 0.1f : delta / 1000f;
    }

    @Override
    public double getDeltaInMS() {
        return (delta >= 100) ? 100 : delta;
    }

    @Override
    public double getFps() {
        return 1000.0f / avgDelta;
    }

    @Override
    public long getTimeInMs() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
