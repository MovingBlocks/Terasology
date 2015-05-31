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
import org.terasology.monitoring.Activity;
import org.terasology.registry.CoreRegistry;

import java.util.Deque;
import java.util.List;

/**
 * Active implementation of Performance Monitor
 *
 *         TODO: Check to ensure activities are being started and stopped correctly
 *         TODO: Remove activities with 0 time
 */
public class PerformanceMonitorImpl implements PerformanceMonitorInternal {
    private static final int RETAINED_CYCLES = 60;
    private static final double DECAY_RATE = 0.98;
    private static final Activity OFF_THREAD_ACTIVITY = new NullActivity();
    // NullActivity instances are used by the NullPerformanceMonitor and for processes NOT running
    // on the main thread. Not strictly necessary (these processes are ignored by the PerformanceMonitor
    // anyway) an instance of this class offers a slight performance improvement over standard Activity
    // implementations as it doesn't call the PerformanceMonitor.endActivity() method.

    private final Activity activityInstance = new ActivityInstance();

    private Deque<ActivityInfo> activityStack;
    private List<TObjectLongMap<String>> executionData;
    private List<TObjectLongMap<String>> allocationData;
    private      TObjectLongMap<String>  currentExecutionData;
    private      TObjectLongMap<String>  currentAllocationData;
    private      TObjectLongMap<String>  runningExecutionTotals;
    private      TObjectLongMap<String>  runningAllocationTotals;
    private    TObjectDoubleMap<String>  spikeData;

    private long timerTicksPerSecond;
    private double timeFactor;

    private Thread mainThread;
    private EngineTime timer;

    public PerformanceMonitorImpl() {
        activityStack  = Queues.newArrayDeque();
        executionData  = Lists.newLinkedList();
        allocationData = Lists.newLinkedList();
        currentExecutionData    = new TObjectLongHashMap<>();
        currentAllocationData   = new TObjectLongHashMap<>();
        runningExecutionTotals  = new TObjectLongHashMap<>();
        runningAllocationTotals = new TObjectLongHashMap<>();
        spikeData               = new TObjectDoubleHashMap<>();

        timerTicksPerSecond = 1000;
        timeFactor = 1000.0 / timerTicksPerSecond;

        timer = (EngineTime) CoreRegistry.get(Time.class);
        mainThread = Thread.currentThread();
    }

    public void rollCycle() {
        executionData.add(currentExecutionData);
        allocationData.add(currentAllocationData);

        // decays all spikes
        spikeData.forEachEntry(new TObjectDoubleProcedure<String>() {
            public boolean execute(String activityName, double executionTime) {
                spikeData.put(activityName, executionTime * DECAY_RATE);
                return true;
            }
        });

        // updates execution time totals and spike data
        currentExecutionData.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String activityName, long executionTime) {
                runningExecutionTotals.adjustOrPutValue(activityName, executionTime, executionTime);
                double time = executionTime * timeFactor;
                double latestSpike = spikeData.get(activityName);
                if (time > latestSpike) {
                    spikeData.put(activityName, time);
                }
                return true;
            }
        });

        // updates memory allocation totals
        currentAllocationData.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String activityName, long allocationValue) {
                runningAllocationTotals.adjustOrPutValue(activityName, allocationValue, allocationValue);
                return true;
            }
        });

        // remove old execution times data
        while (executionData.size() > RETAINED_CYCLES) {
            executionData.get(0).forEachEntry(new TObjectLongProcedure<String>() {
                public boolean execute(String activityName, long v) {
                    runningExecutionTotals.adjustValue(activityName, -v);
                    return true;
                }
            });
            executionData.remove(0);
        }

        // removes old memory allocation data
        while (allocationData.size() > RETAINED_CYCLES) {
            allocationData.get(0).forEachEntry(new TObjectLongProcedure<String>() {
                public boolean execute(String activityName, long v) {
                    runningAllocationTotals.adjustValue(activityName, -v);
                    return true;
                }
            });
            allocationData.remove(0);
        }

        currentExecutionData = new TObjectLongHashMap<>();
        currentAllocationData = new TObjectLongHashMap<>();
    }

    public Activity startActivity(String activityName) {
        if (Thread.currentThread() != mainThread) {
            return OFF_THREAD_ACTIVITY;
        }

        ActivityInfo newActivity = new ActivityInfo(activityName).initialize();

        if (!activityStack.isEmpty()) {
            ActivityInfo currentActivity = activityStack.peek();
            currentActivity.ownTime += newActivity.startTime - ((currentActivity.resumeTime > 0) ? currentActivity.resumeTime : currentActivity.startTime);
            currentActivity.ownMem += (currentActivity.startMem - newActivity.startMem > 0) ? currentActivity.startMem - newActivity.startMem : 0;
        }

        activityStack.push(newActivity);
        return activityInstance;
    }

    public void endActivity() {
        if (Thread.currentThread() != mainThread || activityStack.isEmpty()) {
            return;
        }

        ActivityInfo oldActivity = activityStack.pop();

        long endTime = timer.getRawTimeInMs();
        long totalTime = (oldActivity.resumeTime > 0) ? oldActivity.ownTime + endTime - oldActivity.resumeTime : endTime - oldActivity.startTime;
        currentExecutionData.adjustOrPutValue(oldActivity.name, totalTime, totalTime);
        
        long endMem = Runtime.getRuntime().freeMemory();
        long totalMem = (oldActivity.startMem - endMem > 0) ? oldActivity.startMem - endMem + oldActivity.ownMem : oldActivity.ownMem;
        currentAllocationData.adjustOrPutValue(oldActivity.name, totalMem, totalMem);

        if (!activityStack.isEmpty()) {
            ActivityInfo currentActivity = activityStack.peek();
            currentActivity.resumeTime = endTime;
            currentActivity.startMem = endMem;
        }
    }

    public TObjectDoubleMap<String> getRunningMean() {
        final TObjectDoubleMap<String> activityToMeanMap = new TObjectDoubleHashMap<>();
        final double factor = timeFactor / executionData.size();
        runningExecutionTotals.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String activityName, long executionTotal) {
                if (executionTotal > 0) {
                    activityToMeanMap.put(activityName, executionTotal * factor);
                }
                return true;
            }
        });
        return activityToMeanMap;
    }

    public TObjectDoubleMap<String> getDecayingSpikes() {
        return spikeData;
    }

    @Override
    public TObjectDoubleMap<String> getAllocationMean() {
        final TObjectDoubleMap<String> activityToAllocationMap = new TObjectDoubleHashMap<>();
        final double factor = 1.0 / allocationData.size();
        runningAllocationTotals.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String activityName, long allocationTotal) {
                if (allocationTotal > 0) {
                    activityToAllocationMap.put(activityName, allocationTotal * factor);
                }
                return true;
            }
        });
        return activityToAllocationMap;
    }

    private class ActivityInfo {
        public String name;
        public long startTime;
        public long resumeTime;
        public long ownTime;
        public long startMem;
        public long ownMem;

        public ActivityInfo(String activityName) {
            this.name = activityName;
        }

        public ActivityInfo initialize() {
            this.startTime = timer.getRawTimeInMs();
            this.startMem = Runtime.getRuntime().freeMemory();
            return this;
        }
    }

    private class ActivityInstance implements Activity {

        @Override
        public void close() {
            endActivity();
        }
    }
}
