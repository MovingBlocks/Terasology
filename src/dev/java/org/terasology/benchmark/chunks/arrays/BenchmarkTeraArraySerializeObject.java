package org.terasology.benchmark.chunks.arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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
    public void prerun() {}

    @Override
    public void run() {
        try {
            (new ObjectOutputStream(out)).writeObject(array);
            out.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postrun() {}

    @Override
    public void finish(boolean aborted) {}
}
