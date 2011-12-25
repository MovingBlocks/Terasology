/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.utilities;

import java.util.HashMap;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockmaniaProfiler {

    private static long _prevTime;
    private static HashMap<String, Double> _checkpointDurations = new HashMap<String, Double>();

    private static HashMap<String, Double> _results;

    public static void begin() {
        _prevTime = System.nanoTime();
    }

    public static void log(String label) {
        long now = System.nanoTime();
        double value = (now - _prevTime);
        _checkpointDurations.put(label, value);
        _prevTime = now;
    }

    public static void end() {
        _results = new HashMap<String, Double>();

        double sum = 0.0;

        for (Double value : _checkpointDurations.values())
            sum += value;

        for (String label : _checkpointDurations.keySet()) {
            double percentage = (_checkpointDurations.get(label) / sum);
            _results.put(label, percentage);
        }
    }

    public static HashMap<String, Double> getResults() {
        return _results;
    }
}
