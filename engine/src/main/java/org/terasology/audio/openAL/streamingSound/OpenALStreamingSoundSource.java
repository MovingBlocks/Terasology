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

import org.lwjgl.BufferUtils;
import org.terasology.audio.openAL.BaseSoundSource;
import org.terasology.audio.openAL.OpenALException;
import org.terasology.audio.openAL.SoundPool;
import org.terasology.audio.openAL.SoundSource;

import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;

public class OpenALStreamingSoundSource
        extends BaseSoundSource<OpenALStreamingSound> {

    private OpenALStreamingSound audio;
    private boolean loop;

    public OpenALStreamingSoundSource(
            SoundPool<OpenALStreamingSound, OpenALStreamingSoundSource> owningPool) {
        super(owningPool);
    }

    @Override
    public SoundSource<OpenALStreamingSound> stop() {
        if (audio != null) {
            audio.reset();
        }
        return super.stop();
    }

    @Override
    public boolean isLooping() {
        return loop;
    }

    @Override
    public OpenALStreamingSound getAudio() {
        return audio;
    }

    @Override
    public OpenALStreamingSoundSource setLooping(boolean looping) {
        this.loop = looping;

        return this;
    }

    @Override
    public void update(float delta) {
        int buffersProcessed = alGetSourcei(this.getSourceId(),
                AL_BUFFERS_PROCESSED);

        while (buffersProcessed-- > 0) {
            int buffer = alSourceUnqueueBuffers(this.getSourceId());
            OpenALException.checkState("Buffer unqueue");

            if (audio.updateBuffer(buffer)) {
                alSourceQueueBuffers(this.getSourceId(), buffer);
                OpenALException.checkState("Buffer refill");
            } else {
                stop(); // we aren't playing anymore, because stream seems to end
            }

            if (!isPlaying() && isLooping()) {
                play();
            }
        }

        super.update(delta);
    }

    @Override
    protected void updateState() {
        // Start playing if playback for stopped by end of buffers
        if (isPlaying()
            && alGetSourcei(getSourceId(), AL_SOURCE_STATE) != AL_PLAYING) {
            alSourcePlay(this.getSourceId());
        } else if (isLooping()) {
            play();
        }
    }

    @Override
    public OpenALStreamingSoundSource setAudio(OpenALStreamingSound sound) {
        boolean isPlaying = this.isPlaying();
        if (isPlaying) {
            alSourceStop(getSourceId());
            alSourceRewind(getSourceId());
        }

        alSourcei(this.getSourceId(), AL_BUFFER, 0);

        this.audio = sound;

        sound.reset();

        int[] buffers = sound.getBuffers();

        for (int buffer : buffers) {
            sound.updateBuffer(buffer);
        }

        alSourceQueueBuffers(this.getSourceId(), (IntBuffer) BufferUtils
                .createIntBuffer(buffers.length).put(buffers).flip());

        if (isPlaying) {
            this.play();
        }

        return this;
    }

    @Override
    public void purge() {
        if (isPlaying()) {
            alSourceStop(getSourceId());
            alSourceRewind(getSourceId());
        }

        alSourcei(this.getSourceId(), AL_BUFFER, 0);
    }

}
