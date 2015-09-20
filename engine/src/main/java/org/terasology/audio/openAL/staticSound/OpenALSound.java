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
package org.terasology.audio.openAL.staticSound;

import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StaticSoundData;
import org.terasology.audio.openAL.OpenALException;
import org.terasology.audio.openAL.OpenALManager;
import org.terasology.engine.GameThread;

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

    private DisposalAction disposalAction;

    // TODO: Do we have proper support for unloading sounds (as mods are changed?)
    private int bufferId;

    public OpenALSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType, StaticSoundData data, OpenALManager audioManager) {
        super(urn, assetType);
        this.audioManager = audioManager;
        disposalAction = new DisposalAction(urn, this);
        getDisposalHook().setDisposeAction(disposalAction);
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

                AL10.alBufferData(bufferId, newData.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, newData.getData(), newData.getSampleRate());
                OpenALException.checkState("Allocating sound buffer");

                int bits = newData.getBufferBits();
                int size = getBufferSize();
                int channels = getChannels();
                int frequency = getSamplingRate();
                length = (float) size / channels / (bits / 8) / frequency;
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e);
        }
    }

    private static class DisposalAction implements Runnable {
        private final ResourceUrn urn;
        private int bufferId;
        private final WeakReference<OpenALSound> asset;

        public DisposalAction(ResourceUrn urn, OpenALSound openALSound) {
            this.urn = urn;
             asset = new WeakReference<>(openALSound);
        }

        @Override
        public void run() {
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
