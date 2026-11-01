// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.joml.Vector3ic;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class PositionFuture<T> extends FutureTask<T> {
    private final Vector3ic position;

    public PositionFuture(Callable callable, Vector3ic position) {
        super(callable);
        this.position = position;
    }

    public PositionFuture(Runnable runnable, T result, Vector3ic position) {
        super(runnable, result);
        this.position = position;
    }

    public Vector3ic getPosition() {
        return position;
    }
}
