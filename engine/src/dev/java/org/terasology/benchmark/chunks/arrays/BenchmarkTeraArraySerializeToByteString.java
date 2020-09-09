// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.chunks.arrays;

import com.google.protobuf.ByteString;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraArray.SerializationHandler;

import java.nio.ByteBuffer;

public class BenchmarkTeraArraySerializeToByteString extends BenchmarkTeraArraySerialization {

    protected ByteBuffer buffer;

    @SuppressWarnings("rawtypes")
    public BenchmarkTeraArraySerializeToByteString(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " serialization into ByteString";
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
        handler.serialize(array, buffer);
        int length = buffer.position();
        buffer.rewind();
        final ByteString b = ByteString.copyFrom(buffer, length);
        buffer.rewind();
    }

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }
}
