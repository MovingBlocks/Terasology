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
package org.terasology.monitoring.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gnu.trove.TCollections;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectLongProcedure;
import org.lwjgl.Sys;

import java.util.Deque;
import java.util.List;

/**
 * Active implementation of Performance Monitor
 *
 * @author Immortius <immortius@gmail.com>
 *         TODO: Check to ensure activities are being started and stopped correctly
 *         TODO: Remove activities with 0 time
 */
public class PerformanceMonitorImpl implements PerformanceMonitorInternal {
    private static final int RETAINED_CYCLES = 60;
    private static final double DECAY_RATE = 0.98;

    private Deque<Activity> activityStack;
    private List<TObjectLongMap<String>> metricData;
    private TObjectLongMap<String> currentData;
    private TObjectLongMap<String> runningTotals;
    private TObjectIntMap<String> runningThreads;
    private TObjectIntMap<String> stoppedThreads;
    private long timerTicksPerSecond;
    private TObjectDoubleMap<String> spikeData;
    private double timeFactor;
    private TObjectIntMap<String> lastRunningThreads;

    private Thread mainThread;

    public PerformanceMonitorImpl() {
        activityStack = Queues.newArrayDeque();
        metricData = Lists.newLinkedList();
        runningTotals = new TObjectLongHashMap<>();
        timerTicksPerSecond = Sys.getTimerResolution();
        currentData = new TObjectLongHashMap<>();
        spikeData = new TObjectDoubleHashMap<>();
        runningThreads = TCollections.synchronizedMap(new TObjectIntHashMap<String>());
        stoppedThreads = TCollections.synchronizedMap(new TObjectIntHashMap<String>());
        lastRunningThreads = new TObjectIntHashMap<>();
        timeFactor = 1000.0 / timerTicksPerSecond;
        mainThread = Thread.currentThread();

    }

    public void rollCycle() {
        metricData.add(currentData);
        spikeData.forEachEntry(new TObjectDoubleProcedure<String>() {
            public boolean execute(String s, double v) {
                spikeData.put(s, v * DECAY_RATE);
                return true;
            }
        });

        currentData.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String s, long v) {
                runningTotals.adjustOrPutValue(s, v, v);
                double time = v * timeFactor;
                double prev = spikeData.get(s);
                if (time > prev) {
                    spikeData.put(s, time);
                }
                return true;
            }
        });

        while (metricData.size() > RETAINED_CYCLES) {
            metricData.get(0).forEachEntry(new TObjectLongProcedure<String>() {
                public boolean execute(String s, long v) {
                    runningTotals.adjustValue(s, -v);
                    return true;
                }
            });
            metricData.remove(0);
        }
        currentData = new TObjectLongHashMap<String>();

        runningThreads.forEachEntry(new TObjectIntProcedure<String>() {
            public boolean execute(String s, int i) {
                lastRunningThreads.adjustOrPutValue(s, i, i);
                return true;
            }
        });
        TObjectIntMap<String> temp = runningThreads;
        temp.clear();
        runningThreads = stoppedThreads;
        stoppedThreads = temp;
        lastRunningThreads.retainEntries(new TObjectIntProcedure<String>() {
            public boolean execute(String s, int i) {
                return i > 0;
            }
        });

    }

    public void startActivity(String activity) {
        if (Thread.currentThread() != mainThread) {
            return;
        }
        Activity newActivity = new Activity();
        newActivity.name = activity;
        newActivity.startTime = Sys.getTime();
        if (!activityStack.isEmpty()) {
            Activity currentActivity = activityStack.peek();
            currentActivity.ownTime += newActivity.startTime - ((currentActivity.resumeTime > 0) ? currentActivity.resumeTime : currentActivity.startTime);
        }

        activityStack.push(newActivity);
    }

    public void endActivity() {
        if (Thread.currentThread() != mainThread || activityStack.isEmpty()) {
            return;
        }

        Activity oldActivity = activityStack.pop();
        long time = Sys.getTime();
        long total = (oldActivity.resumeTime > 0) ? oldActivity.ownTime + time - oldActivity.resumeTime : time - oldActivity.startTime;
        currentData.adjustOrPutValue(oldActivity.name, total, total);

        if (!activityStack.isEmpty()) {
            Activity currentActivity = activityStack.peek();
            currentActivity.resumeTime = time;
        }
    }

    public TObjectDoubleMap<String> getRunningMean() {
        final TObjectDoubleMap<String> result = new TObjectDoubleHashMap<String>();
        final double factor = timeFactor / metricData.size();
        runningTotals.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String s, long l) {
                if (l > 0) {
                    result.put(s, l * factor);
                }
                return true;
            }
        });
        return result;
    }

    public TObjectDoubleMap<String> getDecayingSpikes() {
        return spikeData;
    }

    public void startThread(String name) {
        runningThreads.adjustOrPutValue(name, 1, 1);
    }

    public void endThread(String name) {
        stoppedThreads.adjustOrPutValue(name, -1, -1);
    }

    public TObjectIntMap<String> getRunningThreads() {
        return lastRunningThreads;
    }

    private static class Activity {
        public String name;
        public long startTime;
        public long resumeTime;
        public long ownTime;
    }
}
