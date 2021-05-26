// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.openal.AL10;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.Sound;

import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_MAX_DISTANCE;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;
import static org.lwjgl.openal.AL10.AL_ROLLOFF_FACTOR;
import static org.lwjgl.openal.AL10.AL_SOURCE_RELATIVE;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourceRewind;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;

public abstract class BaseSoundSource<T extends Sound<?>> implements SoundSource<T> {

    private int sourceId;

    private float srcGain = 1.0f;
    private float targetGain = 1.0f;
    private boolean fade;

    private final Vector3f position = new Vector3f();
    private final Vector3f velocity = new Vector3f();
    private final Vector3f direction = new Vector3f();

    private boolean absolutePosition;

    private SoundPool<T, ? extends SoundSource<T>> owningPool;

    public BaseSoundSource(SoundPool<T, ? extends SoundSource<T>> pool) {
        this.owningPool = pool;
        sourceId = alGenSources();
        OpenALException.checkState("Creating sound source");

        reset();
    }

    @Override
    public SoundSource<T> play() {
        if (!isPlaying()) {
            AL10.alSourcePlay(getSourceId());
        }

        return this;
    }

    @Override
    public SoundSource<T> stop() {
        alSourceStop(getSourceId());
        OpenALException.checkState("Stop playback");

        alSourceRewind(getSourceId());
        OpenALException.checkState("Rewind");

        return this;
    }

    @Override
    public SoundSource<T> pause() {
        if (isPlaying()) {
            AL10.alSourcePause(getSourceId());

            OpenALException.checkState("Pause playback");
        }

        return this;
    }

    @Override
    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    @Override
    public void update(float delta) {
        updateFade(delta);
        updateState();
    }

    protected void updateState() {
    }

    @Override
    public SoundSource<T> reset() {
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
        AL10.alSourcef(getSourceId(), AL_ROLLOFF_FACTOR, 0.25f);

        fade = false;
        srcGain = 1.0f;
        targetGain = 1.0f;

        return this;
    }


    @Override
    public SoundSource<T> setAbsolute(boolean absolute) {
        absolutePosition = absolute;
        AL10.alSourcei(getSourceId(), AL_SOURCE_RELATIVE, (absolute) ? AL_FALSE : AL_TRUE);

        return this;
    }

    @Override
    public boolean isAbsolute() {
        return absolutePosition;
    }

    @Override
    public Vector3fc getVelocity() {
        return velocity;
    }

    @Override
    public SoundSource<T> setVelocity(Vector3fc value) {
        if (velocity.equals(value)) {
            return this;
        }

        velocity.set(value);
        AL10.alSource3f(getSourceId(), AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
        OpenALException.checkState("Setting sound source velocity");

        return this;
    }

    public Vector3fc getPosition() {
        return position;
    }

    @Override
    public SoundSource<T> setPosition(Vector3fc value) {
        if (position.equals(value)) {
            return this;
        }

        position.set(value);
        alSource3f(getSourceId(), AL10.AL_POSITION, position.x, position.y, position.z);
        OpenALException.checkState("Changing sound position");

        return this;
    }

    @Override
    public SoundSource<T> setDirection(Vector3fc value) {
        if (direction.equals(value)) {
            return this;
        }

        direction.set(value);
        AL10.alSource3f(getSourceId(), AL10.AL_DIRECTION, direction.x, direction.y, direction.z);
        OpenALException.checkState("Setting sound source direction");

        return this;
    }

    @Override
    public Vector3fc getDirection() {
        return direction;
    }

    @Override
    public float getPitch() {
        return AL10.alGetSourcef(getSourceId(), AL10.AL_PITCH);
    }

    @Override
    public SoundSource<T> setPitch(float pitch) {
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
    public SoundSource<T> setGain(float gain) {
        srcGain = gain;
        alSourcef(getSourceId(), AL_GAIN, gain * owningPool.getVolume());

        OpenALException.checkState("Setting sound gain");

        return this;
    }

    @Override
    public SoundSource<T> fade(float value) {
        this.targetGain = value;
        fade = true;

        return this;
    }

    protected int getSourceId() {
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

    public void dispose() {
        if (sourceId != 0) {
            AL10.alDeleteSources(sourceId);
        }
    }
}
