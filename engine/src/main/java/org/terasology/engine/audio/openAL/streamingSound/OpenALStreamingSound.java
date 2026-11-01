// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL.streamingSound;

import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.StreamingSoundData;
import org.terasology.engine.audio.openAL.OpenALException;
import org.terasology.engine.audio.openAL.OpenALManager;
import org.terasology.engine.core.GameThread;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Optional;

import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alGetBufferi;

public final class OpenALStreamingSound extends StreamingSound {
    private static final int BUFFER_POOL_SIZE = 8;
    private static final int BUFFER_SIZE = 4096 * 8;

    private static final Logger logger = LoggerFactory.getLogger(OpenALStreamingSound.class);

    private final OpenALManager audioManager;
    private StreamingSoundData stream;
    private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private OpenALStreamingSound.DisposalAction internalResources;
    private int lastUpdatedBuffer;

    public OpenALStreamingSound(ResourceUrn urn, AssetType<?, StreamingSoundData> assetType, StreamingSoundData data,
                                OpenALManager audioManager, OpenALStreamingSound.DisposalAction disposableAction) {
        super(urn, assetType, disposableAction);
        this.internalResources = disposableAction;
        this.internalResources.setAsset(this);
        this.audioManager = audioManager;
        reload(data);
    }

    public int[] getBuffers() {
        return this.internalResources.buffers;
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
        if (internalResources.buffers.length == 0) {
            internalResources.buffers = new int[BUFFER_POOL_SIZE];

            for (int i = 0; i < internalResources.buffers.length; i++) {
                internalResources.buffers[i] = AL10.alGenBuffers();
                OpenALException.checkState("Creating buffer");
            }

            lastUpdatedBuffer = internalResources.buffers[0];
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
            logger.error("Failed to reload {}", getUrn(), e); //NOPMD
        }
    }

    @Override
    protected Optional<? extends Asset<StreamingSoundData>> doCreateCopy(ResourceUrn copyUrn, AssetType<?,
            StreamingSoundData> parentAssetType) {
        return Optional.of(new OpenALStreamingSound(copyUrn, parentAssetType, stream, audioManager,
                new DisposalAction(copyUrn)));
    }


    public static class DisposalAction implements DisposableResource {

        protected int[] buffers = new int[0];
        private final ResourceUrn urn;
        private WeakReference<OpenALStreamingSound> asset;

        public DisposalAction(ResourceUrn urn) {
            this.urn = urn;
        }

        public void setAsset(OpenALStreamingSound asset) {
            this.asset = new WeakReference<>(asset);
        }


        @Override
        public void close() {
            try {
                GameThread.synch(() -> {
                    OpenALStreamingSound sound = asset.get();
                    if (sound != null) {
                        sound.audioManager.purgeSound(sound);
                    }

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
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
