package org.terasology.logic.manager;

import org.terasology.audio.Sound;
import org.terasology.audio.SoundPool;
import org.terasology.audio.SoundSource;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Rename this to AudioManager, and AudioManager to AudioManagerAbstract or something
public interface SoundManager {
    float MAX_DISTANCE = 50.0f;
    int PRIORITY_LOCKED = Integer.MAX_VALUE;
    int PRIORITY_HIGHEST = 100;
    int PRIORITY_HIGH = 10;
    int PRIORITY_NORMAL = 5;
    int PRIORITY_LOW = 3;
    int PRIORITY_LOWEST = 1;

    Sound getSound(String name);

    Sound getMusic(String name);

    /**
     * Initializes AudioManager
     */
    void initialize();

    /**
     * Update AudioManager sound sources
     * <p/>
     * Should be called in main game loop
     */
    void update();

    /**
     * Gracefully destroy audio subsystem
     */
    void destroy();

    SoundPool getSoundPool(String pool);

    SoundSource getSoundSource(String pool, String sound, int priority);

    SoundSource getSoundSource(String pool, Sound sound, int priority);

    void stopAllSounds();
}
