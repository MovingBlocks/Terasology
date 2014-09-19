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

package org.terasology.audio.nullAudio;

import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetUri;
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StaticSoundData;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.StreamingSoundData;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Null implementation of the AudioManager
 *
 * @author Immortius
 */
public class NullAudioManager implements AudioManager {
    @Override
    public boolean isMute() {
        return true;
    }

    @Override
    public void setMute(boolean mute) {
    }

    @Override
    public void playSound(StaticSound sound) {
    }

    @Override
    public void playSound(StaticSound sound, float volume) {
    }

    @Override
    public void playSound(StaticSound sound, float volume, int priority) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position, float volume) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position, float volume, int priority) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position, float volume, int priority, AudioEndListener endListener) {
    }

    @Override
    public void playMusic(StreamingSound sound) {
    }

    @Override
    public void playMusic(StreamingSound sound, float volume) {
    }

    @Override
    public void playMusic(StreamingSound sound, AudioEndListener endListener) {
    }

    @Override
    public void playMusic(StreamingSound sound, float volume, AudioEndListener endListener) {
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void updateListener(Vector3f position, Quat4f orientation, Vector3f velocity) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stopAllSounds() {
    }

    @Override
    public AssetFactory<StaticSoundData, StaticSound> getStaticSoundFactory() {
        return new AssetFactory<StaticSoundData, StaticSound>() {
            @Override
            public StaticSound buildAsset(AssetUri uri, StaticSoundData data) {
                return new NullSound(uri, data);
            }
        };
    }

    @Override
    public AssetFactory<StreamingSoundData, StreamingSound> getStreamingSoundFactory() {
        return new AssetFactory<StreamingSoundData, StreamingSound>() {
            @Override
            public StreamingSound buildAsset(AssetUri uri, StreamingSoundData data) {
                return new NullStreamingSound(uri, data);
            }
        };
    }
}
