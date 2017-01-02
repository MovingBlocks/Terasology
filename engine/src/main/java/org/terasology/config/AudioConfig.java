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

package org.terasology.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * For configuring the audio of the game
 */
public class AudioConfig {
    public static final String SOUND_VOLUME = "soundVolume";
    public static final String MUSIC_VOLUME = "musicVolume";

    private float soundVolume;          // The sound volume
    private float musicVolume;          // The music volume
    private boolean disableSound;       // The boolean property of whether sound is disabled or not

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
     * Returns the current sound volume
     *
     * @return a float representing the current sound volume
     */
    public float getSoundVolume() {
        return soundVolume;
    }
    
    /**
     * Sets the sound volume
     *
     * @param soundVolume   A float representing the desired sound volume
     */
    public void setSoundVolume(float soundVolume) {
        float oldValue = this.soundVolume;
        this.soundVolume = soundVolume;
        propertyChangeSupport.firePropertyChange(SOUND_VOLUME, oldValue, soundVolume);
    }

    /**
     * Returns the current music volume
     *
     * @return a float representing the current music volume
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the music volume
     *
     * @param musicVolume   A float representing the desired music volume
     */
    public void setMusicVolume(float musicVolume) {
        float oldValue = this.musicVolume;
        this.musicVolume = musicVolume;
        propertyChangeSupport.firePropertyChange(MUSIC_VOLUME, oldValue, musicVolume);
    }

    /**
     * Returns a boolean representing whether or not sound is disabled
     *
     * @return a boolean representing whether or not sound is disabled
     */
    public boolean isDisableSound() {
        return disableSound;
    }

    /**
     * Sets the property of sound disabled 
     *
     * @param disableSound  A boolean representing whether sound should or should not be disabled
     */
    public void setDisableSound(boolean disableSound) {
        this.disableSound = disableSound;
    }

    /**
     * Adds a Property Change Listener to the AudioConfig
     *
     * @param changeListener  A PropertyChangeListener to be added
     */
    public void subscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(changeListener);
    }
    
    /**
     * Removes a Property Change Listener to the AudioConfig
     *
     * @param changeListener   A PropertyChangeListener to be removed
     */
    public void unsubscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(changeListener);
    }
}
