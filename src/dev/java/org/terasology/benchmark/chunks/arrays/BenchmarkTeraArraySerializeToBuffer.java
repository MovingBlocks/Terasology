package org.terasology.benchmark.chunks.arrays;

import java.nio.ByteBuffer;

import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler;

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
    public void prerun() {}

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        int bogus = 0;
        handler.serialize(array, buffer);
        bogus += buffer.position();
        buffer.rewind();
    }

    @Override
    public void postrun() {}

    @Override
    public void finish(boolean aborted) {}
}
