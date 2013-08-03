/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.monitoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.terasology.monitoring.impl.SingleThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitorImpl;
import org.terasology.monitoring.impl.ThreadActivityInternal;
import org.terasology.monitoring.impl.ThreadMonitorEvent;

import java.util.List;
import java.util.Map;

public class ThreadMonitor {

    private static final EventBus eventbus = new EventBus("ThreadMonitor");
    private static final Map<Thread, SingleThreadMonitor> threadInfoById = Maps.newConcurrentMap();

    private ThreadMonitor() {
    }

    public static ThreadActivity startThreadActivity(String activityName) {
        SingleThreadMonitor monitor = getMonitor();
        monitor.beginTask(activityName);
        return new ThreadActivityInternal(monitor);

    }

    public static synchronized List<SingleThreadMonitor> getThreadMonitors(List<SingleThreadMonitor> output, boolean aliveThreadsOnly) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output.clear();
        for (SingleThreadMonitor entry : threadInfoById.values()) {
            if (!aliveThreadsOnly || entry.isAlive()) {
                output.add(entry);
            }
        }
        return output;
    }

    public static synchronized List<SingleThreadMonitor> getThreadMonitors(boolean aliveThreadsOnly) {
        return getThreadMonitors(Lists.<SingleThreadMonitor>newArrayList(), aliveThreadsOnly);
    }

    public static void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        eventbus.register(object);
    }

    public static void addError(Throwable e) {
        SingleThreadMonitor monitor = getMonitor();
        monitor.addError(e);
    }

    private static SingleThreadMonitor getMonitor() {
        SingleThreadMonitor monitor = threadInfoById.get(Thread.currentThread());
        if (monitor == null) {
            monitor = new SingleThreadMonitorImpl(Thread.currentThread());
            threadInfoById.put(Thread.currentThread(), monitor);
            eventbus.post(new ThreadMonitorEvent(monitor, ThreadMonitorEvent.Type.MonitorAdded));
        }
        return monitor;
    }
}
