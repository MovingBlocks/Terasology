package org.terasology.benchmark.chunks.arrays;

import java.nio.ByteBuffer;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.perBlockStorage.TeraArray;
import org.terasology.world.chunks.perBlockStorage.TeraArray.SerializationHandler;

import com.google.protobuf.ByteString;

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
    public void prerun(int index) {}

    @SuppressWarnings("unchecked")
    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        int bogus = 0;
        for (int i = 0; i < repetitions; i++) {
            handler.serialize(array, buffer);
            int length = buffer.position();
            buffer.rewind();
            final ByteString b = ByteString.copyFrom(buffer, length);
            buffer.rewind();
            bogus += b.size();
        }
        return bogus;
    }

    @Override
    public void postrun(int index, BenchmarkResult result) {}

    @Override
    public void finish(boolean aborted) {}
}
