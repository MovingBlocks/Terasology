// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.openAL;

import com.google.common.collect.Maps;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.Sound;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseSoundPool<SOUND extends Sound<?>, SOURCE extends SoundSource<SOUND>> implements SoundPool<SOUND, SOURCE> {

    private static final int DEFAULT_POOL_SIZE = 32;

    protected Map<SOURCE, Integer> soundSources;

    private float volume;

    public BaseSoundPool(int capacity) {
        soundSources = Maps.newHashMapWithExpectedSize(capacity);

        this.fillPool(capacity);
    }

    public BaseSoundPool() {
        this(DEFAULT_POOL_SIZE);
    }

    public SOURCE getLockedSource() {
        for (SOURCE source : soundSources.keySet()) {
            if (!isActive(source) && lock(source)) {
                return source;
            }
        }

        return null;
    }

    @Override
    public SOURCE getSource(SOUND sound, int priority) {
        if (sound == null) {
            return null;
        }

        // TODO: should be optimized (performance crucial)
        for (SOURCE source : soundSources.keySet()) {
            if (!isActive(source)) {
                soundSources.put(source, priority);
                return (SOURCE) source.setAudio(sound);
            }
        }

        // No free sound found, will look by priority
        for (Map.Entry<SOURCE, Integer> entry : soundSources.entrySet()) {
            SOURCE source = entry.getKey();
            Integer soundPriority = entry.getValue();

            if (soundPriority < priority) { // sound playing wil lower priority than our query
                soundSources.put(source, priority);
                return (SOURCE) source.setAudio(sound);
            }
        }

        return null;
    }

    @Override
    public SOURCE getSource(SOUND sound) {
        return getSource(sound, AudioManager.PRIORITY_NORMAL);
    }

    @Override
    public Set<SOURCE> getSources() {
        return soundSources.keySet();
    }

    @Override
    public Set<SOURCE> getInactiveSources() {
        return soundSources.keySet().stream().filter(source ->
                !isActive(source)).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Set<SOURCE> getActiveSources() {
        return soundSources.keySet().stream().filter(this::isActive).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public void stopAll() {
        soundSources.keySet().forEach(SOURCE::stop);
    }

    @Override
    public void update(float delta) {
        soundSources.keySet().stream().filter(SoundSource::isPlaying).forEach(source -> source.update(delta));
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        soundSources.keySet().forEach(SOURCE::updateGain);
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
    public boolean isInPool(SOURCE source) {
        return soundSources.containsKey(source);
    }

    public boolean isLocked(SOURCE source) {
        Integer lock = soundSources.get(source);
        return lock != null && lock == AudioManager.PRIORITY_LOCKED;
    }

    public boolean lock(SOURCE source) {
        if (isLocked(source) && !isInPool(source)) {
            return false;
        }

        soundSources.put(source, AudioManager.PRIORITY_LOCKED);

        return true;
    }

    public void unlock(SOURCE source) {
        soundSources.put(source, null);
    }

    public boolean isActive(SOURCE source) {
        return isLocked(source) || source.isPlaying();
    }

    protected abstract SOURCE createSoundSource();

    private void fillPool(int capacity) {
        for (int i = 0; i < capacity; i++) {
            this.soundSources.put(createSoundSource(), null);
        }
    }

    @Override
    public void purge(Sound<?> sound) {
        soundSources.keySet().stream().filter(source -> sound.equals(source.getAudio())).forEach(SOURCE::purge);
    }

}
