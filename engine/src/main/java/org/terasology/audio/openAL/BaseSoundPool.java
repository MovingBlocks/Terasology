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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.terasology.audio.AudioManager;
import org.terasology.audio.Sound;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public abstract class BaseSoundPool<SOUND extends Sound<?>, SOUNDSOURCE extends SoundSource<SOUND>> implements SoundPool<SOUND, SOUNDSOURCE> {

    private static final int DEFAULT_POOL_SIZE = 32;

    protected Map<SOUNDSOURCE, Integer> soundSources;

    private float volume;

    public BaseSoundPool(int capacity) {
        soundSources = Maps.newHashMapWithExpectedSize(capacity);

        this.fillPool(capacity);
    }

    public BaseSoundPool() {
        this(DEFAULT_POOL_SIZE);
    }

    public SOUNDSOURCE getLockedSource() {
        for (SOUNDSOURCE source : soundSources.keySet()) {
            if (!isActive(source)) {
                if (lock(source)) {
                    return source;
                }
            }
        }

        return null;
    }

    @Override
    public SOUNDSOURCE getSource(SOUND sound, int priority) {
        if (sound == null) {
            return null;
        }

        // TODO: should be optimized (performance crucial)
        for (SOUNDSOURCE source : soundSources.keySet()) {
            if (!isActive(source)) {
                soundSources.put(source, priority);
                return (SOUNDSOURCE) source.setAudio(sound);
            }
        }

        // No free sound found, will look by priority
        for (Map.Entry<SOUNDSOURCE, Integer> entry : soundSources.entrySet()) {
            SOUNDSOURCE source = entry.getKey();
            Integer soundPriority = entry.getValue();

            if (soundPriority < priority) { // sound playing wil lower priority than our query
                soundSources.put(source, priority);
                return (SOUNDSOURCE) source.setAudio(sound);
            }
        }

        return null;
    }

    @Override
    public SOUNDSOURCE getSource(SOUND sound) {
        return getSource(sound, AudioManager.PRIORITY_NORMAL);
    }

    @Override
    public Set<SOUNDSOURCE> getSources() {
        return soundSources.keySet();
    }

    @Override
    public Set<SOUNDSOURCE> getInactiveSources() {
        Set<SOUNDSOURCE> inactiveSources = Sets.newHashSet();

        for (SOUNDSOURCE source : soundSources.keySet()) {
            if (!isActive(source)) {
                inactiveSources.add(source);
            }
        }

        return inactiveSources;
    }

    @Override
    public Set<SOUNDSOURCE> getActiveSources() {
        Set<SOUNDSOURCE> inactiveSources = Sets.newHashSet();

        for (SOUNDSOURCE source : soundSources.keySet()) {
            if (isActive(source)) {
                inactiveSources.add(source);
            }
        }

        return inactiveSources;
    }

    @Override
    public void stopAll() {
        for (SOUNDSOURCE source : soundSources.keySet()) {
            source.stop();
        }
    }

    @Override
    public void update(float delta) {
        for (SOUNDSOURCE source : soundSources.keySet()) {
            if (source.isPlaying()) {
                source.update(delta);
            }
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        for (SOUNDSOURCE soundSource : soundSources.keySet()) {
            soundSource.updateGain();
        }
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    @Override
    public int size() {
        return soundSources.size();
    }

    @Override
    public boolean isInPool(SOUNDSOURCE source) {
        return soundSources.containsKey(source);
    }

    public boolean isLocked(SOUNDSOURCE source) {
        Integer lock = soundSources.get(source);
        return lock != null && lock == AudioManager.PRIORITY_LOCKED;
    }

    public boolean lock(SOUNDSOURCE source) {
        if (isLocked(source) && !isInPool(source)) {
            return false;
        }

        soundSources.put(source, AudioManager.PRIORITY_LOCKED);

        return true;
    }

    public void unlock(SOUNDSOURCE source) {
        soundSources.put(source, null);
    }

    public boolean isActive(SOUNDSOURCE source) {
        return isLocked(source) || source.isPlaying();
    }

    protected abstract SOUNDSOURCE createSoundSource();

    private void fillPool(int capacity) {
        for (int i = 0; i < capacity; i++) {
            this.soundSources.put(createSoundSource(), null);
        }
    }

    @Override
    public void purge(Sound<?> sound) {
        for (SOUNDSOURCE source : soundSources.keySet()) {
            if (sound.equals(source.getAudio())) {
                source.purge();
            }
        }
    }

}
