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
 * Maintains a running average of execution times and memory allocated by different activities.
 * Activities call to denote when they start and stop.
 * <br><br>
 * Activities may be nested, and while a nested activity is running the collection of data from outer activities
 * is paused: time passing and allocated memory are not assigned to them.
 * <br><br>
 * Performance monitor is intended only for use by the main thread of Terasology, and does not handle
 * activities being started and ended on other threads at this time.
 */
public final class PerformanceMonitor {
    private static PerformanceMonitorInternal instance;

    static {
        instance = new NullPerformanceMonitor();
    }

    private PerformanceMonitor() {
    }

    /**
     * Indicates the start of an activity. All started activities must either be ended with a call to endActivity()
     * (see example 1) or take advantage of the autocloseable interface implemented by Activity (see example 2).<br>
     * <br>
     *
     * Example 1 - explicitly ending an activity:
     * <pre>
     * PerformanceMonitor.startActivity("myActivity")
     * doSomething();
     * PerformanceMonitor.endActivity()
     * </pre>
     *
     * Example 2 - the end of the activity is handled internally:
     * <pre>
     * try (Activity ignored = PerformanceMonitor.startActivity("myActivity") {
     *     doSomething();
     * }
     * </pre>
     *
     * The latter case is particularly useful whenever the activity's code has a number of possible exit paths,
     * as it might be undesirable or simply non-trivial to add an endActivity() call at the end of each.<br>
     * <br>
     *
     * Activities may be nested. Example: (the indentation pattern is not strictly necessary)
     * <pre>
     * PerformanceMonitor.startActivity("myActivity")
     * doSomething();
     *
     *     PerformanceMonitor.startActivity("myNestedActivity")
     *     doSomethingNested();
     *     PerformanceMonitor.endActivity()
     *
     * doSomethingElse()
     * PerformanceMonitor.endActivity()
     * </pre>
     *
     * @param activityName the name of the activity starting.
     */
    public static Activity startActivity(String activityName) {
        return instance.startActivity(activityName);
    }

    /**
     * Indicates the end of the last started activity.
     */
    public static void endActivity() {
        instance.endActivity();
    }

    /**
     * Drops old information and updates the metrics. Should be called once per frame.
     */
    public static void rollCycle() {
        instance.rollCycle();
    }

    /**
     * Returns a mapping from the name of an activity to a running mean of its execution times, over a number of cycles.
     * <br><br>
     * Activities may be nested, and while a nested activity is running the collection of data from outer activities
     * is paused and time passing is not assigned to them.
     *
     * @return a mapping from activity name to running mean of execution times.
     */
    public static TObjectDoubleMap<String> getRunningMean() {
        return instance.getRunningMean();
    }

    /**
     * Returns a mapping from the name of an activity to the activity's largest execution time, decayed by time.
     * <br><br>
     * Without decay this method would return the absolute largest execution time for each activity, since
     * it was first started. Instead, this method returns the largest -most recent- execution time for
     * each activity.
     *
     * @return a mapping from activity name to largest most recent execution time per cycle.
     */
    // TODO: change to return the largest execution time over the monitored interval - no decay involved. See comment in
    // TODO: https://github.com/emanuele3d/Terasology/commit/36cf9bf23b42f76793a5d5968ef4e25986aa9706#commitcomment-11445526
    public static TObjectDoubleMap<String> getDecayingSpikes() {
        return instance.getDecayingSpikes();
    }

    /**
     * Returns a mapping from the name of an activity to a running mean of allocated memory during
     * the execution of the activity, over a number of cycles.
     * <br><br>
     * Activities may be nested, and while a nested activity is running the collection of data from
     * outer activities is paused and allocated memory is not assigned to them.
     * <br><br>
     * No guarantee can be given that the memory allocated during the execution of an activity is
     * entirely due to the activity. Other threads for example might increase or decrease the
     * figure.
     *
     * @return a mapping from activity name to running mean of allocated memory.
     */
    public static TObjectDoubleMap<String> getAllocationMean() {
        return instance.getAllocationMean();
    }

    /**
     * Enables or disables the Performance Monitoring system.
     * <br><br>
     * When disabled all data is purged and calls to startActivity()/endActivity() and rollCycle() are ignored.
     *
     * @param enabled True turns the Performance Monitoring system ON. False turns it OFF.
     */
    public static void setEnabled(boolean enabled) {
        if (enabled && !(instance instanceof PerformanceMonitorImpl)) {
            instance = new PerformanceMonitorImpl();
        } else if (!enabled && !(instance instanceof NullPerformanceMonitor)) {
            instance = new NullPerformanceMonitor();
        }
    }

}
