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

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.terasology.monitoring.Activity;

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
