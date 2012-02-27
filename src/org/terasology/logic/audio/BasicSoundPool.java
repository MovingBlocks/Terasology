package org.terasology.logic.audio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicSoundPool implements SoundPool {
    private final static int DEFAULT_POOL_SIZE = 32;

    protected Map<SoundSource, Object> _soundSources;

    public BasicSoundPool(int capacity) {
        _soundSources = new HashMap<SoundSource, Object>(capacity);

        this.fillPool(capacity);
    }

    public BasicSoundPool() {
        this(DEFAULT_POOL_SIZE);
    }

    public SoundSource getLockedSource() {
        for (SoundSource source : _soundSources.keySet()) {
            if (!isActive(source)) {
                if (lock(source)) {
                    return source;
                }
            }
        }

        return null;
    }

    public SoundSource getSource(Sound sound) {
        for (SoundSource source : _soundSources.keySet()) {
            if (!isActive(source)) {
                return source.setAudio(sound);
            }
        }

        return null;
    }

    public Set<SoundSource> getSources() {
        return _soundSources.keySet();
    }

    public Set<SoundSource> getInactiveSources() {
        Set<SoundSource> inactiveSources = new HashSet<SoundSource>();

        for (SoundSource source : _soundSources.keySet()) {
            if (!isActive(source)) {
                inactiveSources.add(source);
            }
        }

        return inactiveSources;
    }

    public Set<SoundSource> getActiveSources() {
        Set<SoundSource> inactiveSources = new HashSet<SoundSource>();

        for (SoundSource source : _soundSources.keySet()) {
            if (isActive(source)) {
                inactiveSources.add(source);
            }
        }

        return inactiveSources;
    }

    public void stopAll() {
        for (SoundSource source : _soundSources.keySet()) {
            source.stop();
        }
    }

    public void update() {
        for (SoundSource source : _soundSources.keySet()) {
            if (source.isPlaying()) {
                source.update();
            }
        }
    }

    public int size() {
        return _soundSources.size();
    }

    public boolean isInPool(SoundSource source) {
        return _soundSources.containsKey(source);
    }

    public boolean isLocked(SoundSource source) {
        return _soundSources.get(source) != null;
    }

    public boolean lock(SoundSource source) {
        if (isLocked(source) && !isInPool(source)) {
            return false;
        }

        _soundSources.put(source, 1);

        return true;
    }

    public void unlock(SoundSource source) {
        _soundSources.put(source, null);
    }

    public boolean isActive(SoundSource source) {
        return isLocked(source) || source.isPlaying();
    }

    protected SoundSource createSoundSource() {
        return new BasicSoundSource();
    }

    private void fillPool(int capacity) {
        for (int i = 0; i < capacity; i++) {
            this._soundSources.put(createSoundSource(), null);
        }
    }
}
