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
package org.terasology.audio.openAL;

import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCcontext;
import org.lwjgl.openal.ALCdevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetFactory;
import org.terasology.audio.AudioEndListener;
import org.terasology.audio.AudioManager;
import org.terasology.audio.Sound;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StaticSoundData;
import org.terasology.audio.StreamingSound;
import org.terasology.audio.StreamingSoundData;
import org.terasology.audio.openAL.staticSound.OpenALSound;
import org.terasology.audio.openAL.staticSound.OpenALSoundPool;
import org.terasology.audio.openAL.streamingSound.OpenALStreamingSound;
import org.terasology.audio.openAL.streamingSound.OpenALStreamingSoundPool;
import org.terasology.config.AudioConfig;
import org.terasology.math.Direction;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.beans.PropertyChangeListener;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.Map;

public class OpenALManager implements AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(OpenALManager.class);

    /**
     * For faster distance check *
     */
    private static final float MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

    protected Map<String, SoundPool<? extends Sound<?>, ?>> pools = Maps.newHashMap();

    private Vector3f listenerPosition = new Vector3f();

    private Map<SoundSource<?>, AudioEndListener> endListeners = Maps.newHashMap();

    private PropertyChangeListener configListener = evt -> {
        if (evt.getPropertyName().equals(AudioConfig.MUSIC_VOLUME)) {
            setMusicVolume((Float) evt.getNewValue());
        } else if (evt.getPropertyName().equals(AudioConfig.SOUND_VOLUME)) {
            setSoundVolume((Float) evt.getNewValue());
        }
    };

    public OpenALManager(AudioConfig config) throws OpenALException, LWJGLException {
        logger.info("Initializing OpenAL audio manager");
        config.subscribe(configListener);

        AL.create();

        AL10.alGetError();

        logger.info("OpenAL {} initialized!", AL10.alGetString(AL10.AL_VERSION));

        ALCcontext context = ALC10.alcGetCurrentContext();
        ALCdevice device = ALC10.alcGetContextsDevice(context);

        logger.info("Using OpenAL: {} by {}", AL10.alGetString(AL10.AL_RENDERER), AL10.alGetString(AL10.AL_VENDOR));
        logger.info("Using device: {}", ALC10.alcGetString(device, ALC10.ALC_DEVICE_SPECIFIER));
        logger.info("Available AL extensions: {}", AL10.alGetString(AL10.AL_EXTENSIONS));
        logger.info("Available ALC extensions: {}", ALC10.alcGetString(device, ALC10.ALC_EXTENSIONS));

        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        ALC10.alcGetInteger(device, ALC11.ALC_MONO_SOURCES, buffer);
        logger.info("Max mono sources: {}", buffer.get(0));
        buffer.rewind();

        ALC10.alcGetInteger(device, ALC11.ALC_STEREO_SOURCES, buffer);
        logger.info("Max stereo sources: {}", buffer.get(0));
        buffer.rewind();

        ALC10.alcGetInteger(device, ALC10.ALC_FREQUENCY, buffer);
        logger.info("Mixer frequency: {}", buffer.get(0));
        buffer.rewind();

        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE_CLAMPED);

        // Initialize sound pools
        pools.put("sfx", new OpenALSoundPool(30)); // effects pool
        pools.put("music", new OpenALStreamingSoundPool(2)); // music pool
        pools.get("sfx").setVolume(config.getSoundVolume());
        pools.get("music").setVolume(config.getMusicVolume());
    }

    @Override
    public void dispose() {
        AL.destroy();
    }


    @Override
    public void stopAllSounds() {
        pools.values().forEach(SoundPool::stopAll);
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
        playSound(sound, null, 1.0f, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, float volume) {
        playSound(sound, null, volume, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, float volume, int priority) {
        playSound(sound, null, volume, priority);
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position) {
        playSound(sound, position, 1.0f, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position, float volume) {
        playSound(sound, position, volume, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position, float volume, int priority) {
        playSound(sound, position, volume, priority, null);
    }

    @Override
    public void playSound(StaticSound sound, Vector3f position, float volume, int priority, AudioEndListener endListener) {
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
    public void updateListener(Vector3f position, Quat4f orientation, Vector3f velocity) {

        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);

        OpenALException.checkState("Setting listener velocity");

        Vector3f dir = orientation.rotate(Direction.FORWARD.getVector3f(), new Vector3f());
        Vector3f up = orientation.rotate(Direction.UP.getVector3f(), new Vector3f());

        FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float[]{dir.x, dir.y, dir.z, up.x, up.y, up.z});
        listenerOri.flip();
        AL10.alListener(AL10.AL_ORIENTATION, listenerOri);

        OpenALException.checkState("Setting listener orientation");
        this.listenerPosition.set(position);

        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);

        OpenALException.checkState("Setting listener position");
    }

    @Override
    public void update(float delta) {
        for (SoundPool<?, ?> pool : pools.values()) {
            pool.update(delta);
        }
        Iterator<Map.Entry<SoundSource<?>, AudioEndListener>> iterator = endListeners.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SoundSource<?>, AudioEndListener> entry = iterator.next();
            if (!entry.getKey().isPlaying()) {
                iterator.remove();

                try {
                    entry.getValue().onAudioEnd();
                } catch (Exception e) {
                    logger.error("onAudioEnd() notification failed for {}", entry.getValue(), e);
                }
            }
        }
    }

    protected boolean checkDistance(Vector3f soundPosition) {
        Vector3f distance = new Vector3f(soundPosition);
        distance.sub(listenerPosition);

        return distance.lengthSquared() < MAX_DISTANCE_SQUARED;
    }

    @Override
    public AssetFactory<StaticSound, StaticSoundData> getStaticSoundFactory() {
        return (urn, assetType, data) -> new OpenALSound(urn, assetType, data, OpenALManager.this);
    }

    @Override
    public AssetFactory<StreamingSound, StreamingSoundData> getStreamingSoundFactory() {
        return (urn, assetType, data) -> new OpenALStreamingSound(urn, assetType, data, OpenALManager.this);
    }

    public void purgeSound(Sound<?> sound) {
        for (SoundPool<?, ?> pool : pools.values()) {
            pool.purge(sound);
        }
    }
}
