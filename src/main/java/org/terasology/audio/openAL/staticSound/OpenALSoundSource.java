/*
* Copyright 2013 Moving Blocks
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.terasology.audio.openAL.staticSound;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.terasology.audio.openAL.BaseSoundSource;
import org.terasology.audio.openAL.OpenALException;
import org.terasology.audio.openAL.SoundPool;
import org.terasology.audio.openAL.SoundSource;

import static org.lwjgl.openal.AL10.*;

/**
 *
 */
public class OpenALSoundSource extends BaseSoundSource<OpenALSound> {

    public OpenALSoundSource(SoundPool pool) {
        super(pool);
    }

    public float getLength() {
        return audio.getLength();
    }

    /**
     * @return the playback position in second
     */
    public float getPlaybackPositionSeconds() {
        return (float) AL10.alGetSourcei(getSourceId(), AL11.AL_BYTE_OFFSET) / audio.getBufferSize();
    }

    // TODO: Work out what this method returns and rename it to fit
    public float getPlaybackPosition() {
        return (float) AL10.alGetSourcei(getSourceId(), AL11.AL_SAMPLE_OFFSET) / audio.getSamplingRate();
    }

    /**
     * Set playback position in seconds
     *
     * @param position
     */
    public SoundSource setPlaybackPositionInSeconds(float position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(getSourceId());
        }

        AL10.alSourceRewind(getSourceId());
        AL10.alSourcei(getSourceId(), AL11.AL_SAMPLE_OFFSET, (int) (audio.getSamplingRate() * position));

        OpenALException.checkState("Setting sound playback absolute position");

        if (playing) {
            play();
        }

        return this;
    }

    /**
     * Set relative playback position (0.0f - start, 1.0f - end)
     *
     * @param position
     */
    // TODO: This is broken for compressed streams - is this something that we need to worry about?
    public SoundSource setPlaybackPosition(float position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(getSourceId());
        }

        AL10.alSourceRewind(getSourceId());
        AL10.alSourcei(getSourceId(), AL11.AL_BYTE_OFFSET, (int) (audio.getBufferSize() * position));

        OpenALException.checkState("Setting sound playback relaive position");

        if (playing) {
            play();
        }

        return this;
    }

    @Override
    public boolean isLooping() {
        return alGetSourcei(getSourceId(), AL_LOOPING) == AL_TRUE;
    }

    @Override
    public SoundSource setLooping(boolean looping) {
        alSourcei(getSourceId(), AL_LOOPING, looping ? AL_TRUE : AL_FALSE);

        OpenALException.checkState("Setting sound looping");

        return this;
    }

    @Override
    public SoundSource setAudio(OpenALSound sound) {
        boolean playing = isPlaying();
        if (playing) {
            stop();
        }

        reset();

        audio = sound;
        AL10.alSourcei(getSourceId(), AL10.AL_BUFFER, audio.getBufferId());

        OpenALException.checkState("Assigning buffer to source");

        if (playing) {
            play();
        }

        return this;
    }
}