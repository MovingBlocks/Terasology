// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL.staticSound;

import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StaticSoundData;
import org.terasology.engine.audio.openAL.OpenALException;
import org.terasology.engine.audio.openAL.OpenALManager;
import org.terasology.engine.core.GameThread;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

import java.lang.ref.WeakReference;

import static org.lwjgl.openal.AL10.AL_BITS;
import static org.lwjgl.openal.AL10.AL_CHANNELS;
import static org.lwjgl.openal.AL10.AL_FREQUENCY;
import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGetBufferi;

public final class OpenALSound extends StaticSound {

    private static final Logger logger = LoggerFactory.getLogger(OpenALSound.class);

    protected float length;
    private final OpenALManager audioManager;
    private final DisposalAction disposalAction;

    // TODO: Do we have proper support for unloading sounds (as mods are changed?)
    private int bufferId;

    public OpenALSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType, StaticSoundData data,
                       OpenALManager audioManager, OpenALSound.DisposalAction disposalAction) {
        super(urn, assetType, disposalAction);
        disposalAction.setAsset(this);
        this.audioManager = audioManager;
        this.disposalAction = disposalAction;
        reload(data);
    }

    @Override
    public float getLength() {
        return length;
    }

    @Override
    public int getChannels() {
        return alGetBufferi(bufferId, AL_CHANNELS);
    }

    @Override
    public int getSamplingRate() {
        return alGetBufferi(bufferId, AL_FREQUENCY);
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getBufferBits() {
        return alGetBufferi(bufferId, AL_BITS);
    }

    @Override
    public int getBufferSize() {
        return alGetBufferi(bufferId, AL_SIZE);
    }

    @Override
    public void play() {
        audioManager.playSound(this);
    }

    @Override
    public void play(float volume) {
        audioManager.playSound(this, volume);
    }

    @Override
    protected void doReload(StaticSoundData newData) {
        try {
            GameThread.synch(() -> {
                if (bufferId == 0) {
                    bufferId = alGenBuffers();
                    disposalAction.bufferId = bufferId;
                } else {
                    audioManager.purgeSound(this);
                }

                AL10.alBufferData(bufferId, newData.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16,
                        newData.getData(), newData.getSampleRate());
                OpenALException.checkState("Allocating sound buffer");

                int bits = newData.getBufferBits();
                int size = getBufferSize();
                int channels = getChannels();
                int frequency = getSamplingRate();
                length = (float) size / channels / (bits / 8) / frequency;
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e); //NOPMD
        }
    }

    public static class DisposalAction implements DisposableResource {
        private final ResourceUrn urn;
        private int bufferId;
        private WeakReference<OpenALSound> asset;

        public DisposalAction(ResourceUrn urn) {
            this.urn = urn;
        }

        public OpenALSound getAsset() {
            return asset.get();
        }

        public void setAsset(OpenALSound asset) {
            this.asset = new WeakReference<>(asset);
        }

        @Override
        public void close() {
            try {
                GameThread.synch(() -> {
                    OpenALSound sound = asset.get();
                    if (bufferId != 0) {
                        if (sound != null) {
                            sound.audioManager.purgeSound(sound);
                        }
                        alDeleteBuffers(bufferId);
                        OpenALException.checkState("Deleting buffer data");
                    }
                });
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
