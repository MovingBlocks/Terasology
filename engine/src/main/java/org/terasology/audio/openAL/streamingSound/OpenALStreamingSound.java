/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.audio.openAL.streamingSound;

import org.lwjgl.openal.AL10;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.StreamingSoundData;
import org.terasology.audio.openAL.OpenALException;

import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alGetBufferi;

public final class OpenALStreamingSound extends AbstractAsset<StreamingSoundData> implements StreamingSound {
    private static final int BUFFER_POOL_SIZE = 8;
    private static final int BUFFER_SIZE = 4096 * 8;

    protected int[] buffers = new int[0];
    protected int lastUpdatedBuffer;

    private AudioManager audioManager;
    private StreamingSoundData stream;
    private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public OpenALStreamingSound(AssetUri uri, StreamingSoundData data, AudioManager audioManager) {
        super(uri);
        this.audioManager = audioManager;
        reload(data);
    }

    public int[] getBuffers() {
        return this.buffers;
    }

    public boolean updateBuffer(int buffer) {
        stream.readNextInto(dataBuffer);

        if (dataBuffer.limit() == 0) {
            return false;
        }

        AL10.alBufferData(buffer, stream.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16,
                dataBuffer, stream.getSamplingRate());
        OpenALException.checkState("Uploading buffer data");

        this.lastUpdatedBuffer = buffer;

        return true;
    }

    private void initializeBuffers() {
        if (buffers.length == 0) {
            buffers = new int[BUFFER_POOL_SIZE];

            for (int i = 0; i < buffers.length; i++) {
                buffers[i] = AL10.alGenBuffers();
                OpenALException.checkState("Creating buffer");
            }

            this.lastUpdatedBuffer = buffers[0];
        }
    }

    @Override
    public int getChannels() {
        return stream.getChannels();
    }

    @Override
    public int getSamplingRate() {
        return stream.getSamplingRate();
    }

    @Override
    public int getBufferSize() {
        return alGetBufferi(lastUpdatedBuffer, AL_SIZE);
    }

    @Override
    public void play() {
        audioManager.playMusic(this);
    }

    @Override
    public void play(float volume) {
        audioManager.playMusic(this, volume);
    }

    public int getBufferId() {
        return lastUpdatedBuffer;
    }

    public int getBufferBits() {
        return stream.getBufferBits();
    }

    @Override
    public void dispose() {
        // TODO: Fix this - probably failing if sound is playing
        for (int buffer : buffers) {
            if (buffer != 0) {
                AL10.alDeleteBuffers(buffer);
            }
        }
        OpenALException.checkState("Deleting buffer data");
        buffers = new int[0];
    }

    @Override
    public void reload(StreamingSoundData data) {
        stream = data;
        this.initializeBuffers();
    }

    @Override
    public boolean isDisposed() {
        return buffers.length == 0;
    }

    @Override
    public void reset() {
        stream.reset();
    }
}
