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

import javax.vecmath.Vector3f;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.terasology.audio.Sound;
import org.terasology.audio.AudioManager;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.openal.AL10.*;

public class BasicSoundSource implements SoundSource {

    private SoundPool owningPool;

    protected Sound audio;
    protected int sourceId;

    protected float srcGain = 1.0f;
    protected float targetGain = 1.0f;
    protected boolean fade = false;

    protected Vector3f position = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    protected Vector3f direction = new Vector3f();

    protected boolean absolutePosition = false;

    protected boolean playing = false;

    public BasicSoundSource(SoundPool pool) {
        this.owningPool = pool;
        sourceId = alGenSources();
        OpenALException.checkState("Creating sound source");

        reset();
    }

    @Override
    public SoundSource play() {
        if (!isPlaying()) {
            AL10.alSourcePlay(getSourceId());
            playing = true;
        }

        return this;
    }

    @Override
    public SoundSource stop() {
        if (audio != null) {
            audio.reset();
        }

        alSourceStop(getSourceId());
        OpenALException.checkState("Stop playback");

        alSourceRewind(getSourceId());
        OpenALException.checkState("Rewind");


        playing = false;

        return this;
    }

    @Override
    public SoundSource pause() {
        if (isPlaying()) {
            AL10.alSourcePause(getSourceId());

            OpenALException.checkState("Pause playback");

            playing = false;
        }

        return this;
    }

    @Override
    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING || playing;
    }

    @Override
    public void update(float delta) {
        updateFade(delta);
        updateState();
    }

    protected void updateState() {
        if (playing && alGetSourcei(sourceId, AL_SOURCE_STATE) != AL_PLAYING) {
            playing = false; // sound stop playing
        }
    }

    @Override
    public SoundSource reset() {
        setPitch(1.0f);
        setLooping(false);
        setGain(1.0f);
        setAbsolute(true);

        Vector3f zeroVector = new Vector3f();
        setPosition(zeroVector);
        setVelocity(zeroVector);
        setDirection(zeroVector);

        // some additional settings
        alSourcef(getSourceId(), AL_MAX_DISTANCE, AudioManager.MAX_DISTANCE);
        alSourcef(getSourceId(), AL_REFERENCE_DISTANCE, 1f);
        AL10.alSourcei(getSourceId(), AL_SOURCE_RELATIVE, AL_FALSE);
        AL10.alSourcef(getSourceId(), AL_ROLLOFF_FACTOR, 1.0f);

        fade = false;
        srcGain = 1.0f;
        targetGain = 1.0f;

        return this;
    }

    @Override
    public int getLength() {
        return audio.getLength();
    }

    @Override
    public SoundSource setPlaybackPosition(int position) {
        boolean playing = isPlaying();
        if (playing) {
            AL10.alSourceStop(getSourceId());
        }

        AL10.alSourceRewind(getSourceId());
        AL10.alSourcei(getSourceId(), AL11.AL_SAMPLE_OFFSET, audio.getSamplingRate() * position);

        OpenALException.checkState("Setting sound playback absolute position");

        if (playing) {
            play();
        }

        return this;
    }

    @Override
    public int getPlaybackPosition() {
        return AL10.alGetSourcei(getSourceId(), AL11.AL_SAMPLE_OFFSET) / audio.getSamplingRate();
    }

    @Override
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
    public float getPlaybackPositionf() {
        return AL10.alGetSourcei(getSourceId(), AL11.AL_BYTE_OFFSET) / audio.getBufferSize();
    }

    @Override
    public SoundSource setAbsolute(boolean absolute) {
        absolutePosition = absolute;
        AL10.alSourcei(getSourceId(), AL_SOURCE_RELATIVE, (absolute) ? AL_FALSE : AL_TRUE);

        return this;
    }

    @Override
    public boolean isAbsolute() {
        return absolutePosition;
    }

    @Override
    public Vector3f getVelocity() {
        return velocity;
    }

    @Override
    public SoundSource setVelocity(Vector3f velocity) {
        if (velocity == null || this.velocity.equals(velocity)) {
            return this;
        }

        this.velocity.set(velocity);

        AL10.alSource3f(getSourceId(), AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);

        OpenALException.checkState("Setting sound source velocity");

        return this;
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public SoundSource setPosition(Vector3f position) {
        if (position == null || this.position.equals(position)) {
            return this;
        }

        this.position.set(position);
        alSource3f(getSourceId(), AL10.AL_POSITION, position.x, position.y, position.z);

        OpenALException.checkState("Changing sound position");

        return this;
    }

    @Override
    public SoundSource setDirection(Vector3f direction) {
        if (direction == null || this.direction.equals(direction)) {
            return this;
        }

        AL10.alSource3f(getSourceId(), AL10.AL_DIRECTION, direction.x, direction.y, direction.z);

        OpenALException.checkState("Setting sound source direction");

        this.direction.set(direction);

        return this;
    }

    @Override
    public Vector3f getDirection() {
        return direction;
    }

    @Override
    public float getPitch() {
        return AL10.alGetSourcef(getSourceId(), AL10.AL_PITCH);
    }

    @Override
    public SoundSource setPitch(float pitch) {
        AL10.alSourcef(getSourceId(), AL10.AL_PITCH, pitch);

        OpenALException.checkState("Setting sound pitch");

        return this;
    }

    @Override
    public float getGain() {
        return srcGain;
    }

    @Override
    public void updateGain() {
        alSourcef(getSourceId(), AL_GAIN, srcGain * owningPool.getVolume());

        OpenALException.checkState("Setting sound gain");
    }

    @Override
    public SoundSource setGain(float gain) {
        srcGain = gain;
        alSourcef(getSourceId(), AL_GAIN, gain * owningPool.getVolume());

        OpenALException.checkState("Setting sound gain");

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
    public SoundSource setAudio(Sound sound) {
        boolean playing = isPlaying();
        if (playing) {
            stop();
        }

        reset();

        if (sound instanceof OpenALSound) {
            audio = sound;
            AL10.alSourcei(getSourceId(), AL10.AL_BUFFER, audio.getBufferId());

            OpenALException.checkState("Assigning buffer to source");
        } else {
            throw new IllegalArgumentException("Unsupported sound object!");
        }

        if (playing) {
            play();
        }

        return this;
    }

    @Override
    public Sound getAudio() {
        return audio;
    }

    @Override
    public SoundSource fade(float targetGain) {
        this.targetGain = targetGain;
        fade = true;

        return this;
    }

    public int getSourceId() {
        return sourceId;
    }

    private void updateFade(float delta) {
        if (!fade) {
            return;
        }

        float newGain = Math.max(targetGain, srcGain - delta);
        if (newGain == 0.0f) {
            stop();
        } else {
            setGain(newGain);
        }
        if (targetGain == newGain) {
            fade = false;
        }
    }

    private Vector3f getCameraPosition() {
        return CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
    }

    @Override
    // TODO: This is no guaranteed to be executed at all – move to a safer place
    protected void finalize() throws Throwable {
        if (sourceId != 0) {
            AL10.alDeleteSources(sourceId);
        }
        super.finalize();
    }

    @Override
    public int hashCode() {
        return getSourceId();
    }


}
