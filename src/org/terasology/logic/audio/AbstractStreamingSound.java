package org.terasology.logic.audio;

import org.lwjgl.openal.AL10;

import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class AbstractStreamingSound extends AbstractSound {
    private final static int BUFFER_POOL_SIZE = 3;

    protected InputStream audioStream = null;

    protected int[] buffers;

    protected int lastUpdatedBuffer;

    public AbstractStreamingSound(String name, InputStream source) {
        super(name, source);

        this.audioStream = source;

        this.initializeBuffers();
    }

    protected abstract ByteBuffer fetchData();

    public InputStream getAudioStream() {
        return audioStream;
    }

    public void load(InputStream stream) {
        this.audioStream = stream;
    }

    public int[] getBuffers() {
        return this.buffers;
    }


    public boolean updateBuffer(int buffer) {
        ByteBuffer bufferData = this.fetchData();

        if (bufferData == null || bufferData.limit() == 0) {
            return false; // no more data available
        }

        AL10.alBufferData(buffer, this.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, bufferData, this.getSamplingRate());

        OpenALException.checkState("Uploading buffer data");

        this.lastUpdatedBuffer = buffer;

        return true;
    }

    private void initializeBuffers() {
        buffers = new int[BUFFER_POOL_SIZE];

        for(int i = 0 ; i < buffers.length ; i ++) {
            buffers[i] = AL10.alGenBuffers();
            OpenALException.checkState("Creating buffer");
        }

        this.lastUpdatedBuffer = buffers[0];
    }

    @Override
    public int getBufferId() {
        return lastUpdatedBuffer;
    }

    @Override
    protected void finalize() throws Throwable {
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] != 0) {
                AL10.alDeleteBuffers(buffers[i]);
            }
        }
    }
}
