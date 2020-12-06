/*
 * Copyright 2014 MovingBlocks
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

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.assets.AssetFactory;

/**
 * Manages the playing of sounds and music as well as muting and updating listeners
 */
public interface AudioManager {
    float MAX_DISTANCE = 100.0f;
    int PRIORITY_LOCKED = Integer.MAX_VALUE;
    int PRIORITY_HIGHEST = 100;
    int PRIORITY_HIGH = 10;
    int PRIORITY_NORMAL = 5;
    int PRIORITY_LOW = 3;
    int PRIORITY_LOWEST = 1;

    /**
     * @return A boolean indicting the mute status
     */
    boolean isMute();

    /**
     * @param mute A boolean indicating the new mute status.
     */
    void setMute(boolean mute);

    void playSound(StaticSound sound);

    void playSound(StaticSound sound, float volume);

    void playSound(StaticSound sound, float volume, int priority);

    void playSound(StaticSound sound, Vector3fc position);

    void playSound(StaticSound sound, Vector3fc position, float volume);

    void playSound(StaticSound sound, Vector3fc position, float volume, int priority);

    /**
     * Plays a sound at an specified point and volume.
     *
     * @param sound The StaticSound to play
     * @param position The position to play the sound at, relative to the listener
     * @param volume The volume
     * @param priority The priority with which this sound should play. Higher values means this sound will be
     *         able to override others.
     * @param endListener The listener to call when the sound is finished
     */
    void playSound(StaticSound sound, Vector3fc position, float volume, int priority, AudioEndListener endListener);

    /**
     * Plays music once, this does not have a direction unlike playSound.
     *
     * @param music The music to play
     */
    void playMusic(StreamingSound music);

    /**
     * Plays music once, this does not have a direction unlike playSound.
     *
     * @param volume The volume to play it at
     */
    void playMusic(StreamingSound music, float volume);

    /**
     * Plays music once, this does not have a direction unlike playSound.
     *
     * @param music The music to play
     * @param endListener The listener to call once the music ends
     */
    void playMusic(StreamingSound music, AudioEndListener endListener);

    /**
     * Plays music once, this does not have a direction unlike playSound.
     *
     * @param music The music to play
     * @param volume The volume to play it at
     * @param endListener The listener to call once the music ends
     */
    void playMusic(StreamingSound music, float volume, AudioEndListener endListener);

    /**
     * Loops music until it gets stopped, this does not have a direction unlike playSound.
     *
     * @param music The music to play
     */
    void loopMusic(StreamingSound music);

    /**
     * Loops music until it gets stopped, this does not have a direction unlike playSound.
     *
     * @param music The music to play
     * @param volume The volume to play it at
     */
    void loopMusic(StreamingSound music, float volume);

    /**
     * Update AudioManager sound sources.
     * <br><br>
     * Should be called in main game loop.
     */
    void update(float delta);

    /**
     * Set the position, orientation and velocity of the listener to updated values.
     *
     * @param position The new position
     * @param orientation The new orientation (in a quaternion)
     * @param velocity The new velocity
     */
    void updateListener(Vector3fc position, Quaternionfc orientation, Vector3fc velocity);

    /**
     * Gracefully destroy audio subsystem.
     */
    void dispose();

    /**
     * Stops all the currently playing sounds.
     */
    void stopAllSounds();

    AssetFactory<StaticSound, StaticSoundData> getStaticSoundFactory();

    AssetFactory<StreamingSound, StreamingSoundData> getStreamingSoundFactory();
}
