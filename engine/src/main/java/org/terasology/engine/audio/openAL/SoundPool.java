// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL;

import org.terasology.engine.audio.Sound;

import java.util.Set;

public interface SoundPool<SOUND extends Sound<?>, SOURCE extends SoundSource<SOUND>> {

    /**
     * Returns sound source tuned for specified sound with specified priority
     *
     * @param sound
     * @param priority
     * @return
     */
    SOURCE getSource(SOUND sound, int priority);

    /**
     * Returns sound source tuned for specified sound with normal priority
     *
     * @param sound
     * @return
     */
    SOURCE getSource(SOUND sound);

    /**
     * Returns all available sound sources
     * Do not use it for any purpose except sound management
     *
     * @return
     */
    Set<SOURCE> getSources();

    /**
     * Returns all inactive (available) sources
     *
     * @return
     */
    Set<SOURCE> getInactiveSources();

    /**
     * Returns all active or locked sources
     *
     * @return
     */
    Set<SOURCE> getActiveSources();

    /**
     * Returns sound sources amount in this pool
     *
     * @return
     */
    int size();

    /**
     * Checks if specified sound source is part of this pool
     *
     * @param source
     * @return
     */
    boolean isInPool(SOURCE source);

    /**
     * Stop playback of all sources of this pool
     */
    void stopAll();

    /**
     * Update sound sources
     */
    void update(float delta);

    /**
     * @param volume The new volume to set the sounds to
     */
    void setVolume(float volume);


    /**
     * @return The volume of the sounds in the pool
     */
    float getVolume();

    /**
     * Remove a sound from the pool
     *
     * @param sound The sound to remove
     */
    void purge(Sound<?> sound);

}
