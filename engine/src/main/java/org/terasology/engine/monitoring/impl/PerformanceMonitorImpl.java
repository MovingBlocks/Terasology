// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectLongProcedure;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.monitoring.Activity;
import org.terasology.engine.registry.CoreRegistry;

import java.util.Deque;
import java.util.List;

/**
 * Active implementation of Performance Monitor
 */
// TODO: Check to ensure activities are being started and stopped correctly
// TODO: Remove activities with 0 time
public class PerformanceMonitorImpl implements PerformanceMonitorInternal {
    private static final int RETAINED_CYCLES = 60;
    private static final double DECAY_RATE = 0.98;
    private static final Activity OFF_THREAD_ACTIVITY = new NullActivity();
    // NullActivity instances are used by the NullPerformanceMonitor and for processes NOT running
    // on the main thread. Not strictly necessary (these processes are ignored by the PerformanceMonitor
    // anyway) an instance of this class offers a slight performance improvement over standard Activity
    // implementations as it doesn't call the PerformanceMonitor.endActivity() method.

    private final Activity activityInstance = new ActivityInstance();

    private final Deque<ActivityInfo> activityStack;

    private final List<TObjectLongMap<String>> executionData;
    private final List<TObjectLongMap<String>> allocationData;

    private TObjectLongMap<String> currentExecutionData;
    private TObjectLongMap<String> currentAllocationData;
    private final TObjectLongMap<String> runningExecutionTotals;
    private final TObjectLongMap<String> runningAllocationTotals;
    private final TObjectDoubleMap<String> spikeData;

    private final TObjectDoubleProcedure<String> decayLargestExecutionTime;
    private final TObjectLongProcedure<String> updateExecutionTimeTotalAndSpikeData;
    private final TObjectLongProcedure<String> updateAllocatedMemoryTotal;
    private final TObjectLongProcedure<String> removeExpiredExecutionTimeValueFromTotal;
    private final TObjectLongProcedure<String> removeExpiredAllocatedMemoryValueFromTotal;

    private final SetterOfActivityToRunningMeanMapEntry setExecutionTimeRunningMean;
    private final SetterOfActivityToRunningMeanMapEntry setAllocatedMemoryRunningMean;

    private final Thread mainThread;
    private final EngineTime timer;

    public PerformanceMonitorImpl() {
        activityStack  = Queues.newArrayDeque();
        executionData  = Lists.newLinkedList();
        allocationData = Lists.newLinkedList();
        currentExecutionData = new TObjectLongHashMap<>();
        currentAllocationData = new TObjectLongHashMap<>();
        runningExecutionTotals = new TObjectLongHashMap<>();
        runningAllocationTotals = new TObjectLongHashMap<>();
        spikeData = new TObjectDoubleHashMap<>();

        decayLargestExecutionTime  = new DecayerOfActivityLargestExecutionTime();
        updateExecutionTimeTotalAndSpikeData = new UpdaterOfActivityExecutionTimeTotalAndSpikeData();
        updateAllocatedMemoryTotal = new UpdaterOfActivityAllocatedMemoryTotal();
        removeExpiredExecutionTimeValueFromTotal  = new RemoverFromTotalOfActivityExpiredExecutionTimeValue();
        removeExpiredAllocatedMemoryValueFromTotal = new RemoverFromTotalOfActivityExpiredAllocatedMemoryValue();

        setExecutionTimeRunningMean = new SetterOfActivityToRunningMeanMapEntry();
        setAllocatedMemoryRunningMean = new SetterOfActivityToRunningMeanMapEntry();

        timer = (EngineTime) CoreRegistry.get(Time.class);
        mainThread = Thread.currentThread();
    }

    @Override
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

    @Override
    public Activity startActivity(String activityName) {
        if (!Thread.currentThread().equals(mainThread)) {
            return OFF_THREAD_ACTIVITY;
        }

        ActivityInfo newActivity = new ActivityInfo(activityName).initialize();

        if (!activityStack.isEmpty()) {
            ActivityInfo currentActivity = activityStack.peek();
            currentActivity.ownTime += newActivity.startTime - ((currentActivity.resumeTime > 0)
                    ? currentActivity.resumeTime
                    : currentActivity.startTime);
            currentActivity.ownMem += (currentActivity.startMem - newActivity.startMem > 0)
                    ? currentActivity.startMem - newActivity.startMem
                    : 0;
        }

        activityStack.push(newActivity);
        return activityInstance;
    }

