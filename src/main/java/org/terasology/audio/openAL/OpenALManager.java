/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.bulletphysics.linearmath.QuaternionUtil;
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
import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.audio.AudioManager;
import org.terasology.config.AudioConfig;
import org.terasology.math.Direction;
import org.terasology.utilities.OggReader;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.AL10.alGenBuffers;

public class OpenALManager implements AudioManager {

    private static final Logger logger = LoggerFactory.getLogger(OpenALManager.class);

    /**
     * For faster distance check *
     */
    private final static float MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

    protected Map<String, SoundPool> pools = new HashMap<String, SoundPool>();

    private Vector3f listenerPosition = new Vector3f();

    private PropertyChangeListener configListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(AudioConfig.MUSIC_VOLUME)) {
                setMusicVolume((Float)evt.getNewValue());
            } else if (evt.getPropertyName().equals(AudioConfig.SOUND_VOLUME)) {
                setSoundVolume((Float)evt.getNewValue());
            }
        }
    };

    public OpenALManager(AudioConfig config) {
        logger.info("Initializing OpenAL audio manager");
        config.subscribe(configListener);
        try {
            AL.create();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

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
        pools.put("sfx", new BasicSoundPool(30)); // effects pool
        pools.put("music", new BasicStreamingSoundPool(2)); // music pool
        pools.get("sfx").setVolume(config.getSoundVolume());
        pools.get("music").setVolume(config.getMusicVolume());
    }

    @Override
    public void dispose() {
        AL.destroy();
    }

    @Override
    public void stopAllSounds() {
        for (SoundPool pool : pools.values()) {
            pool.stopAll();
        }
    }

    @Override
    public Sound loadStreamingSound(AssetUri uri, List<URL> urls) {
        return new OggStreamingSound(uri, urls.get(0));
    }

    @Override
    public Sound loadSound(AssetUri uri, InputStream stream) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OggReader reader = new OggReader(stream);

            byte buffer[] = new byte[1024];
            int read;
            int totalRead = 0;

            do {
                read = reader.read(buffer, 0, buffer.length);

                if (read < 0) {
                    break;
                }

                totalRead += read;

                bos.write(buffer, 0, read);
            } while (read > 0);

            buffer = bos.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(totalRead).put(buffer);
            data.flip();

            int channels = reader.getChannels();
            int sampleRate = reader.getRate();
            int bufferId = alGenBuffers();
            AL10.alBufferData(bufferId, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data, sampleRate);

            OpenALException.checkState("Uploading buffer");
            return new OggSound(uri, bufferId);
        } catch (IOException e) {
            throw new IOException("Failed to load sound: " + e.getMessage(), e);
        }
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
    public void playSound(Sound sound) {
        playSound(sound, null, 1.0f, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(Sound sound, float volume) {
        playSound(sound, null, volume, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(Sound sound, float volume, int priority) {
        playSound(sound, null, volume, priority);
    }

    @Override
    public void playSound(Sound sound, Vector3f position) {
        playSound(sound, position, 1.0f, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(Sound sound, Vector3f position, float volume) {
        playSound(sound, position, volume, PRIORITY_NORMAL);
    }

    @Override
    public void playSound(Sound sound, Vector3f position, float volume, int priority) {
        if (position != null && !checkDistance(position)) {
            return;
        }
        SoundSource source = pools.get("sfx").getSource(sound, priority);
        if (source != null) {
            source.setAbsolute(position != null);
            if (position != null) {
                source.setPosition(position);
            }
            source.setGain(volume);
            source.play();
        }
    }

    @Override
    public void playMusic(Sound sound) {
        SoundPool pool = pools.get("music");

        pool.stopAll();

        if (sound == null) {
            return;
        }

        SoundSource source = pool.getSource(sound);
        if (source != null) {
            source.setGain(1.0f).play();
        }
    }

    @Override
    public void updateListener(Vector3f position, Quat4f orientation, Vector3f velocity) {

        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);

        OpenALException.checkState("Setting listener velocity");

        Vector3f dir = QuaternionUtil.quatRotate(orientation, Direction.FORWARD.getVector3f(), new Vector3f());
        Vector3f up = QuaternionUtil.quatRotate(orientation, Direction.UP.getVector3f(), new Vector3f());

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
        for (SoundPool pool : pools.values()) {
            pool.update(delta);
        }
    }

    protected boolean checkDistance(Vector3f soundPosition) {
        Vector3f distance = new Vector3f(soundPosition);
        distance.sub(listenerPosition);

        return distance.lengthSquared() < MAX_DISTANCE_SQUARED;
    }
}
