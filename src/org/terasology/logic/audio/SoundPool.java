package org.terasology.logic.audio;

import java.util.Set;

public interface SoundPool {

    public SoundSource getLockedSource();

    public SoundSource getSource(Sound sound);

    public Set<SoundSource> getSources();

    public Set<SoundSource> getInactiveSources();

    public Set<SoundSource> getActiveSources();

    public int size();

    public boolean isInPool(SoundSource source);

    public boolean isLocked(SoundSource source);



    public boolean lock(SoundSource source);

    public void unlock(SoundSource source);

    public void stopAll();

    public void update();

}
