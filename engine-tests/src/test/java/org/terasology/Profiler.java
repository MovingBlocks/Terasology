/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

/**
 * HACK: DUPLICATE -- See Cities module !
 */
public final class Profiler {
    
    private static final Logger logger = LoggerFactory.getLogger(Profiler.class);
    
    private static final Map<Object, Long> TIME_MAP = new MapMaker().makeMap();
    
    private Profiler() {
        // private constructor
    }

    /**
     * Starts a measurement with a generated ID
     * @return the generated ID
     */
    public static Object start() {
        Object id = new Object();
        start(id);
        return id;
    }
    
    /**
     * Starts a measurement with the specified ID
     * @param id an identifier object
     */
    public static void start(Object id) {
        if (TIME_MAP.size() % 100 == 99) {
            logger.warn("Number of active measurements suspiciously large ({})", TIME_MAP.size());
        }
            
        long time = measure();
        
        if (TIME_MAP.put(id, time) != null) {
            logger.warn("ID {} already existed - overwriting..", id);
        }
    }
    
    /**
     * Get the measurement with the specified ID and stop the timer
     * @param id an identifier object
     * @return the time in milliseconds
     */
    public static double getAndStop(Object id) {
        double time = get(id);
        TIME_MAP.remove(id);
        return time;
    }

    /**
     * Get the time since start() was last called
     * @param id an identifier object
     * @return the time in milliseconds
     */
    public static double get(Object id) {
        Long start = TIME_MAP.get(id);
        long time = measure();

        if (start == null) {
            throw new IllegalArgumentException("Invalid id '" + String.valueOf(id) + "'");
        }

        return (time - start) / 1000000.0;
    }

    /**
     * Get the time since start() was last called as formatted string (e.g. 334.22ms)
     * @param id an identifier object
     * @return the time in milliseconds as formatted string
     */
    public static String getAsString(Object id) {
        double time = get(id);
        return String.format("%.2fms.", time);
    }
    
    /**
     * Get the time since start() was last called as formatted string (e.g. 334.22ms) and stop the timer
     * @param id an identifier object
     * @return the time in milliseconds as formatted string
     */
    public static String getAsStringAndStop(Object id) {
        String str = getAsString(id);
        TIME_MAP.remove(id);
        return str;
    }

    /**
     * Get the time since start() was last called
     * @param id an identifier object
     * @return the time in milliseconds
     */
    public static double getAndReset(Object id) {
        double val = get(id);
        TIME_MAP.put(id, measure());
        return val;
    }

    /**
     * Get the time since start() was last called as formatted string (e.g. 334.22ms) and reset the timer
     * @param id an identifier object
     * @return the time in milliseconds as formatted string
     */
    public static String getAsStrindAndReset(Object id) {
        String str = getAsString(id);
        TIME_MAP.put(id, measure());
        return str;
    }
    
    private static long measure() {
        return System.nanoTime();
    }

    /**
     * Removes the id, if it exists
     * @param id an identifier object
     */
    public static void stop(Object id) {
        TIME_MAP.remove(id);
    }
}
