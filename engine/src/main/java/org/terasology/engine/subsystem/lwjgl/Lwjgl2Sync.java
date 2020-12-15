// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.glfw.GLFW;

/**
 * A highly accurate sync method that continually adapts to the system it runs on to provide reliable results.
 */
public final class Lwjgl2Sync {

    /**
     * number of nano seconds in a second
     */
    private static final long NANOS_IN_SECOND = 1000L * 1000L * 1000L;
    /**
     * for calculating the averages the previous sleep/yield times are stored
     */
    private static final RunningAvg SLEEP_DURATIONS = new RunningAvg(10);
    private static final RunningAvg YIELD_DURATIONS = new RunningAvg(10);
    /**
     * The time to sleep/yield until the next frame
     */
    private static long nextFrame;
    /**
     * whether the initialisation code has run
     */
    private static boolean initialised;

    private Lwjgl2Sync() {
    }

    /**
     * An accurate sync method that will attempt to run at a constant frame rate. It should be called once every frame.
     *
     * @param fps - the desired frame rate, in frames per second
     */
    public static void sync(int fps) {
        if (fps <= 0) {
            return;
        }
        if (!initialised) {
            initialise();
        }

        try {
            // sleep until the average sleep time is greater than the time remaining till nextFrame
            for (long t0 = getTime(), t1; (nextFrame - t0) > SLEEP_DURATIONS.avg(); t0 = t1) {
                //noinspection BusyWait
                Thread.sleep(1);
                SLEEP_DURATIONS.add((t1 = getTime()) - t0); // update average sleep time
            }

            // slowly dampen sleep average if too high to avoid yielding too much
            SLEEP_DURATIONS.dampenForLowResTicker();

            // yield until the average yield time is greater than the time remaining till nextFrame
            for (long t0 = getTime(), t1; (nextFrame - t0) > YIELD_DURATIONS.avg(); t0 = t1) {
                Thread.yield();
                YIELD_DURATIONS.add((t1 = getTime()) - t0); // update average yield time
            }
        } catch (InterruptedException ignored) {
        }

        // schedule next frame, drop frame(s) if already too late for next frame
        nextFrame = Math.max(nextFrame + NANOS_IN_SECOND / fps, getTime());
    }

    /**
     * This method will initialise the sync method by setting initial values for sleepDurations/yieldDurations and
     * nextFrame.
     * <p>
     * If running on windows it will start the sleep timer fix.
     */
    private static void initialise() {
        initialised = true;

        SLEEP_DURATIONS.init(1000 * 1000);
        YIELD_DURATIONS.init((int) (-(getTime() - getTime()) * 1.333));

        nextFrame = getTime();

        String osName = System.getProperty("os.name");

        if (osName.startsWith("Win")) {
            // On windows the sleep functions can be highly inaccurate by
            // over 10ms making in unusable. However it can be forced to
            // be a bit more accurate by running a separate sleeping daemon
            // thread.
            Thread timerAccuracyThread = new Thread(() -> {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (Exception ignored) {
                }
            });

            timerAccuracyThread.setName("LWJGL Timer");
            timerAccuracyThread.setDaemon(true);
            timerAccuracyThread.start();
        }
    }

    /**
     * Get the system time in nano seconds
     *
     * @return will return the current time in nano's
     */
    private static long getTime() {
        return (long) (GLFW.glfwGetTime() * NANOS_IN_SECOND);
    }

    private static class RunningAvg {
        private static final long DAMPEN_THRESHOLD = 10 * 1000L * 1000L; // 10ms
        private static final float DAMPEN_FACTOR = 0.9f; // don't change: 0.9f is exactly right!
        private final long[] slots;
        private int offset;

        RunningAvg(int slotCount) {
            this.slots = new long[slotCount];
            this.offset = 0;
        }

        public void init(long value) {
            while (this.offset < this.slots.length) {
                this.slots[this.offset++] = value;
            }
        }

        public void add(long value) {
            this.slots[this.offset++ % this.slots.length] = value;
            this.offset %= this.slots.length;
        }

        public long avg() {
            long sum = 0;
            for (long slot : this.slots) {
                sum += slot;
            }
            return sum / this.slots.length;
        }

        public void dampenForLowResTicker() {
            if (this.avg() > DAMPEN_THRESHOLD) {
                for (int i = 0; i < this.slots.length; i++) {
                    this.slots[i] *= (long) DAMPEN_FACTOR;
                }
            }
        }
    }
}
