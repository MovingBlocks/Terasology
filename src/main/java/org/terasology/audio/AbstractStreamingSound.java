package org.terasology.audio;

import org.lwjgl.openal.AL10;

import java.net.URL;
import java.nio.ByteBuffer;

public abstract class AbstractStreamingSound extends AbstractSound {
    private final static int BUFFER_POOL_SIZE = 3;

    protected URL audioSource = null;

    protected int[] buffers;

    protected int lastUpdatedBuffer;

    public AbstractStreamingSound(String name, URL source) {
        super(name);

        this.audioSource = source;

        this.initializeBuffers();

        this.reset();
    }

    protected abstract ByteBuffer fetchData();

    public URL getAudioSource() {
        return audioSource;
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

        for (int i = 0; i < buffers.length; i++) {
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
    public Sound reset() {
        this.load(this.audioSource);
        return super.reset();
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
