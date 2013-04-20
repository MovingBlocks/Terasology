package org.terasology.monitoring.impl;


import com.google.common.base.Preconditions;

public class ThreadMonitorEvent {
    
    public enum Type {
        MonitorAdded
    }
    
    public final SingleThreadMonitor monitor;
    public final ThreadMonitorEvent.Type type;
    
    public ThreadMonitorEvent(SingleThreadMonitor monitor, ThreadMonitorEvent.Type type) {
        Preconditions.checkNotNull(monitor, "The parameter 'monitor' must not be null");
        Preconditions.checkNotNull(type, "The parameter 'type' must not be null");
        this.monitor = monitor;
        this.type = type;
    }
}