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

import java.util.Set;

public interface SoundPool {

    /**
     * Returns sound source tuned for specified sound with specified priority
     *
     * @param sound
     * @param priority
     * @return
     */
    SoundSource getSource(Sound sound, int priority);

    /**
     * Returns sound source tuned for specified sound with normal priority
     *
     * @param sound
     * @return
     */
    SoundSource getSource(Sound sound);

    /**
     * Returns all available sound sources
     * Do not use it for any purpose except sound management
     *
     * @return
     */
    Set<SoundSource> getSources();

    /**
     * Returns all inactive (available) sources
     *
     * @return
     */
    Set<SoundSource> getInactiveSources();

    /**
     * Returns all active or locked sources
     *
     * @return
     */
    Set<SoundSource> getActiveSources();

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
    boolean isInPool(SoundSource source);

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

}
