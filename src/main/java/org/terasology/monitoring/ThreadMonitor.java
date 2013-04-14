package org.terasology.monitoring;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.LinkedList;
import java.util.List;

import org.terasology.monitoring.impl.NullThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitorImpl;
import org.terasology.monitoring.impl.ThreadMonitorEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;

public class ThreadMonitor {

    private static final EventBus eventbus = new EventBus("ThreadMonitor");
    private static final TLongObjectMap<SingleThreadMonitor> threadsById = new TLongObjectHashMap<SingleThreadMonitor>();
    private static final Multimap<String, SingleThreadMonitor> threadsByName = HashMultimap.create();
    private static final LinkedList<SingleThreadMonitor> threads = new LinkedList<SingleThreadMonitor>();
    
    private ThreadMonitor() {}
    
    public static boolean isThreadMonitored(long id) {
        return getThreadMonitor(id) != null;
    }
    
    public static synchronized SingleThreadMonitor getThreadMonitor(long id) {
        return threadsById.get(id);
    }

    public static synchronized int getThreadMonitors(List<SingleThreadMonitor> output, boolean aliveThreadsOnly) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output.clear();
        for (SingleThreadMonitor entry : threads) {
            if (!aliveThreadsOnly || entry.isAlive())
                output.add(entry);
        }
        return output.size();
    }
    
    public static SingleThreadMonitor create(String name, Thread thread, String... counters) {
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return NullThreadMonitor.getInstance();
        return new SingleThreadMonitorImpl(name, thread, counters);
    }
    
    public static SingleThreadMonitor create(String name, String... counters) {
        if (!Monitoring.isAdvancedMonitoringEnabled())
            return NullThreadMonitor.getInstance();
        return new SingleThreadMonitorImpl(name, Thread.currentThread(), counters);
    }
    
    public static synchronized void register(SingleThreadMonitor monitor) {
        Preconditions.checkNotNull(monitor, "The parameter 'monitor' must not be null");
        threads.add(monitor);
        threadsById.put(monitor.getThreadId(), monitor);
        threadsByName.put(monitor.getName(), monitor);
        eventbus.post(new ThreadMonitorEvent(monitor, ThreadMonitorEvent.Type.MonitorAdded));
    }

    public static void registerForEvents(Object object) {
        Preconditions.checkNotNull(object, "The parameter 'object' must not be null");
        eventbus.register(object);
    }
}