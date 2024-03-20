// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.openAL;

import com.google.common.collect.Maps;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioEndListener;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.Sound;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StaticSoundData;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.StreamingSoundData;
import org.terasology.engine.audio.openAL.staticSound.OpenALSound;
import org.terasology.engine.audio.openAL.staticSound.OpenALSoundPool;
import org.terasology.engine.audio.openAL.streamingSound.OpenALStreamingSound;
import org.terasology.engine.audio.openAL.streamingSound.OpenALStreamingSoundPool;
import org.terasology.engine.config.AudioConfig;
import org.terasology.engine.math.Direction;
import org.terasology.gestalt.assets.AssetFactory;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * An Open AL implementation of AudioManager
 */
public class OpenALManager implements AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(OpenALManager.class);

    /**
     * For faster distance check *
     */
    private static final float MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

    protected Map<String, SoundPool<? extends Sound<?>, ?>> pools = Maps.newHashMap();

    private final Vector3f listenerPosition = new Vector3f();

    private final Map<SoundSource<?>, AudioEndListener> endListeners = Maps.newHashMap();

    @SuppressWarnings("PMD.GuardLogStatement")
    public OpenALManager(AudioConfig config) throws OpenALException {
        logger.info("Initializing OpenAL audio manager");

        config.musicVolume.subscribe((setting, oldValue) -> setMusicVolume(setting.get()));
        config.soundVolume.subscribe((setting, oldValue) -> setSoundVolume(setting.get()));

        long device = ALC10.alcOpenDevice((java.lang.CharSequence) null);
        long context = ALC10.alcCreateContext(device, (int[]) null);
        ALC10.alcMakeContextCurrent(context);
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        AL.createCapabilities(alcCapabilities);

        logger.info("OpenAL {} initialized!", AL10.alGetString(AL10.AL_VERSION));
        logger.info("Using OpenAL: {} by {}", AL10.alGetString(AL10.AL_RENDERER), AL10.alGetString(AL10.AL_VENDOR));
        logger.info("Using device: {}", ALC10.alcGetString(device, ALC10.ALC_DEVICE_SPECIFIER));
        logger.info("Available AL extensions: {}", AL10.alGetString(AL10.AL_EXTENSIONS));
        logger.info("Available ALC extensions: {}", ALC10.alcGetString(device, ALC10.ALC_EXTENSIONS));
        logger.info("Max mono sources: {}", ALC10.alcGetInteger(device, ALC11.ALC_MONO_SOURCES));
        logger.info("Max stereo sources: {}", ALC10.alcGetInteger(device, ALC11.ALC_STEREO_SOURCES));
        logger.info("Mixer frequency: {}", ALC10.alcGetInteger(device, ALC10.ALC_FREQUENCY));

        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);

        // Initialize sound pools
        pools.put("sfx", new OpenALSoundPool(30)); // effects pool
        pools.put("music", new OpenALStreamingSoundPool(2)); // music pool
        pools.get("sfx").setVolume(config.soundVolume.get());
        pools.get("music").setVolume(config.musicVolume.get());
    }

    @Override
    public void dispose() {
        ALC10.alcCloseDevice(ALC10.alcGetContextsDevice(ALC10.alcGetCurrentContext()));
        ALC.destroy();
    }

    @Override
    public void stopAllSounds() {
        pools.values().forEach(SoundPool::stopAll);
        notifyEndListeners(true);
    }

    @Override
    public boolean isMute() {
        return AL10.alGetListenerf(AL10.AL_GAIN) < 0.01f;
    }

    @Override
    public void setMute(boolean mute) {
        if (mute) {
            AL10.alListenerf(AL10.AL_GAIN, 0);
        } else {
            AL10.alListenerf(AL10.AL_GAIN, 1.0f);
        }
    }

    private void setSoundVolume(float volume) {
        pools.get("sfx").setVolume(volume);
    }

    private void setMusicVolume(float volume) {
        pools.get("music").setVolume(volume);
    }

    @Override
    public void playSound(StaticSound sound) {
        playSound(sound, (Vector3fc) null, 1.0f, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, float volume) {
        playSound(sound, (Vector3fc) null, volume, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, float volume, int priority) {
        playSound(sound, (Vector3fc) null, volume, priority);
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position) {
        playSound(sound, position, 1.0f, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position, float volume) {
        playSound(sound, position, volume, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position, float volume, int priority) {
        playSound(sound, position, volume, priority, null);
    }

    @Override
    public void playSound(StaticSound sound, Vector3fc position, float volume, int priority,
                          AudioEndListener endListener) {
        if (position != null && !checkDistance(position)) {
            return;
        }
        SoundPool<StaticSound, ?> pool = (SoundPool<StaticSound, ?>) pools.get("sfx");

        SoundSource<?> source = pool.getSource(sound, priority);
        if (source != null) {
            source.setAbsolute(position != null);
            if (position != null) {
                source.setPosition(position);
            }
            source.setGain(volume);
            source.play();

            if (endListener != null) {
                endListeners.put(source, endListener);
            }
        }
    }

    @Override
    public void playMusic(StreamingSound music) {
        playMusic(music, 1.0f, null);
    }

    @Override
    public void playMusic(StreamingSound music, AudioEndListener endListener) {
        playMusic(music, 1.0f, endListener);
    }

    @Override
    public void playMusic(StreamingSound music, float volume) {
        playMusic(music, volume, null);
    }

    @Override
    public void playMusic(StreamingSound music, float volume, AudioEndListener endListener) {
        SoundPool<StreamingSound, ?> pool = (SoundPool<StreamingSound, ?>) pools.get("music");

        pool.stopAll();

        if (music == null) {
            return;
        }

        SoundSource<?> source = pool.getSource(music);
        if (source != null) {
            source.setGain(volume).play();

            if (endListener != null) {
                endListeners.put(source, endListener);
            }
        }
    }

    @Override
    public void loopMusic(StreamingSound music) {
        loopMusic(music, 1.0f);
    }

    @Override
    public void loopMusic(StreamingSound music, float volume) {
        AudioEndListener loopingEndListener = interrupted -> {
            if (!interrupted) {
                loopMusic(music, volume);
            }
        };
        playMusic(music, volume, loopingEndListener);
    }

    @Override
    public void updateListener(Vector3fc position, Quaternionfc orientation, Vector3fc velocity) {
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x(), velocity.y(), velocity.z());

        OpenALException.checkState("Setting listener velocity");

        Vector3f dir = new Vector3f(Direction.FORWARD.asVector3f())
                .rotate(orientation);
        Vector3f up = new Vector3f(Direction.UP.asVector3f())
                .rotate(orientation);

        FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6)
                .put(new float[]{dir.x, dir.y, dir.z, up.x, up.y, up.z});
        listenerOri.flip();
        AL10.alListenerfv(AL10.AL_ORIENTATION, listenerOri);
        OpenALException.checkState("Setting listener orientation");

        listenerPosition.set(position);
        AL10.alListener3f(AL10.AL_POSITION, position.x(), position.y(), position.z());
        OpenALException.checkState("Setting listener position");
    }

    @Override
    public void update(float delta) {
        for (SoundPool<?, ?> pool : pools.values()) {
            pool.update(delta);
        }
        notifyEndListeners(false);
    }

    private void notifyEndListeners(boolean interrupted) {
        Iterator<Map.Entry<SoundSource<?>, AudioEndListener>> iterator = endListeners.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SoundSource<?>, AudioEndListener> entry = iterator.next();
            if (!entry.getKey().isPlaying()) {
                iterator.remove();

                try {
                    entry.getValue().onAudioEnd(interrupted);
                } catch (Exception e) {
                    logger.error("onAudioEnd() notification failed for {}", entry.getValue(), e); //NOPMD
                }
            }
        }
    }

    protected boolean checkDistance(Vector3fc soundPosition) {
        Vector3f distance = new Vector3f(soundPosition)
                .sub(listenerPosition);

        return distance.lengthSquared() < MAX_DISTANCE_SQUARED;
    }

    @Override
    public AssetFactory<StaticSound, StaticSoundData> getStaticSoundFactory() {
        return (urn, assetType, data) -> new OpenALSound(urn, assetType, data,
                OpenALManager.this, new OpenALSound.DisposalAction(urn));
    }

    @Override
    public AssetFactory<StreamingSound, StreamingSoundData> getStreamingSoundFactory() {
        return (urn, assetType, data) -> new OpenALStreamingSound(urn, assetType, data,
                OpenALManager.this, new OpenALStreamingSound.DisposalAction(urn));
    }

    public void purgeSound(Sound<?> sound) {
        for (SoundPool<?, ?> pool : pools.values()) {
            pool.purge(sound);
        }
    }
}
