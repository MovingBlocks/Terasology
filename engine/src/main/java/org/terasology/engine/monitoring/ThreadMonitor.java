// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import org.terasology.engine.monitoring.impl.SingleThreadMonitor;
import org.terasology.engine.monitoring.impl.SingleThreadMonitorImpl;
import org.terasology.engine.monitoring.impl.ThreadActivityInternal;
import org.terasology.engine.monitoring.impl.ThreadMonitorEvent;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class ThreadMonitor {

    private static final EventBus EVENT_BUS = new EventBus("ThreadMonitor");
    private static final Map<Thread, SingleThreadMonitor> THREAD_INFO_BY_ID = Maps.newConcurrentMap();



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
        for (SingleThreadMonitor entry : THREAD_INFO_BY_ID.values()) {
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
        EVENT_BUS.register(object);
    }

    public static void addError(Throwable e) {
        SingleThreadMonitor monitor = getMonitor();
        monitor.addError(e);
    }

    private static SingleThreadMonitor getMonitor() {
        SingleThreadMonitor monitor = THREAD_INFO_BY_ID.get(Thread.currentThread());
        if (monitor == null) {
            monitor = new SingleThreadMonitorImpl(Thread.currentThread());
            THREAD_INFO_BY_ID.put(Thread.currentThread(), monitor);
            EVENT_BUS.post(new ThreadMonitorEvent(monitor, ThreadMonitorEvent.Type.MonitorAdded));
        }
        return monitor;
    }
}
