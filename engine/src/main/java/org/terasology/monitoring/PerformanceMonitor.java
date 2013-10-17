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
package org.terasology.monitoring;

import gnu.trove.map.TObjectDoubleMap;
import org.terasology.monitoring.impl.NullPerformanceMonitor;
import org.terasology.monitoring.impl.PerformanceMonitorImpl;
import org.terasology.monitoring.impl.PerformanceMonitorInternal;

/**
 * Maintains a running average of time taken by different activities. Activities call to denote when they
 * start and stop.
 * <p/>
 * Activities may be nested, and while a nested activity is running the out activities are paused and time passing
 * is not assigned to them.
 * <p/>
 * Performance monitor is intended only for use by the main thread of Terasology, and does not handle
 * activities being started and ended on other threads at this time.
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class PerformanceMonitor {
    private static PerformanceMonitorInternal instance;

    static {
        instance = new NullPerformanceMonitor();
    }

    private PerformanceMonitor() {
    }

    /**
     * Indicates the start of an activity. All started activities must be ended with endActivity(). Activities may
     * be nested.
     *
     * @param activity The name of the activity stating.
     */
    public static void startActivity(String activity) {
        instance.startActivity(activity);
    }

    /**
     * Indicates the end of the last started activity.
     */
    public static void endActivity() {
        instance.endActivity();
    }

    /**
     * Should be called once per frame, drops old information and updates the metrics.
     */
    public static void rollCycle() {
        instance.rollCycle();
    }

    /**
     * @return A mapping of activities to a running mean of time it has taken over a number of frames.
     */
    public static TObjectDoubleMap<String> getRunningMean() {
        return instance.getRunningMean();
    }

    /**
     * @return A mapping of activities to the largest cost over recent frames, decayed by time.
     */
    public static TObjectDoubleMap<String> getDecayingSpikes() {
        return instance.getDecayingSpikes();
    }

    /**
     * Allows the enabling/deactivation of the Performance Monitoring system.
     * When disabled calls to startActivity()/endActivity() and rollCycle() are ignored
     * and all data is purged.
     *
     * @param enabled Turns the performance monitoring system on or off.
     */
    public static void setEnabled(boolean enabled) {
        if (enabled && !(instance instanceof PerformanceMonitorImpl)) {
            instance = new PerformanceMonitorImpl();
        } else if (!enabled && !(instance instanceof NullPerformanceMonitor)) {
            instance = new NullPerformanceMonitor();
        }
    }

}
