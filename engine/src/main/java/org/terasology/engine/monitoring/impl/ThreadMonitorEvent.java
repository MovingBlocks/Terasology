// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;


import com.google.common.base.Preconditions;

public class ThreadMonitorEvent {

    public enum Type {
        MonitorAdded
    }

    public final SingleThreadMonitor monitor;
    public final Type type;

    public ThreadMonitorEvent(SingleThreadMonitor monitor, Type type) {
        Preconditions.checkNotNull(monitor, "The parameter 'monitor' must not be null");
        Preconditions.checkNotNull(type, "The parameter 'type' must not be null");
        this.monitor = monitor;
        this.type = type;
    }
}
