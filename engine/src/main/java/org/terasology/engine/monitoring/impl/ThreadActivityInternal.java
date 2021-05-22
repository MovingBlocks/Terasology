// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import org.terasology.engine.monitoring.ThreadActivity;

/**
 */
public class ThreadActivityInternal implements ThreadActivity {

    private SingleThreadMonitor monitor;

    public ThreadActivityInternal(SingleThreadMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void close() {
        monitor.endTask();
    }
}
