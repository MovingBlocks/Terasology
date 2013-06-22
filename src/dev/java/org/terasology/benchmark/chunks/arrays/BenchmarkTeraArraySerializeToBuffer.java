package org.terasology.benchmark.chunks.arrays;

import java.nio.ByteBuffer;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.perBlockStorage.TeraArray;
import org.terasology.world.chunks.perBlockStorage.TeraArray.SerializationHandler;

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
    public void prerun(int index) {}

    @SuppressWarnings("unchecked")
    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        int bogus = 0;
        for (int i = 0; i < repetitions; i++) {
            handler.serialize(array, buffer);
            bogus += buffer.position();
            buffer.rewind();
        }
        return bogus;
    }

    @Override
    public void postrun(int index, BenchmarkResult result) {}

    @Override
    public void finish(boolean aborted) {}
}
