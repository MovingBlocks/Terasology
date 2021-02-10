/*
 * Copyright 2013 MovingBlocks
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
