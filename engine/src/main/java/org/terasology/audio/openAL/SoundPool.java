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

import java.util.Set;

public interface SoundPool<SOUND extends Sound<?>, SOUNDSOURCE extends SoundSource<SOUND>> {

    /**
     * Returns sound source tuned for specified sound with specified priority
     *
     * @param sound
     * @param priority
     * @return
     */
    SOUNDSOURCE getSource(SOUND sound, int priority);

    /**
     * Returns sound source tuned for specified sound with normal priority
     *
     * @param sound
     * @return
     */
    SOUNDSOURCE getSource(SOUND sound);

    /**
     * Returns all available sound sources
     * Do not use it for any purpose except sound management
     *
     * @return
     */
    Set<SOUNDSOURCE> getSources();

    /**
     * Returns all inactive (available) sources
     *
     * @return
     */
    Set<SOUNDSOURCE> getInactiveSources();

    /**
     * Returns all active or locked sources
     *
     * @return
     */
    Set<SOUNDSOURCE> getActiveSources();

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
    boolean isInPool(SOUNDSOURCE source);

    /**
     * Stop playback of all sources of this pool
     */
    void stopAll();

    /**
     * Update sound sources
     */
    void update(float delta);

    void setVolume(float volume);

    float getVolume();

    void purge(Sound<?> sound);

}
