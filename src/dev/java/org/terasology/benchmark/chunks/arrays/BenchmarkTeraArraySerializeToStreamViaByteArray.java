package org.terasology.benchmark.chunks.arrays;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler;

public class BenchmarkTeraArraySerializeToStreamViaByteArray extends BenchmarkTeraArraySerialization {

    protected ByteBuffer buffer;
    protected ByteArrayOutputStream out;
    protected byte[] via;

    @SuppressWarnings("rawtypes")
    public BenchmarkTeraArraySerializeToStreamViaByteArray(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " serialization into ByteBuffer and transfer via byte[] array into ByteArrayOutputStream";
    }

    @Override
    public void setup() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        out = new ByteArrayOutputStream(BUFFER_SIZE);
        via = new byte[BUFFER_SIZE];
    }

    @Override
    public void prerun(int index) {}

    @SuppressWarnings("unchecked")
    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        int bogus = 0;
        for (int i = 0; i < repetitions; i++) {
            handler.serialize(array, buffer);
            final int length = buffer.position();
            buffer.rewind();
            buffer.get(via, 0, length);
            buffer.rewind();
            out.write(via, 0, length);
            out.reset();
            bogus += length;
        }
        return bogus;
    }

    @Override
    public void postrun(int index, BenchmarkResult result) {}

    @Override
    public void finish(boolean aborted) {}
}
