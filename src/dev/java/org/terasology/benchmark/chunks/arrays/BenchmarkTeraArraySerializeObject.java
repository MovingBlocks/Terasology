package org.terasology.benchmark.chunks.arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler;

public class BenchmarkTeraArraySerializeObject extends BenchmarkTeraArraySerialization {

    protected ByteArrayOutputStream out;

    @SuppressWarnings("rawtypes")
    public BenchmarkTeraArraySerializeObject(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " builtin object serialization directly into ByteArrayOutputStream";
    }

    @Override
    public void setup() {
        out = new ByteArrayOutputStream(BUFFER_SIZE);
    }

    @Override
    public void prerun(int index) {}

    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        try {
            int bogus = 0;
            for (int i = 0; i < repetitions; i++) {
                (new ObjectOutputStream(out)).writeObject(array);
                bogus += out.size();
                out.reset();
            }
            return bogus;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postrun(int index, BenchmarkResult result) {}

    @Override
    public void finish(boolean aborted) {}
}
