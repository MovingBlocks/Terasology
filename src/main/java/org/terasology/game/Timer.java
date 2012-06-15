package org.terasology.game;

import org.lwjgl.Sys;

public final class Timer {
    private final float decayRate = 0.95f;
    private final float oneMinusDecayRate = 1.0f - decayRate;
    private final long timerTicksPerSecond;
    private long last = 0;
    private long delta = 0;
    private float avgDelta = 0;

    public Timer() {
        //lwjgl libs need to be loaded prior to initialization!
        //do not construct timer earlier
        timerTicksPerSecond = Sys.getTimerResolution();
    }

    public void tick() {
        long now = getTimeInMs();
        delta = now - last;
        last = now;
        avgDelta = avgDelta * decayRate + delta * oneMinusDecayRate;
    }

    public float getDelta() {
        return (delta >= 100) ? 0.1f : delta / 1000f;
    }

    public double getDeltaInMS() {
        return (delta >= 100) ? 100 : delta;
    }

    public double getFps() {
        return 1000.0f / avgDelta;
    }

    public long getTimeInMs() {
        return (Sys.getTime() * 1000) / timerTicksPerSecond;
    }
}