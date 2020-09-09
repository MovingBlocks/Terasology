// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.chunks.arrays;

import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraArray.SerializationHandler;

import java.nio.ByteBuffer;

public class BenchmarkTeraArrayDeserializeFromBuffer extends BenchmarkTeraArraySerialization {

    protected ByteBuffer buffer;

    @SuppressWarnings("rawtypes")
    public BenchmarkTeraArrayDeserializeFromBuffer(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " deserialization from buffer";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setup() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        handler.serialize(array, buffer);
        buffer.rewind();
    }

    @Override
    public void prerun() {
    }

    @Override
    public void run() {
        handler.deserialize(buffer);
        buffer.rewind();
    }

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }
}
