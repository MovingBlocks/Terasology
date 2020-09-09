// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.chunks.arrays;

import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraArray.SerializationHandler;

import java.nio.ByteBuffer;

@SuppressWarnings("rawtypes")
public class BenchmarkTeraArraySerializeToBuffer extends BenchmarkTeraArraySerialization {

    protected ByteBuffer buffer;

    public BenchmarkTeraArraySerializeToBuffer(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " serialization directly into ByteBuffer";
    }

    @Override
    public void setup() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    @Override
    public void prerun() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        int bogus = 0;
        handler.serialize(array, buffer);
        bogus += buffer.position();
        buffer.rewind();
    }

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }
}
