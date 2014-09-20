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
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectLongProcedure;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.registry.CoreRegistry;

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
    private List<TObjectLongMap<String>> allocationData;
    private TObjectLongMap<String> currentData;
    private TObjectLongMap<String> currentMemData;
    private TObjectLongMap<String> runningTotals;
    private TObjectLongMap<String> runningAllocationTotals;
    private long timerTicksPerSecond;
    private TObjectDoubleMap<String> spikeData;
    private double timeFactor;

    private Thread mainThread;
    private EngineTime timer;

    public PerformanceMonitorImpl() {
        timer = (EngineTime) CoreRegistry.get(Time.class);
        activityStack = Queues.newArrayDeque();
        metricData = Lists.newLinkedList();
        allocationData = Lists.newLinkedList();
        runningTotals = new TObjectLongHashMap<>();
        runningAllocationTotals = new TObjectLongHashMap<>();
        timerTicksPerSecond = 1000;
        currentData = new TObjectLongHashMap<>();
        currentMemData = new TObjectLongHashMap<>();
        spikeData = new TObjectDoubleHashMap<>();
        timeFactor = 1000.0 / timerTicksPerSecond;
        mainThread = Thread.currentThread();

    }

    public void rollCycle() {
        metricData.add(currentData);
        allocationData.add(currentMemData);
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

        currentMemData.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String s, long v) {
                runningAllocationTotals.adjustOrPutValue(s, v, v);
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
        while (allocationData.size() > RETAINED_CYCLES) {
            allocationData.get(0).forEachEntry(new TObjectLongProcedure<String>() {
                public boolean execute(String s, long v) {
                    runningAllocationTotals.adjustValue(s, -v);
                    return true;
                }
            });
            allocationData.remove(0);
        }
        currentData = new TObjectLongHashMap<>();
        currentMemData = new TObjectLongHashMap<>();
    }

    public void startActivity(String activity) {
        if (Thread.currentThread() != mainThread) {
            return;
        }
        Activity newActivity = new Activity();
        newActivity.name = activity;
        newActivity.startTime = timer.getRawTimeInMs();
        newActivity.startMem = Runtime.getRuntime().freeMemory();
        if (!activityStack.isEmpty()) {
            Activity currentActivity = activityStack.peek();
            currentActivity.ownTime += newActivity.startTime - ((currentActivity.resumeTime > 0) ? currentActivity.resumeTime : currentActivity.startTime);
            currentActivity.ownMem += (currentActivity.startMem - newActivity.startMem > 0) ? currentActivity.startMem - newActivity.startMem : 0;
        }

        activityStack.push(newActivity);
    }

    public void endActivity() {
        if (Thread.currentThread() != mainThread || activityStack.isEmpty()) {
            return;
        }

        Activity oldActivity = activityStack.pop();
        long time = timer.getRawTimeInMs();
        long total = (oldActivity.resumeTime > 0) ? oldActivity.ownTime + time - oldActivity.resumeTime : time - oldActivity.startTime;
        currentData.adjustOrPutValue(oldActivity.name, total, total);
        long endMem = Runtime.getRuntime().freeMemory();
        long totalMem = (oldActivity.startMem - endMem > 0) ? oldActivity.startMem - endMem + oldActivity.ownMem : oldActivity.ownMem;
        currentMemData.adjustOrPutValue(oldActivity.name, totalMem, totalMem);

        if (!activityStack.isEmpty()) {
            Activity currentActivity = activityStack.peek();
            currentActivity.resumeTime = time;
            currentActivity.startMem = endMem;
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

    @Override
    public TObjectDoubleMap<String> getAllocationMean() {
        final TObjectDoubleMap<String> result = new TObjectDoubleHashMap<String>();
        final double factor = 1.0 / allocationData.size();
        runningAllocationTotals.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String s, long l) {
                if (l > 0) {
                    result.put(s, l * factor);
                }
                return true;
            }
        });
        return result;
    }

    private static class Activity {
        public String name;
        public long startTime;
        public long resumeTime;
        public long ownTime;
        public long startMem;
        public long ownMem;
    }
}
