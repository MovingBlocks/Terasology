package org.terasology.logic.audio;

import javax.vecmath.Vector3d;

public interface SoundSource {

    public SoundSource play();

    public SoundSource stop();

    public SoundSource pause();

    public boolean isPlaying();

    /**
     * Update method, use it for position update, buffer switching, etc
     */
    public void update();

    /**
     * Returns audio length in milliseconds
     * @return
     */
    public int getLength();

    /**
     * Set playback position in milliseconds
     *
     * @param position
     */
    public SoundSource setPlaybackPosition(int position);
    
    public int getPlaybackPosition();

    /**
     * Set relative playback position (0.0f - start, 1.0f - end)
     *
     * @param position
     */
    public SoundSource setPlaybackPosition(float position);
    public float getPlaybackPositionf();

    public SoundSource setAbsolute(boolean absolute);
    public boolean isAbsolute();

    public SoundSource setPosition(Vector3d pos);
    public Vector3d getPosition();

    public SoundSource setVelocity(Vector3d velocity);
    public Vector3d getVelocity();

    public SoundSource setDirection(Vector3d direction);
    public Vector3d getDirection();
    
    public float getPitch();
    public SoundSource setPitch(float pitch);
    
    public float getGain();
    public SoundSource setGain(float gain);

    public boolean isLooping();
    public SoundSource setLooping(boolean looping);

    public SoundSource setAudio(Sound sound);
    public Sound getAudio();

    public SoundSource fade(float targetGain);

    public SoundSource reset();
}
