package org.terasology.performanceMonitor.impl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;

/**
 * Base interface for performance monitor implementations.
 * @author Immortius <immortius@gmail.com>
 */
public interface IPerformanceMonitor {
    void rollCycle();
    void startActivity(String activity);
    void endActivity();
    void startThread(String name);
    void endThread(String name);
    TObjectIntMap<String> getRunningThreads();
    TObjectDoubleMap<String> getRunningMean();
    TObjectDoubleMap<String> getDecayingSpikes();
}
