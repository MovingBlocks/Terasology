package org.terasology.audio;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Null implementation of the AudioManager
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
    public void playSound(Sound sound) {
    }

    @Override
    public void playSound(Sound sound, float volume) {
    }

    @Override
    public void playSound(Sound sound, float volume, int priority) {
    }

    @Override
    public void playSound(Sound sound, Vector3f position) {
    }

    @Override
    public void playSound(Sound sound, Vector3f position, float volume) {
    }

    @Override
    public void playSound(Sound sound, Vector3f position, float volume, int priority) {
    }

    @Override
    public void playMusic(Sound sound) {
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
}
