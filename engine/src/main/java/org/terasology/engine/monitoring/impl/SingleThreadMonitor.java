// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import java.util.List;

public interface SingleThreadMonitor extends Comparable<SingleThreadMonitor> {

    boolean isAlive();

    boolean isActive();

    String getName();

    long getThreadId();

    boolean hasErrors();

    int getNumErrors();

    Throwable getLastError();

    List<Throwable> getErrors();

    void addError(Throwable error);

    Iterable<String> getTasks();

    long getCounter(String task);

    void beginTask(String task);

    void endTask();

    String getLastTask();
}
