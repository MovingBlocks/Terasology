// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import gnu.trove.map.TObjectDoubleMap;
import org.terasology.engine.monitoring.Activity;

/**
 * Base interface for performance monitor implementations.
 *
 */
public interface PerformanceMonitorInternal {
    void rollCycle();

    Activity startActivity(String activity);

    void endActivity();

    TObjectDoubleMap<String> getRunningMean();

    TObjectDoubleMap<String> getDecayingSpikes();

    TObjectDoubleMap<String> getAllocationMean();
}
