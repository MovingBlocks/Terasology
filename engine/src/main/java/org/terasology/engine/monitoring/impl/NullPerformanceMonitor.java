// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.terasology.engine.monitoring.Activity;

/**
 */
public class NullPerformanceMonitor implements PerformanceMonitorInternal {
    private static final NullActivity NULL_ACTIVITY = new NullActivity();
    private TObjectDoubleMap<String> metrics = new TObjectDoubleHashMap<>();

    @Override
    public void rollCycle() {
    }

    @Override
    public Activity startActivity(String activity) {
        return NULL_ACTIVITY;
    }

    @Override
    public void endActivity() {
    }

    @Override
    public TObjectDoubleMap<String> getRunningMean() {
        return metrics;
    }

    @Override
    public TObjectDoubleMap<String> getDecayingSpikes() {
        return metrics;
    }

    @Override
    public TObjectDoubleMap<String> getAllocationMean() {
        return metrics;
    }

}
