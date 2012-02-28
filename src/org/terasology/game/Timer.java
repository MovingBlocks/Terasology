package org.terasology.game;

import org.lwjgl.Sys;

public final class Timer {
    private final float _decayRate = 0.95f;
    private final float _oneMinusDecayRate = 1.0f - _decayRate;
    private final long _timerTicksPerSecond;
    private long _last = 0;
    private long _delta = 0;
    private float _avgDelta = 0;

    public Timer(){
        //lwjgl libs need to be loaded prior to initialization!
        //do not construct timer earlier
        _timerTicksPerSecond = Sys.getTimerResolution();
    }

    public void tick(){
        long now = getTimeInMs();
        _delta = now - _last;
        _last = now;
        _avgDelta =  _avgDelta * _decayRate + _delta * _oneMinusDecayRate;
    }

    public double getDelta() {
        return _delta;
    }

    public double getFps() {
        return 1000.0f / _avgDelta;
    }

    public long getTimeInMs() {
        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }
}