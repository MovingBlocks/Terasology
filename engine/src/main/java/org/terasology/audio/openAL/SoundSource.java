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
package org.terasology.audio.openAL;

import org.terasology.audio.Sound;
import org.terasology.math.geom.Vector3f;

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
    SoundSource<T> setPosition(Vector3f pos);

    /**
     * Returns sound position in space
     *
     * @return
     */
    Vector3f getPosition();

    /**
     * Set sound source velocity
     * Sound source velocity used for doppler effect calculation
     *
     * @param velocity
     * @return
     */
    SoundSource<T> setVelocity(Vector3f velocity);

    /**
     * Returns sound source velocity
     *
     * @return
     */
    Vector3f getVelocity();

    /**
     * Set sound source direction in cartesian coordinates
     *
     * @param direction
     * @return
     */
    SoundSource<T> setDirection(Vector3f direction);

    /**
     * Returns sound source direction in cartesian coordinates
     *
     * @return
     */
    Vector3f getDirection();

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
     * @return
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
     * @return
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
     * @return
     */
    SoundSource<T> setLooping(boolean looping);

    /**
     * Set source of sound (samples)
     *
     * @param sound
     * @return
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
     * @return
     */
    SoundSource<T> fade(float targetGain);

    SoundSource<T> reset();

    void purge();
}
