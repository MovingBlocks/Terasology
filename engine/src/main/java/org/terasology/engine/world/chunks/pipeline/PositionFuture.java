// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3ic;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PositionFuture<T> implements RunnableFuture<T> {

    private final RunnableFuture<T> delegate;
    private final Vector3ic position;

    public PositionFuture(RunnableFuture<T> delegate, Vector3ic position) {
        this.delegate = delegate;
        this.position = position;
    }

    public Vector3ic getPosition() {
        return position;
    }

    @Override
    public void run() {
        delegate.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        return delegate.get(timeout, unit);
    }
}
