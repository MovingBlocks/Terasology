// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL;

import org.joml.Vector3fc;
import org.terasology.engine.audio.Sound;

/**
 * Interface for a sound that includes the data required for relative sound positioning.
 * @param <T>
 */

public interface SoundSource<T extends Sound<?>> {

    /**
     * Start sound playback
     *
     * @return
     */
    SoundSource<T> play();

    /**
     * Stop sound playback
     *
     * @return
     */
    SoundSource<T> stop();

    /**
     * Pause sound playback
     *
     * @return
     */
    SoundSource<T> pause();

    /**
     * Returns true if sound is currently playing, or intended to be played
     *
     * @return
     */
    boolean isPlaying();

    /**
     * Update method, use it for position update, buffer switching, etc
     */
    void update(float delta);

    /**
     * Set sound source absolute positioning.
     * This means sound source position would be updated on listener move
     *
     * @param absolute
     * @return
     */
    SoundSource<T> setAbsolute(boolean absolute);

    /**
     * Returns true if sound source is absolute relative to listener
     *
     * @return
     */
    boolean isAbsolute();

    /**
     * Set sound source position in space
     *
     * @param pos
     * @return
     */
    SoundSource<T> setPosition(Vector3fc pos);

    /**
     * Returns sound position in space
     *
     * @return
     */
    Vector3fc getPosition();

    /**
     * Set sound source velocity
     * Sound source velocity used for doppler effect calculation
     *
     * @param velocity
     * @return the sound source
     */
    SoundSource<T> setVelocity(Vector3fc velocity);

    /**
     * Returns sound source velocity
     *
     * @return
     */
    Vector3fc getVelocity();

    /**
     * Set sound source direction in cartesian coordinates
     *
     * @param direction
     * @return the sound source
     */
    SoundSource<T> setDirection(Vector3fc direction);

    /**
     * Returns sound source direction in cartesian coordinates
     *
     * @return
     */
    Vector3fc getDirection();

    /**
     * Returns sound source pitch
     *
     * @return
     */
    float getPitch();

    /**
     * Sets sound source pitch
     *
     * @param pitch
     * @return the sound source
     */
    SoundSource<T> setPitch(float pitch);

    /**
     * Returns sound source gain
     *
     * @return
     */
    float getGain();

    /**
     * Updates gain, used after pool volume is altered
     */
    void updateGain();

    /**
     * Set sound source gain
     *
     * @param gain
     * @return the sound source
     */
    SoundSource<T> setGain(float gain);

    /**
     * Returns true if sound source is looped (sound will be repeated)
     *
     * @return
     */
    boolean isLooping();

    /**
     * Set sound source looping
     * WARNING! This will cause UnsupportedOperationException on streaming sounds
     *
     * @param looping
     * @return the sound source
     */
    SoundSource<T> setLooping(boolean looping);

    /**
     * Set source of sound (samples)
     *
     * @param sound
     * @return the sound source
     */
    SoundSource<T> setAudio(T sound);

    /**
     * Returns sound of source :)
     *
     * @return
     */
    T getAudio();

    /**
     * Fade source smoothly
     *
     * @param targetGain
     * @return the sound source
     */
    SoundSource<T> fade(float targetGain);

    SoundSource<T> reset();

    void purge();
}
