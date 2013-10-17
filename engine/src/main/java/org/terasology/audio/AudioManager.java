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
package org.terasology.audio;

import org.terasology.asset.AssetFactory;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface AudioManager {
    float MAX_DISTANCE = 100.0f;
    int PRIORITY_LOCKED = Integer.MAX_VALUE;
    int PRIORITY_HIGHEST = 100;
    int PRIORITY_HIGH = 10;
    int PRIORITY_NORMAL = 5;
    int PRIORITY_LOW = 3;
    int PRIORITY_LOWEST = 1;

    boolean isMute();

    void setMute(boolean mute);

    void playSound(Sound sound);

    void playSound(Sound sound, float volume);

    void playSound(Sound sound, float volume, int priority);

    void playSound(Sound sound, Vector3f position);

    void playSound(Sound sound, Vector3f position, float volume);

    void playSound(Sound sound, Vector3f position, float volume, int priority);

    void playMusic(Sound sound);

    /**
     * Update AudioManager sound sources
     * <p/>
     * Should be called in main game loop
     */
    void update(float delta);

    void updateListener(Vector3f position, Quat4f orientation, Vector3f velocity);

    /**
     * Gracefully destroy audio subsystem
     */
    void dispose();

    void stopAllSounds();

    AssetFactory<StaticSoundData, StaticSound> getStaticSoundFactory();

    AssetFactory<StreamingSoundData, StreamingSound> getStreamingSoundFactory();
}
