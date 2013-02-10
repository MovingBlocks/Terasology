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
import org.terasology.audio.AudioManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicSoundPool implements SoundPool {
    private final static int DEFAULT_POOL_SIZE = 32;

    private float volume;

    protected Map<SoundSource, Integer> soundSources;

    public BasicSoundPool(int capacity) {
        soundSources = new HashMap<SoundSource, Integer>(capacity);

        this.fillPool(capacity);
    }

    public BasicSoundPool() {
        this(DEFAULT_POOL_SIZE);
    }

    public SoundSource getLockedSource() {
        for (SoundSource source : soundSources.keySet()) {
            if (!isActive(source)) {
                if (lock(source)) {
                    return source;
                }
            }
        }

        return null;
    }

    public SoundSource getSource(Sound sound, int priority) {
        if (sound == null) {
            return null;
        }

        // @todo should be optimized (performance crucial)
        for (SoundSource source : soundSources.keySet()) {
            if (!isActive(source)) {
                soundSources.put(source, priority);
                return source.setAudio(sound);
            }
        }

        // No free sound found, will look by priority
        for (Map.Entry<SoundSource, Integer> entry : soundSources.entrySet()) {
            SoundSource source = entry.getKey();
            Integer soundPriority = entry.getValue();

            if (soundPriority < priority) { // sound playing wil lower priority than our query
                soundSources.put(source, priority);
                return source.setAudio(sound);
            }
        }

        return null;
    }

    public SoundSource getSource(Sound sound) {
        return getSource(sound, AudioManager.PRIORITY_NORMAL);
    }

    public Set<SoundSource> getSources() {
        return soundSources.keySet();
    }

    public Set<SoundSource> getInactiveSources() {
        Set<SoundSource> inactiveSources = new HashSet<SoundSource>();

        for (SoundSource source : soundSources.keySet()) {
            if (!isActive(source)) {
                inactiveSources.add(source);
            }
        }

        return inactiveSources;
    }

    public Set<SoundSource> getActiveSources() {
        Set<SoundSource> inactiveSources = new HashSet<SoundSource>();

        for (SoundSource source : soundSources.keySet()) {
            if (isActive(source)) {
                inactiveSources.add(source);
            }
        }

        return inactiveSources;
    }

    public void stopAll() {
        for (SoundSource source : soundSources.keySet()) {
            source.stop();
        }
    }

    public void update(float delta) {
        for (SoundSource source : soundSources.keySet()) {
            if (source.isPlaying()) {
                source.update(delta);
            }
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        for (SoundSource soundSource : soundSources.keySet()) {
            soundSource.updateGain();
        }
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    public int size() {
        return soundSources.size();
    }

    public boolean isInPool(SoundSource source) {
        return soundSources.containsKey(source);
    }

    public boolean isLocked(SoundSource source) {
        Integer lock = soundSources.get(source);
        return lock != null && lock == AudioManager.PRIORITY_LOCKED;
    }

    public boolean lock(SoundSource source) {
        if (isLocked(source) && !isInPool(source)) {
            return false;
        }

        soundSources.put(source, AudioManager.PRIORITY_LOCKED);

        return true;
    }

    public void unlock(SoundSource source) {
        soundSources.put(source, null);
    }

    public boolean isActive(SoundSource source) {
        return isLocked(source) || source.isPlaying();
    }

    protected SoundSource createSoundSource() {
        return new BasicSoundSource(this);
    }

    private void fillPool(int capacity) {
        for (int i = 0; i < capacity; i++) {
            this.soundSources.put(createSoundSource(), null);
        }
    }
}
