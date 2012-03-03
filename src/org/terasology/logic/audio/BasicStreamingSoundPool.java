package org.terasology.logic.audio;


public class BasicStreamingSoundPool extends BasicSoundPool {

    public BasicStreamingSoundPool(int capacity) {
        super(capacity);
    }

    public BasicStreamingSoundPool() {
        super();
    }

    @Override
    protected SoundSource createSoundSource() {
        return new BasicStreamingSoundSource();
    }
}
