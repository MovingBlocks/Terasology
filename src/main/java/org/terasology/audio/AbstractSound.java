/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.audio;

import org.terasology.asset.AssetUri;

import static org.lwjgl.openal.AL10.*;

public abstract class AbstractSound implements Sound {

    // TODO: Do we have proper support for unloading sounds (as mods are changed?)

    private AssetUri uri;
    private int bufferId = 0;
    protected int length = 0;

    public AbstractSound(AssetUri uri, int bufferId) {
        this.uri = uri;
        this.bufferId = bufferId;

        OpenALException.checkState("Allocating sound buffer");
    }

    @Override
    public int getLength() {
        if (length == 0 && bufferId != 0) { // only if buffer is already initialized
            int bits = getBufferBits();
            int size = getBufferSize();
            int channels = getChannels();
            int frequency = getSamplingRate();

            length = size / channels / (bits / 8) / frequency;
        }

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

    @Override
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
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void reset() {
    }

    @Override
    public void dispose() {
        if (bufferId != 0) {
            // TODO: need to ensure the sound is not in use, or stop it?
            //alDeleteBuffers(bufferId);
            //bufferId = 0;
            //OpenALException.checkState("Deleting buffer data");
        }
    }
}
