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
 */
public class AudioConfig {
    public static final String SOUND_VOLUME = "soundVolume";
    public static final String MUSIC_VOLUME = "musicVolume";

    private float soundVolume;
    private float musicVolume;
    private boolean disableSound;

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * @return Returns a float of the sound volume
     */
    public float getSoundVolume() {
        return soundVolume;
    }

    /**
     * @param soundVolume Sets the sound volume starting from the previous value and going to the soundVolume passed in
     */
    public void setSoundVolume(float soundVolume) {
        float oldValue = this.soundVolume;
        this.soundVolume = soundVolume;
        propertyChangeSupport.firePropertyChange(SOUND_VOLUME, oldValue, soundVolume);
    }

    /**
     * @return Returns music volume
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * @param musicVolume Sets music volume using the same principle as setSoundVolume()
     */
    public void setMusicVolume(float musicVolume) {
        float oldValue = this.musicVolume;
        this.musicVolume = musicVolume;
        propertyChangeSupport.firePropertyChange(MUSIC_VOLUME, oldValue, musicVolume);
    }

    /**
     * @return A boolean indicating if sound is disabled
     */
    public boolean isDisableSound() {
        return disableSound;
    }

    /**
     * @param disableSound Sets either true or false if sound is disabled or not
     */
    public void setDisableSound(boolean disableSound) {
        this.disableSound = disableSound;
    }

    /**
     * @param changeListener The PropertyChangeListener to subscribe to
     */
    public void subscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(changeListener);
    }

    /**
     * @param changeListener The PropertyChangeListener to unsusbcribe from
     */
    public void unsubscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(changeListener);
    }
}