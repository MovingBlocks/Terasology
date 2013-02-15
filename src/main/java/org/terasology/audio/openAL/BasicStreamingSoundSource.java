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
package org.terasology.audio.openAL;

import org.lwjgl.BufferUtils;
import org.terasology.audio.Sound;

import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceQueueBuffers;
import static org.lwjgl.openal.AL10.alSourceRewind;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourceUnqueueBuffers;
import static org.lwjgl.openal.AL10.alSourcei;

public class BasicStreamingSoundSource extends BasicSoundSource {

    public BasicStreamingSoundSource(SoundPool owningPool) {
        super(owningPool);
    }


    @Override
    public float getPlaybackPositionf() {
        // @todo add logger warning
        return -1.0f;
    }

    @Override
    public SoundSource setPlaybackPosition(int position) {
        // do nothing
        // @todo add logger warning
        return this;
    }

    @Override
    public int getPlaybackPosition() {
        // @todo add logger warning
        return -1;
    }

    @Override
    public SoundSource setPlaybackPosition(float position) {
        // do nothing
        // @todo add logger warning
        return this;
    }

    @Override
    public boolean isLooping() {
        return false;
    }

    @Override
    public SoundSource setLooping(boolean looping) {
        if (looping) {
            throw new UnsupportedOperationException("Looping is unsupported on streaming sounds!");
        }

        return this;
    }

    @Override
    public void update(float delta) {
        int buffersProcessed = alGetSourcei(this.getSourceId(), AL_BUFFERS_PROCESSED);

        while (buffersProcessed-- > 0) {
            int buffer = alSourceUnqueueBuffers(this.getSourceId());
            OpenALException.checkState("Buffer unqueue");

            if (((OpenALStreamingSound) audio).updateBuffer(buffer)) {
                alSourceQueueBuffers(this.getSourceId(), buffer);
                OpenALException.checkState("Buffer refill");
            } else {
                playing = false; // we aren't playing anymore, because stream seems to end
            }
        }

        super.update(delta);
    }

    @Override
    protected void updateState() {
        // Start playing if playback for stopped by end of buffers
        if (playing && alGetSourcei(getSourceId(), AL_SOURCE_STATE) != AL_PLAYING) {
            alSourcePlay(this.getSourceId());
        }
    }

    public SoundSource setAudio(Sound sound) {
        boolean playing = this.isPlaying();
        if (playing) {
            alSourceStop(this.sourceId);
            alSourceRewind(this.sourceId);
        }

        if (sound instanceof OpenALStreamingSound) {
            alSourcei(this.getSourceId(), AL_BUFFER, 0);

            OpenALStreamingSound asa = (OpenALStreamingSound) sound;
            this.audio = asa;

            asa.reset();

            int[] buffers = asa.getBuffers();

            for (int buffer : buffers) {
                asa.updateBuffer(buffer);
            }

            alSourceQueueBuffers(this.getSourceId(), (IntBuffer) BufferUtils.createIntBuffer(buffers.length).put(buffers).flip());
        } else {
            throw new IllegalArgumentException("Unsupported sound object!");
        }

        if (playing) {
            this.play();
        }

        return this;
    }

}
