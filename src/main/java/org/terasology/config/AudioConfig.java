/*
 * Copyright 2013 Benjamin Glatzel <benjamin.glatzel@me.com>
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
 * @author Immortius
 */
public class AudioConfig {
    public final static String SOUND_VOLUME = "soundVolume";
    public final static String MUSIC_VOLUME = "musicVolume";

    private float soundVolume = 1.0f;
    private float musicVolume = 0.1f;
    private boolean disableSound = false;

    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        float oldValue = this.soundVolume;
        this.soundVolume = soundVolume;
        propertyChangeSupport.firePropertyChange(SOUND_VOLUME, oldValue, soundVolume);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        float oldValue = this.musicVolume;
        this.musicVolume = musicVolume;
        propertyChangeSupport.firePropertyChange(MUSIC_VOLUME, oldValue, musicVolume);
    }

    public boolean isDisableSound() {
        return disableSound;
    }

    public void setDisableSound(boolean disableSound) {
        this.disableSound = disableSound;
    }

    public void subscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(changeListener);
    }

    public void unsubscribe(PropertyChangeListener changeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(changeListener);
    }
}
