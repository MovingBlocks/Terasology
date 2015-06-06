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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.StreamingSoundData;
import org.terasology.audio.openAL.OpenALException;
import org.terasology.audio.openAL.OpenALManager;
import org.terasology.engine.GameThread;

import java.nio.ByteBuffer;
import java.util.Optional;

import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alGetBufferi;

public final class OpenALStreamingSound extends StreamingSound {
    private static final int BUFFER_POOL_SIZE = 8;
    private static final int BUFFER_SIZE = 4096 * 8;

    private static final Logger logger = LoggerFactory.getLogger(OpenALStreamingSound.class);

    protected int[] buffers = new int[0];
    protected int lastUpdatedBuffer;

    private final OpenALManager audioManager;
    private StreamingSoundData stream;
    private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public OpenALStreamingSound(ResourceUrn urn, AssetType<?, StreamingSoundData> assetType, StreamingSoundData data, OpenALManager audioManager) {
        super(urn, assetType);
        this.audioManager = audioManager;
        reload(data);
    }

    public int[] getBuffers() {
        return this.buffers;
    }

    public boolean updateBuffer(int buffer) {
        stream.readNextInto(dataBuffer);

        if (dataBuffer.limit() == 0) {
            // rewind to ensure that limit is reset to capacity
            dataBuffer.clear();
            return false;
        }

        int format = stream.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        AL10.alBufferData(buffer, format, dataBuffer, stream.getSamplingRate());
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
    public void reset() {
        stream.reset();
    }

    @Override
    protected void doReload(StreamingSoundData data) {
        stream = data;
        try {
            GameThread.synch(this::initializeBuffers);
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    @Override
    protected Optional<? extends Asset<StreamingSoundData>> doCreateCopy(ResourceUrn copyUrn, AssetType<?, StreamingSoundData> parentAssetType) {
        return Optional.of(new OpenALStreamingSound(copyUrn, parentAssetType, stream, audioManager));
    }

    @Override
    protected void doDispose() {
        try {
            GameThread.synch(() -> {
                audioManager.purgeSound(this);

                // TODO: Fix this - probably failing if sound is playing
                for (int buffer : buffers) {
                    if (buffer != 0) {
                        AL10.alDeleteBuffers(buffer);
                    }
                }
                OpenALException.checkState("Deleting buffer data");
                buffers = new int[0];
            });
        } catch (InterruptedException e) {
            logger.error("Failed to dispose {}", getUrn(), e);
        }
    }
}
