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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public class SingleThreadMonitorImpl implements SingleThreadMonitor {

    private final String name;
    private final WeakReference<Thread> ref;
    private final TObjectIntMap<String> taskCounters = new TObjectIntHashMap<>();
    private final Set<String> tasks = Sets.newLinkedHashSet();

    private final long id;

    private Deque<Throwable> errors = Queues.newArrayDeque();

    private boolean active;
    private String lastTask = "";

    public SingleThreadMonitorImpl(Thread thread) {
        Preconditions.checkNotNull(thread, "The parameter 'thread' must not be null");
        this.name = thread.getName();
        this.ref = new WeakReference<>(thread);
        this.id = thread.getId();
    }

    @Override
    public final boolean isAlive() {
        return ref.get() != null;
    }

    @Override
    public final boolean isActive() {
        return active;
    }

    @Override
    public final String getLastTask() {
        return lastTask;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final long getThreadId() {
        return id;
    }

    @Override
    public final synchronized boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public final synchronized int getNumErrors() {
        return errors.size();
    }

    @Override
    public final synchronized Throwable getLastError() {
        return errors.peekLast();
    }

    @Override
    public final synchronized List<Throwable> getErrors() {
        return ImmutableList.copyOf(errors);
    }

    @Override
    public final synchronized void addError(Throwable error) {
        errors.add(error);
    }

    @Override
    public final synchronized Iterable<String> getTasks() {
        return ImmutableSet.copyOf(tasks);
    }

    @Override
    public final synchronized long getCounter(String task) {
        return taskCounters.get(task);
    }

    @Override
    public final synchronized void beginTask(String task) {
        if (taskCounters.adjustOrPutValue(task, 1, 1) == 1) {
            tasks.add(task);
        }
        active = true;
        lastTask = task;
    }

    @Override
    public final synchronized void endTask() {
        active = false;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder(100);
        b.append(name).append(isAlive() ? " [ALIVE]" : " [DEAD]").append(" Id = ").append(id);
        for (String task : tasks) {
            b.append(", ").append(task).append(" = ").append(taskCounters.get(task));
        }
        if (hasErrors()) {
            b.append(" [Errors = ").append(getNumErrors()).append(", ").append(getLastError().getClass().getSimpleName()).append("]");
        }
        return b.toString();
    }

    @Override
    public int compareTo(SingleThreadMonitor other) {
        if (other == null) {
            return -1;
        }
        final boolean alive1 = this.isAlive();
        final boolean alive2 = other.isAlive();
        final int relAlive = alive1 ? (alive2 ? 0 : -1) : (alive2 ? 1 : 0);
        if (relAlive == 0) {
            final boolean active1 = this.isActive();
            final boolean active2 = other.isActive();
            final int relActive = active1 ? (active2 ? 0 : -1) : (active2 ? 1 : 0);
            if (relActive == 0) {
                final String name1 = this.getName();
                final String name2 = other.getName();
                final int relName = name1.compareTo(name2);
                if (relName == 0) {
                    final long id1 = this.getThreadId();
                    final long id2 = other.getThreadId();
                    return (int) (id1 - id2);
                }
                return relName;
            }
            return relActive;
        }
        return relAlive;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SingleThreadMonitor) {
            SingleThreadMonitor other = (SingleThreadMonitor) obj;
            return Objects.equal(isAlive(), other.isActive()) && Objects.equal(isActive(), other.isActive())
                    && Objects.equal(name, other.getName()) && Objects.equal(getThreadId(), other.getThreadId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isActive(), isActive(), name, getThreadId());
    }
}
