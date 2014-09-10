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
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
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

public final class OpenALSound extends AbstractAsset<StaticSoundData> implements StaticSound {

    protected float length;
    private final OpenALManager audioManager;

    // TODO: Do we have proper support for unloading sounds (as mods are changed?)
    private int bufferId;


    public OpenALSound(AssetUri uri, StaticSoundData data, OpenALManager audioManager) {
        super(uri);
        this.audioManager = audioManager;
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
    public void dispose() {
        if (bufferId != 0) {
            audioManager.purgeSound(this);
            alDeleteBuffers(bufferId);
            bufferId = 0;
            OpenALException.checkState("Deleting buffer data");
        }
    }

    @Override
    public boolean isDisposed() {
        return bufferId == 0;
    }

    @Override
    public void reload(StaticSoundData data) {
        if (bufferId == 0) {
            bufferId = alGenBuffers();
        } else {
            audioManager.purgeSound(this);
        }

        AL10.alBufferData(bufferId, data.getChannels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data.getData(), data.getSampleRate());
        OpenALException.checkState("Allocating sound buffer");

        int bits = data.getBufferBits();
        int size = getBufferSize();
        int channels = getChannels();
        int frequency = getSamplingRate();
        length = (float) size / channels / (bits / 8) / frequency;
    }
}
