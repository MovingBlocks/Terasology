package org.terasology.audio;

import java.util.Set;

public interface SoundPool {

    /**
     * Lock SoundSource for futher usage.
     * Guaranteed locked sound sources won't be used by anyone else,
     * until unlocked.
     *
     * @return
     */
    public SoundSource getLockedSource();

    /**
     * Returns sound source tuned for specified sound with specified priority
     *
     * @param sound
     * @param priority
     * @return
     */
    public SoundSource getSource(Sound sound, int priority);

    /**
     * Returns sound source tuned for specified sound with normal priority
     *
     * @param sound
     * @return
     */
    public SoundSource getSource(Sound sound);

    /**
     * Returns all available sound sources
     * Do not use it for any purpose except sound management
     *
     * @return
     */
    public Set<SoundSource> getSources();

    /**
     * Returns all inactive (available) sources
     *
     * @return
     */
    public Set<SoundSource> getInactiveSources();

    /**
     * Returns all active or locked sources
     *
     * @return
     */
    public Set<SoundSource> getActiveSources();

    /**
     * Returns sound sources amount in this pool
     *
     * @return
     */
    public int size();

    /**
     * Checks if specified sound source is part of this pool
     *
     * @param source
     * @return
     */
    public boolean isInPool(SoundSource source);

    /**
     * Checks if specified sound source is locked
     *
     * @param source
     * @return
     */
    public boolean isLocked(SoundSource source);

    /**
     * Locks specified sound source
     *
     * @param source
     * @return
     */
    public boolean lock(SoundSource source);

    /**
     * Unlocks specified sound source
     *
     * @param source
     */
    public void unlock(SoundSource source);

    /**
     * Stop playback of all sources of this pool
     */
    public void stopAll();

    /**
     * Update sound sources
     *
     * <b>!DO NOT USE IT!</b>
     */
    public void update();

}
