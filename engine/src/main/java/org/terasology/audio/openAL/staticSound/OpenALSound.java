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
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StaticSoundData;
import org.terasology.audio.openAL.OpenALException;
import org.terasology.audio.openAL.OpenALManager;

import static org.lwjgl.openal.AL10.AL_BITS;
import static org.lwjgl.openal.AL10.AL_CHANNELS;
import static org.lwjgl.openal.AL10.AL_FREQUENCY;
import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGetBufferi;

public final class OpenALSound extends StaticSound {

    protected float length;
    private final OpenALManager audioManager;
    private StaticSoundData data;

    // TODO: Do we have proper support for unloading sounds (as mods are changed?)
    private int bufferId;

    public OpenALSound(ResourceUrn urn, StaticSoundData data, AssetType<?, StaticSoundData> assetType, OpenALManager audioManager) {
        super(urn, assetType);
        this.audioManager = audioManager;
        reload(data);
    }

    private OpenALSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType, OpenALManager audioManager, float length, int bufferId) {
        super(urn, assetType);
        this.audioManager = audioManager;
        this.length = length;
        this.bufferId = bufferId;
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
        this.data = newData;
        if (bufferId == 0) {
            bufferId = alGenBuffers();
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
    }

    @Override
    protected Asset<StaticSoundData> doCreateInstance(ResourceUrn instanceUrn, AssetType<?, StaticSoundData> parentAssetType) {
        return new OpenALSound(instanceUrn, data, parentAssetType, audioManager);
    }

    @Override
    protected void doDispose() {
        if (bufferId != 0) {
            audioManager.purgeSound(this);
            alDeleteBuffers(bufferId);
            bufferId = 0;
            OpenALException.checkState("Deleting buffer data");
            data = null;
        }
    }
}
