package org.terasology.performanceMonitor.impl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class NullPerformanceMonitor implements IPerformanceMonitor
{
    private TObjectDoubleMap<String> _metrics = new TObjectDoubleHashMap<String>();

    public void rollCycle() {}
    public void startActivity(String activity) {}
    public void endActivity() {}
    public TObjectDoubleMap<String> getRunningMean()
    {
        return _metrics;
    }
    public TObjectDoubleMap<String> getDecayingSpikes()
    {
        return _metrics;
    }
}
