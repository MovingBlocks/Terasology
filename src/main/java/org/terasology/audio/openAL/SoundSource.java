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

import org.terasology.audio.Sound;

import javax.vecmath.Vector3f;

public interface SoundSource {

    /**
     * Start sound playback
     *
     * @return
     */
    public SoundSource play();

    /**
     * Stop sound playback
     *
     * @return
     */
    public SoundSource stop();

    /**
     * Pause sound playback
     *
     * @return
     */
    public SoundSource pause();

    /**
     * Returns true if sound is currently playing, or intended to be played
     *
     * @return
     */
    public boolean isPlaying();

    /**
     * Update method, use it for position update, buffer switching, etc
     */
    public void update(float delta);

    /**
     * Returns audio length in seconds
     * Will return -1 in sound is streaming
     *
     * @return
     */
    public int getLength();

    /**
     * Set playback position in seconds
     *
     * @param position
     */
    public SoundSource setPlaybackPosition(int position);

    /**
     * Returns sound playback position in seconds
     *
     * @return
     */
    public int getPlaybackPosition();

    /**
     * Set relative playback position (0.0f - start, 1.0f - end)
     *
     * @param position
     */
    public SoundSource setPlaybackPosition(float position);

    /**
     * Returns relative playback position (0.0f - start, 1.0f - end)
     *
     * @return
     */
    public float getPlaybackPositionf();

    /**
     * Set sound source absolute positioning.
     * This means sound source position would be updated on listener move
     *
     * @param absolute
     * @return
     */
    public SoundSource setAbsolute(boolean absolute);

    /**
     * Returns true if sound source is absolute relative to listener
     *
     * @return
     */
    public boolean isAbsolute();

    /**
     * Set sound source position in space
     *
     * @param pos
     * @return
     */
    public SoundSource setPosition(Vector3f pos);

    /**
     * Returns sound position in space
     *
     * @return
     */
    public Vector3f getPosition();

    /**
     * Set sound source velocity
     * Sound source velocity used for doppler effect calculation
     *
     * @param velocity
     * @return
     */
    public SoundSource setVelocity(Vector3f velocity);

    /**
     * Returns sound source velocity
     *
     * @return
     */
    public Vector3f getVelocity();

    /**
     * Set sound source direction in cartesian coordinates
     *
     * @param direction
     * @return
     */
    public SoundSource setDirection(Vector3f direction);

    /**
     * Returns sound source direction in cartesian coordinates
     *
     * @return
     */
    public Vector3f getDirection();

    /**
     * Returns sound source pitch
     *
     * @return
     */
    public float getPitch();

    /**
     * Sets sound source pitch
     *
     * @param pitch
     * @return
     */
    public SoundSource setPitch(float pitch);

    /**
     * Returns sound source gain
     *
     * @return
     */
    public float getGain();

    /**
     * Updates gain, used after pool volume is altered
     */
    public void updateGain();

    /**
     * Set sound source gain
     *
     * @param gain
     * @return
     */
    public SoundSource setGain(float gain);

    /**
     * Returns true if sound source is looped (sound will be repeated)
     *
     * @return
     */
    public boolean isLooping();

    /**
     * Set sound source looping
     * WARNING! This will cause UnsupportedOperationException on streaming sounds
     *
     * @param looping
     * @return
     */
    public SoundSource setLooping(boolean looping);

    /**
     * Set source of sound (samples)
     *
     * @param sound
     * @return
     */
    public SoundSource setAudio(Sound sound);

    /**
     * Returns sound of source :)
     *
     * @return
     */
    public Sound getAudio();

    /**
     * Fade source smoothly
     *
     * @param targetGain
     * @return
     */
    public SoundSource fade(float targetGain);

    public SoundSource reset();
}
