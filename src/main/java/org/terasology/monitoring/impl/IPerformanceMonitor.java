/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import gnu.trove.map.TObjectIntMap;

/**
 * Base interface for performance monitor implementations.
 *
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
