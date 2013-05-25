package org.terasology.benchmark.chunks.arrays;

import java.nio.ByteBuffer;

import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler;

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
    public void prerun() {}

    @Override
    public void run() {
        handler.deserialize(buffer);
        buffer.rewind();
    }

    @Override
    public void postrun() {}

    @Override
    public void finish(boolean aborted) {}
}
