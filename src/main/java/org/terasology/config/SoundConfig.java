package org.terasology.config;

/**
 * @author Immortius
 */
public class SoundConfig {

    private float soundVolume = 1.0f;
    private float musicVolume = 0.1f;
    private boolean disableSound = false;

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public boolean isDisableSound() {
        return disableSound;
    }

    public void setDisableSound(boolean disableSound) {
        this.disableSound = disableSound;
    }
}
