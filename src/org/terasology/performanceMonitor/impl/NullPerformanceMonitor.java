package org.terasology.performanceMonitor.impl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class NullPerformanceMonitor implements IPerformanceMonitor {
    private TObjectDoubleMap<String> _metrics = new TObjectDoubleHashMap<String>();
    private TObjectIntMap<String> _threads = new TObjectIntHashMap<String>();

    public void startThread(String name) {
    }

    public void endThread(String name) {
    }

    public void rollCycle() {
    }

    public void startActivity(String activity) {
    }

    public void endActivity() {
    }

    public TObjectDoubleMap<String> getRunningMean() {
        return _metrics;
    }

    public TObjectDoubleMap<String> getDecayingSpikes() {
        return _metrics;
    }

    public TObjectIntMap<String> getRunningThreads() {
        return _threads;
    }

}
