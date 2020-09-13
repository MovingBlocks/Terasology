// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;

import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

/**
 * Chunk task which can be used as entry point in {@link ChunkProcessingPipeline}
 * <p>
 * Useful for generation chunks to pipeline or recieve chunks from any other source.
 */
public class SupplierChunkTask extends ForkJoinTask<Chunk> implements ChunkTask {

    private final String taskName;
    private final Supplier<Chunk> supplier;
    private Chunk resultChunk;
    private final Vector3i position;

    public SupplierChunkTask(String taskName, Vector3i position, Supplier<Chunk> supplier) {
        this.taskName = taskName;
        this.position = position;
        this.supplier = supplier;
    }

    @Override
    public Chunk getChunk() {
        return resultChunk;
    }

    @Override
    public String getName() {
        return taskName;
    }

    @Override
    public void run() {
        resultChunk = supplier.get();
    }

    @Override
    public boolean isTerminateSignal() {
        return false;
    }

    @Override
    public Vector3i getPosition() {
        return position;
    }

    @Override
    public Chunk getRawResult() {
        return resultChunk;
    }

    @Override
    protected void setRawResult(Chunk value) {
        resultChunk = value;
    }

    @Override
    protected boolean exec() {
        setRawResult(supplier.get());
        return true;
    }
}
