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
package com.github.begla.blockmania.debug;

import java.util.HashMap;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockmaniaProfiler {

    private static long _prevTime;
    private static HashMap<String, Double> _checkpointDurations = new HashMap<String, Double>();
    private static String _title;

    public static void begin(String title) {
        _title = title;

        System.out.println("===================");
        System.out.println("BEGIN PROFILING :: " + _title);
        System.out.println("===================");

        _checkpointDurations.clear();
    }

    public static void log(String label) {
        if (_checkpointDurations.containsKey(label)) {
            double value = _checkpointDurations.get(label);
            value = (value + (System.nanoTime() - _prevTime)) / 2.0;
            _checkpointDurations.put(label, value);
        } else {
            double value = (System.nanoTime() - _prevTime);
            _checkpointDurations.put(label, value);
        }

        _prevTime = System.nanoTime();
    }

    public static void end() {
        double sum = 0.0;

        for (Double value : _checkpointDurations.values())
            sum += value;

        for (String label : _checkpointDurations.keySet()) {
            System.out.println(label + " : " + _checkpointDurations.get(label) + " ns (" + String.format("%.5f", (_checkpointDurations.get(label) / sum) * 100.0) + "%)");
        }

        System.out.println("===================");
        System.out.println("END PROFILING :: " + _title);
        System.out.println("===================");
    }
}