    @Override
    public void endActivity() {
        if (!Thread.currentThread().equals(mainThread) || activityStack.isEmpty()) {
            return;
        }

        ActivityInfo oldActivity = activityStack.pop();

        long endTime = timer.getRealTimeInMs();
        long totalTime = (oldActivity.resumeTime > 0)
                ? oldActivity.ownTime + endTime - oldActivity.resumeTime
                : endTime - oldActivity.startTime;
        currentExecutionData.adjustOrPutValue(oldActivity.name, totalTime, totalTime);
        
        long endMem = Runtime.getRuntime().freeMemory();
        long totalMem = (oldActivity.startMem - endMem > 0)
                ? oldActivity.startMem - endMem + oldActivity.ownMem
                : oldActivity.ownMem;
        currentAllocationData.adjustOrPutValue(oldActivity.name, totalMem, totalMem);

        if (!activityStack.isEmpty()) {
            ActivityInfo currentActivity = activityStack.peek();
            currentActivity.resumeTime = endTime;
            currentActivity.startMem = endMem;
        }
    }

    @Override
    public TObjectDoubleMap<String> getRunningMean() {
        TObjectDoubleMap<String> activityToMeanMap = new TObjectDoubleHashMap<>();
        setExecutionTimeRunningMean.setActivityToMeanMap(activityToMeanMap);
        setExecutionTimeRunningMean.setFactor(1.0 / executionData.size());

        runningExecutionTotals.forEachEntry(setExecutionTimeRunningMean);

        return activityToMeanMap;
    }

    @Override
    public TObjectDoubleMap<String> getDecayingSpikes() {
        return spikeData;
    }

    @Override
    public TObjectDoubleMap<String> getAllocationMean() {
        TObjectDoubleMap<String> activityToMeanMap = new TObjectDoubleHashMap<>();
        setAllocatedMemoryRunningMean.setActivityToMeanMap(activityToMeanMap);
        setAllocatedMemoryRunningMean.setFactor(1.0 / allocationData.size());

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

         ActivityInfo(String activityName) {
            this.name = activityName;
        }

        public ActivityInfo initialize() {
            this.startTime = timer.getRealTimeInMs();
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
        @Override
        public boolean execute(String activityName, double executionTime) {
            spikeData.put(activityName, executionTime * DECAY_RATE);
            return true;
        }
    }

    private class UpdaterOfActivityExecutionTimeTotalAndSpikeData implements TObjectLongProcedure<String> {
        double latestSpike;

        @Override
        public boolean execute(String activityName, long latestExecutionTime) {
            runningExecutionTotals.adjustOrPutValue(activityName, latestExecutionTime, latestExecutionTime);
            latestSpike = spikeData.get(activityName);
            if (latestExecutionTime > latestSpike) {
                spikeData.put(activityName, latestExecutionTime);
            }
            return true;
        }
    }

    private class UpdaterOfActivityAllocatedMemoryTotal implements TObjectLongProcedure<String> {
        @Override
        public boolean execute(String activityName, long latestAllocatedMemory) {
            runningAllocationTotals.adjustOrPutValue(activityName, latestAllocatedMemory, latestAllocatedMemory);
            return true;
        }
    }

    private class RemoverFromTotalOfActivityExpiredExecutionTimeValue implements TObjectLongProcedure<String> {
        @Override
        public boolean execute(String activityName, long expiredExecutionTime) {
            runningExecutionTotals.adjustValue(activityName, -expiredExecutionTime);
            return true;
        }
    }


    private class RemoverFromTotalOfActivityExpiredAllocatedMemoryValue implements TObjectLongProcedure<String> {
        @Override
        public boolean execute(String activityName, long expiredAllocatedMemory) {
            runningAllocationTotals.adjustValue(activityName, -expiredAllocatedMemory);
            return true;
        }
    }

    private class SetterOfActivityToRunningMeanMapEntry implements TObjectLongProcedure<String> {
        private TObjectDoubleMap<String> activityToMeanMap;
        private double factor;

        public SetterOfActivityToRunningMeanMapEntry setActivityToMeanMap(TObjectDoubleMap<String> newActivityToMeanMap) {
            this.activityToMeanMap = newActivityToMeanMap;
            return this;
        }

        public SetterOfActivityToRunningMeanMapEntry setFactor(double newFactor) {
            this.factor = newFactor;
            return this;
        }

        @Override
        public boolean execute(String activityName, long total) {
            if (total > 0) {
                activityToMeanMap.put(activityName, total * factor);
            }
            return true;
        }
    }

}
