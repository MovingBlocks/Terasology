/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.benchmark;

/**
 * A simple, but effective pausable timer for micro-benchmarking.
 * Use start() to start a new instance and get() for timings in milli-seconds
 * @author Martin Steiger
 */
public final class Timer {
    
    private long start;
    private long accum;
    private boolean paused;
    
    private Timer() {
        start = measure();
    }

    /**
     * Starts a measurement with a generated timer
     * @return the generated timer
     */
    public static Timer start() {
        Timer id = new Timer();
        
        return id;
    }

    /**
     * Starts a measurement with a generated, paused timer
     * @return the generated, paused timer
     */
    public static Timer startPaused() {
        Timer id = new Timer();
        id.paused = true;   // pause manually and thus don't increment accum       
        return id;
    }
    
    /**
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * Pauses the timer
     */
    public void pause() {
        if (!paused) {
            long now = measure();
            accum += now - start;
            paused = true;
        }
    }
 
    /**
     * Resumes the timer
     */
    public void resume() {
        if (paused) {
            start = measure();
            paused = false;
        }
    }
    
    /**
     * Get the time
     * @return the time in milliseconds
     */
    public double get() {
        if (paused) {
            return accum / 1000000.0;
        }
        
        long now = accum + measure();
        return (now - start) / 1000000.0;
    }

    /**
     * Get the time as formatted string (e.g. 334.22ms)
     * @return the time in milliseconds as formatted string
     */
    public String getAsString() {
        return String.format("%.2fms.", get());
    }
    
    private static long measure() {
        return System.nanoTime();
    }

}
