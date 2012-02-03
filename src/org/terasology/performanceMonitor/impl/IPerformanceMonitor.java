package org.terasology.performanceMonitor.impl;

import gnu.trove.map.TObjectDoubleMap;

/**
 * Base interface for performance monitor implementations.
 * @author Immortius <immortius@gmail.com>
 */
public interface IPerformanceMonitor {
    void rollCycle();
    void startActivity(String activity);
    void endActivity();
    TObjectDoubleMap<String> getRunningMean();
    TObjectDoubleMap<String> getDecayingSpikes();
}
