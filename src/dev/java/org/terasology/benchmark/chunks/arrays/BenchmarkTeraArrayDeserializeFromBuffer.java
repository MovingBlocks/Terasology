package org.terasology.benchmark.chunks.arrays;

import java.nio.ByteBuffer;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.perBlockStorage.TeraArray;
import org.terasology.world.chunks.perBlockStorage.TeraArray.SerializationHandler;

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
    public void prerun(int index) {}

    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        for (int i = 0; i < repetitions; i++) {
            handler.deserialize(buffer);
            buffer.rewind();
        }
        return 0;
    }

    @Override
    public void postrun(int index, BenchmarkResult result) {}

    @Override
    public void finish(boolean aborted) {}
}
