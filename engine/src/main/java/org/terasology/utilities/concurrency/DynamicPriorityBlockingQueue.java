/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.utilities.concurrency;

import com.google.common.collect.Lists;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicPriorityBlockingQueue<T> extends AbstractQueue<T> implements BlockingQueue<T> {
    private Comparator<T> comparator;
    private List<T> elements = Lists.newLinkedList();

    /**
     * Lock used for all public operations
     */
    private final ReentrantLock lock;

    /**
     * Condition for blocking when empty
     */
    private final Condition notEmpty;

    public DynamicPriorityBlockingQueue(Comparator<T> comparator) {
        this.comparator = comparator;

        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
    }

    @Override
    public boolean add(T t) {
        return offer(t);
    }

    @Override
    public void put(T t) throws InterruptedException {
        offer(t);
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        return offer(t);
    }

    @Override
    public boolean offer(T t) {
        lock.lock();
        try {
            elements.add(t);
            notEmpty.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T poll() {
        lock.lock();
        try {
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            T result = dequeue();
            while (result == null) {
                notEmpty.await();
                result = dequeue();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            T result = dequeue();
            while (result == null && nanos > 0) {
                nanos = notEmpty.awaitNanos(nanos);
                result = dequeue();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T peek() {
        lock.lock();
        try {
            return elements.size() == 0 ? null : elements.get(0);
        } finally {
            lock.unlock();
        }
    }

    private T dequeue() {
        if (elements.size() == 0) {
            return null;
        }
        T smallest = elements.remove(0);
        ListIterator<T> iterator = elements.listIterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (comparator.compare(smallest, next) > 0) {
                iterator.set(smallest);
                smallest = next;
            }
        }
        return smallest;
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        lock.lock();
        try {
            int count = 0;
            while (!elements.isEmpty()) {
                c.add(elements.remove(0));
                count++;
            }
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        lock.lock();
        try {
            int count = 0;
            while (!elements.isEmpty() && count < maxElements) {
                c.add(elements.remove(0));
                count++;
            }
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return elements.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }
}
