package org.terasology.logic.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.openal.AL10.*;

public abstract class AbstractSound implements Sound {

    private static int bufferAmount = 0;

    private String name = null;

    private int bufferId = 0;

    protected int length = 0;

    public AbstractSound(String name, InputStream source) {
        this(name);
        this.load(source);
    }

    public AbstractSound(String name, String resource) {
        this(name);
        this.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));
    }

    public AbstractSound(String name) {
        this.name = name;

        bufferId = alGenBuffers();
        OpenALException.checkState("Allocating sound buffer");

        bufferAmount++;
    }

    public void load(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File " + file.getName() + " not exists");
        }

        try {
            this.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load sound: " + e.getMessage(), e);
        }
    }

    public int getLength() {
        if (length == 0 && bufferId != 0) { // only if buffer is already initialized
            int bits = getBufferBits();
            int size = getBufferSize();
            int channels = getChannels();
            int frequency = getSamplingRate();

            length = (size / channels / (bits / 8)) / frequency;
        }

        return length;
    }

    public String getName() {
        return name;
    }

    public int getChannels() {
        return alGetBufferi(bufferId, AL_CHANNELS);
    }

    public int getSamplingRate() {
        return alGetBufferi(bufferId, AL_FREQUENCY);
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getBufferBits() {
        return alGetBufferi(bufferId, AL_BITS);
    }

    public int getBufferSize() {
        return alGetBufferi(bufferId, AL_SIZE);
    }

    @Override
    protected void finalize() throws Throwable {
        if (bufferId != 0) {
            alDeleteBuffers(bufferId);
        }
    }
}
