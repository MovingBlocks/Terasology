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

import gnu.trove.TCollections;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectLongProcedure;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.lwjgl.Sys;

/**
 * Active implementation of Performance Monitor
 *
 * @author Immortius <immortius@gmail.com>
 *         TODO: Check to ensure activities are being started and stopped correctly
 *         TODO: Remove activities with 0 time
 */
public class PerformanceMonitorImpl implements IPerformanceMonitor {
    private static final int RETAINED_CYCLES = 60;
    private static final double DECAY_RATE = 0.98;

    private Stack<Activity> _activityStack;
    private List<TObjectLongMap<String>> _metricData;
    private TObjectLongMap<String> _currentData;
    private TObjectLongMap<String> _runningTotals;
    private TObjectIntMap<String> _runningThreads;
    private TObjectIntMap<String> _stoppedThreads;
    private long _timerTicksPerSecond;
    private TObjectDoubleMap<String> _spikeData;
    private double _timeFactor;
    private TObjectIntMap<String> _lastRunningThreads;

    private Thread _mainThread;

    public PerformanceMonitorImpl() {
        _activityStack = new Stack<Activity>();
        _metricData = new LinkedList<TObjectLongMap<String>>();
        _runningTotals = new TObjectLongHashMap<String>();
        _timerTicksPerSecond = Sys.getTimerResolution();
        _currentData = new TObjectLongHashMap<String>();
        _spikeData = new TObjectDoubleHashMap<String>();
        _runningThreads = TCollections.synchronizedMap(new TObjectIntHashMap<String>());
        _stoppedThreads = TCollections.synchronizedMap(new TObjectIntHashMap<String>());
        _lastRunningThreads = new TObjectIntHashMap<String>();
        _timeFactor = 1000.0 / _timerTicksPerSecond;
        _mainThread = Thread.currentThread();

    }

    public void rollCycle() {
        _metricData.add(_currentData);
        _spikeData.forEachEntry(new TObjectDoubleProcedure<String>() {
            public boolean execute(String s, double v) {
                _spikeData.put(s, v * DECAY_RATE);
                return true;
            }
        });

        _currentData.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String s, long v) {
                _runningTotals.adjustOrPutValue(s, v, v);
                double time = v * _timeFactor;
                double prev = _spikeData.get(s);
                if (time > prev) {
                    _spikeData.put(s, time);
                }
                return true;
            }
        });

        while (_metricData.size() > RETAINED_CYCLES) {
            _metricData.get(0).forEachEntry(new TObjectLongProcedure<String>() {
                public boolean execute(String s, long v) {
                    _runningTotals.adjustValue(s, -v);
                    return true;
                }
            });
            _metricData.remove(0);
        }
        _currentData = new TObjectLongHashMap<String>();

        _runningThreads.forEachEntry(new TObjectIntProcedure<String>() {
            public boolean execute(String s, int i) {
                _lastRunningThreads.adjustOrPutValue(s, i, i);
                return true;
            }
        });
        TObjectIntMap<String> temp = _runningThreads;
        temp.clear();
        _runningThreads = _stoppedThreads;
        _stoppedThreads = temp;
        _lastRunningThreads.retainEntries(new TObjectIntProcedure<String>() {
            public boolean execute(String s, int i) {
                return i > 0;
            }
        });

    }

    public void startActivity(String activity) {
        if (Thread.currentThread() != _mainThread)
            return;
        Activity newActivity = new Activity();
        newActivity.name = activity;
        newActivity.startTime = Sys.getTime();
        if (!_activityStack.isEmpty()) {
            Activity currentActivity = _activityStack.peek();
            currentActivity.ownTime += newActivity.startTime - ((currentActivity.resumeTime > 0) ? currentActivity.resumeTime : currentActivity.startTime);
        }

        _activityStack.push(newActivity);
    }

    public void endActivity() {
        if (Thread.currentThread() != _mainThread || _activityStack.empty())
            return;

        Activity oldActivity = _activityStack.pop();
        long time = Sys.getTime();
        long total = (oldActivity.resumeTime > 0) ? oldActivity.ownTime + time - oldActivity.resumeTime : time - oldActivity.startTime;
        _currentData.adjustOrPutValue(oldActivity.name, total, total);

        if (!_activityStack.isEmpty()) {
            Activity currentActivity = _activityStack.peek();
            currentActivity.resumeTime = time;
        }
    }

    public TObjectDoubleMap<String> getRunningMean() {
        final TObjectDoubleMap<String> result = new TObjectDoubleHashMap<String>();
        final double factor = _timeFactor / _metricData.size();
        _runningTotals.forEachEntry(new TObjectLongProcedure<String>() {
            public boolean execute(String s, long l) {
                if (l > 0) {
                    result.put(s, l * factor);
                }
                return true;
            }
        });
        return result;
    }

    public TObjectDoubleMap<String> getDecayingSpikes() {
        return _spikeData;
    }

    public void startThread(String name) {
        _runningThreads.adjustOrPutValue(name, 1, 1);
    }

    public void endThread(String name) {
        _stoppedThreads.adjustOrPutValue(name, -1, -1);
    }

    public TObjectIntMap<String> getRunningThreads() {
        return _lastRunningThreads;
    }

    private static class Activity {
        public String name;
        public long startTime;
        public long resumeTime;
        public long ownTime;
    }
}
