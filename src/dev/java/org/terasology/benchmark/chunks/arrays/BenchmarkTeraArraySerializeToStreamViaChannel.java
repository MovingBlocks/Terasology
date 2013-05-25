package org.terasology.benchmark.chunks.arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

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
    public void prerun() {}

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            handler.serialize(array, buffer);
            buffer.rewind();
            channel.write(buffer);
            buffer.rewind();
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
