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

    private TObjectDoubleProcedure<String> decayLargestExecutionTime;
    private TObjectLongProcedure<String>   updateExecutionTimeTotalAndSpikeData;
    private TObjectLongProcedure<String>   updateAllocatedMemoryTotal;
    private TObjectLongProcedure<String>   removeExpiredExecutionTimeValueFromTotal;
    private TObjectLongProcedure<String>   removeExpiredAllocatedMemoryValueFromTotal;

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

        decayLargestExecutionTime                  = new DecayerOfActivityLargestExecutionTime();
        updateExecutionTimeTotalAndSpikeData       = new UpdaterOfActivityExecutionTimeTotalAndSpikeData();
        updateAllocatedMemoryTotal                 = new UpdaterOfActivityAllocatedMemoryTotal();
        removeExpiredExecutionTimeValueFromTotal   = new RemoverFromTotalOfActivityExpiredExecutionTimeValue();
        removeExpiredAllocatedMemoryValueFromTotal = new RemoverFromTotalOfActivityExpiredAllocatedMemoryValue();

        timerTicksPerSecond = 1000;
        timeFactor = 1000.0 / timerTicksPerSecond;

        timer = (EngineTime) CoreRegistry.get(Time.class);
        mainThread = Thread.currentThread();
    }

    public void rollCycle() {
        executionData.add(currentExecutionData);
        allocationData.add(currentAllocationData);

        spikeData.forEachEntry(decayLargestExecutionTime);
        currentExecutionData.forEachEntry(updateExecutionTimeTotalAndSpikeData);
        currentAllocationData.forEachEntry(updateAllocatedMemoryTotal);

        while (executionData.size() > RETAINED_CYCLES) {
            executionData.get(0).forEachEntry(removeExpiredExecutionTimeValueFromTotal);
            executionData.remove(0);
        }

        while (allocationData.size() > RETAINED_CYCLES) {
            allocationData.get(0).forEachEntry(removeExpiredAllocatedMemoryValueFromTotal);
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
        TObjectDoubleMap<String> activityToMeanMap = new TObjectDoubleHashMap<>();
        double factor = timeFactor / executionData.size();

        // here we instantiate a new procedure every call in the hope that multiple thread calling this method won't interfere with each other
        TObjectLongProcedure<String> setExecutionTimeRunningMean = new SetterOfActivityToRunningMeanMapEntry(activityToMeanMap, factor);

        runningExecutionTotals.forEachEntry(setExecutionTimeRunningMean);

        return activityToMeanMap;
    }

    public TObjectDoubleMap<String> getDecayingSpikes() {
        return spikeData;
    }

    @Override
    public TObjectDoubleMap<String> getAllocationMean() {
        TObjectDoubleMap<String> activityToMeanMap = new TObjectDoubleHashMap<>();
        double factor = 1.0 / allocationData.size();

        // here we instantiate a new procedure every call in the hope that multiple thread calling this method won't interfere with each other
        TObjectLongProcedure<String> setAllocatedMemoryRunningMean = new SetterOfActivityToRunningMeanMapEntry(activityToMeanMap, factor);

        runningAllocationTotals.forEachEntry(setAllocatedMemoryRunningMean);

        return activityToMeanMap;
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

    private class DecayerOfActivityLargestExecutionTime implements TObjectDoubleProcedure<String> {
        public boolean execute(String activityName, double executionTime) {
            spikeData.put(activityName, executionTime * DECAY_RATE);
            return true;
        }
    }

    private class UpdaterOfActivityExecutionTimeTotalAndSpikeData implements TObjectLongProcedure<String> {
        double time;
        double latestSpike;

        public boolean execute(String activityName, long latestExecutionTime) {
            runningExecutionTotals.adjustOrPutValue(activityName, latestExecutionTime, latestExecutionTime);
            time = latestExecutionTime * timeFactor;
            latestSpike = spikeData.get(activityName);
            if (time > latestSpike) {
                spikeData.put(activityName, time);
            }
            return true;
        }
    }

    private class UpdaterOfActivityAllocatedMemoryTotal implements TObjectLongProcedure<String> {
        public boolean execute(String activityName, long latestAllocatedMemory) {
            runningAllocationTotals.adjustOrPutValue(activityName, latestAllocatedMemory, latestAllocatedMemory);
            return true;
        }
    }

    private class RemoverFromTotalOfActivityExpiredExecutionTimeValue implements TObjectLongProcedure<String> {
        public boolean execute(String activityName, long expiredExecutionTime) {
            runningExecutionTotals.adjustValue(activityName, -expiredExecutionTime);
            return true;
        }
    }


    private class RemoverFromTotalOfActivityExpiredAllocatedMemoryValue implements TObjectLongProcedure<String> {
        public boolean execute(String activityName, long expiredAllocatedMemory) {
            runningAllocationTotals.adjustValue(activityName, -expiredAllocatedMemory);
            return true;
        }
    }

    private class SetterOfActivityToRunningMeanMapEntry implements TObjectLongProcedure<String> {
        private TObjectDoubleMap<String> activityToMeanMap;
        private double factor;

        public SetterOfActivityToRunningMeanMapEntry(TObjectDoubleMap<String> activityToMeanMap, double factor) {
            this.activityToMeanMap = activityToMeanMap;
            this.factor = factor;
        }

        public boolean execute(String activityName, long total) {
            if (total > 0) {
                activityToMeanMap.put(activityName, total * factor);
            }
            return true;
        }
    }

}
