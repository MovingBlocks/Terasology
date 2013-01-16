package org.terasology.benchmark.chunks.arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.blockdata.TeraArray;

public class BenchmarkTeraArraySerializeToStreamViaChannel extends BenchmarkTeraArraySerialization {

    protected ByteBuffer buffer;
    protected ByteArrayOutputStream out;
    protected WritableByteChannel channel;
    
    @SuppressWarnings("rawtypes")
    public BenchmarkTeraArraySerializeToStreamViaChannel(TeraArray.SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " serialization into ByteBuffer and transfer via Channel into ByteArrayOutputStream";
    }

    @Override
    public void setup() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        out = new ByteArrayOutputStream(BUFFER_SIZE);
        channel = Channels.newChannel(out);
    }

    @Override
    public void prerun(int index) {}

    @SuppressWarnings("unchecked")
    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        try {
            int bogus = 0;
            for (int i = 0; i < repetitions; i++) {
                handler.serialize(array, buffer);
                bogus += buffer.position();
                buffer.rewind();
                channel.write(buffer);
                buffer.rewind();
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
