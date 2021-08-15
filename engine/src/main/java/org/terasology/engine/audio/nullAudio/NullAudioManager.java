// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.nullAudio;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.terasology.engine.audio.AudioEndListener;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StaticSoundData;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.StreamingSoundData;
import org.terasology.gestalt.assets.AssetFactory;

/**
 * Null implementation of the AudioManager
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
    public void playSound(StaticSound sound, Vector3fc position) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position, float volume) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position, float volume, int priority) {
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position, float volume, int priority,
                          AudioEndListener endListener) {
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
    public void updateListener(Vector3fc position, Quaternionfc orientation, Vector3fc velocity) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stopAllSounds() {
    }

    @Override
    public AssetFactory<StaticSound, StaticSoundData> getStaticSoundFactory() {
        return NullSound::new;
    }

    @Override
    public AssetFactory<StreamingSound, StreamingSoundData> getStreamingSoundFactory() {
        return NullStreamingSound::new;
    }

    @Override
    public void loopMusic(StreamingSound music) {
    }

    @Override
    public void loopMusic(StreamingSound music, float volume) {
    }
}
