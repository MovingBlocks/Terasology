/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.manager;

import org.terasology.logic.audio.OpenALManager;
import org.terasology.logic.audio.Sound;
import org.terasology.logic.audio.SoundPool;
import org.terasology.logic.audio.SoundSource;
import org.terasology.logic.entities.Entity;
import org.terasology.logic.entities.MovableEntity;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simple managing class for loading and accessing audio files.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author t3hk0d3 <contact@tehkode.ru>
 */
public abstract class AudioManager {
    public final static int PRIORITY_HIGHEST = 100;
    public final static int PRIORITY_HIGH = 10;
    public final static int PRIORITY_NORMAL = 5;
    public final static int PRIORITY_LOW = 0;

    protected Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    private static AudioManager _instance = null;

    protected Map<String, Sound> _audio = new HashMap<String, Sound>();

    protected Map<String, SoundPool> _pools = new HashMap<String, SoundPool>();


    protected AudioManager() {
    }

    /**
     * Return an audio file, loading it from disk if it isn't in the cache yet
     *
     * @param name The name of the audio file
     * @return The loaded audio file
     */
    public Sound getSound(String name) {
        Sound sound = _audio.get(name.toLowerCase());

        if (sound == null) {
            try {
                logger.info("Loading sound 'sounds/" + name + ".ogg'");
                sound = this.createAudio(name, getSoundAssetStream("sounds/" + name + ".ogg"));
                _audio.put(name.toLowerCase(), sound);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load sound " + name + " - " + e.getMessage(), e);
            }
        }

        return sound;
    }

    /**
     * Loads the music file with the given name.
     *
     * @param name The name of the music file
     * @return The loaded music file
     */
    public Sound getMusic(String name) {
        Sound sound = _audio.get(name.toLowerCase());

        if (sound == null) {
            try {
                logger.info("Loading sound 'music/" + name + ".ogg'");
                sound = this.createStreamingAudio(name, getSoundAssetStream("music/" + name + ".ogg"));
                _audio.put(name.toLowerCase(), sound);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load sound " + name + " - " + e.getMessage(), e);
            }
        }

        return sound;
    }

    public abstract void initialize();

    public abstract void update();

    public abstract void destroy();

    protected abstract Sound createAudio(String name, InputStream stream);

    protected abstract Sound createStreamingAudio(String name, InputStream stream);

    public SoundPool getSoundPool(String pool) {
        SoundPool soundPool = this._pools.get(pool);

        if (soundPool == null) {
            throw new IllegalArgumentException("Unknown pool '" + pool + "', typo? Available pools: " + _pools.keySet());
        }

        return soundPool;
    }

    private InputStream getSoundAssetStream(String fileName) throws IOException {
        return AssetManager.assetStream(fileName);
    }

    protected void loadAssets() {
        this.loadSoundAssets();
        this.loadMusicAssets();
    }

    private void loadSoundAssets() {
        for (String sound : AssetManager.list("sounds")) {
            logger.info("Loading sound " + sound);
            try {
                String name = sound.substring(sound.lastIndexOf('/') + 1);

                if (!name.endsWith(".ogg")) {
                    continue;
                }

                name = name.substring(0, name.lastIndexOf("."));

                this._audio.put(name.toLowerCase(), createAudio(name, getSoundAssetStream(sound)));
            } catch (IOException e) {
                logger.info("Failed to load sound asset '" + sound + "'");
            }
        }
    }

    private void loadMusicAssets() {
        for (String sound : AssetManager.list("music")) {
            logger.info("Loading music " + sound);
            try {
                String name = sound.substring(sound.lastIndexOf('/') + 1);

                if (!name.endsWith(".ogg")) {
                    continue;
                }


                name = name.substring(0, name.lastIndexOf("."));
                this._audio.put(name.toLowerCase(), createStreamingAudio(name, getSoundAssetStream(sound)));
            } catch (IOException e) {
                logger.info("Failed to load music asset '" + sound + "'");
            }
        }
    }

    public SoundSource getSoundSource(String pool, String sound) {
        return getSoundSource(pool, getSound(sound));
    }

    public SoundSource getSoundSource(String pool, Sound sound) {
        return getSoundPool(pool).getSource(sound);
    }

    /**
     * Stops all playback.
     */
    public void stopAllSounds() {
        for (SoundPool pool : _pools.values()) {
            pool.stopAll();
        }
    }

    public static AudioManager getInstance() {
        if (_instance == null) {
            _instance = new OpenALManager();
        }

        return _instance;
    }

    public static Sound sound(String name) {
        return getInstance().getSound(name);
    }

    public static Sound[] sounds(String... names) {
        Sound[] sounds = new Sound[names.length];

        int i = 0;
        for (String name : names) {
            sounds[i++] = getInstance().getSound(name);
        }

        return sounds;
    }

    public static SoundSource source(String name) {
        return getInstance().getSoundSource("sfx", name);
    }

    public static SoundSource source(Sound sound) {
        return getInstance().getSoundSource("sfx", sound);
    }

    public static SoundSource source(String name, Vector3d pos, float gain) {
        SoundSource source = source(name);
        if (source == null) {
            return null;
        }

        return (pos != null ? source.setPosition(pos).setAbsolute(true) : source).setGain(gain);
    }

    public static SoundSource source(Sound sound, Vector3d pos, float gain) {
        SoundSource source = source(sound);

        if (source == null) {
            return null;
        }

        return (pos != null ? source.setPosition(pos).setAbsolute(true) : source).setGain(gain);
    }

    public static SoundSource play(String name) {
        return play(name, null, 1.0f);
    }

    public static SoundSource play(String name, float gain) {
        return play(name, null, gain);
    }

    public static SoundSource play(String name, Vector3d pos, float gain) {
        SoundSource source = source(name);

        if (source == null) {
            return null;
        }

        return source.play();
    }

    public static SoundSource play(Sound sound, Vector3d pos, float gain) {
        SoundSource source = source(sound, pos, gain);

        if (source == null) {
            return null;
        }

        return source.play();
    }

    public static SoundSource play(Sound sound, Entity entity, float gain){
        SoundSource source = source(sound, entity.getPosition(), gain);

        if (source == null) {
            // Nof free sound sources
            return null;
        }

        return source.play();
    }

    public static SoundSource play(Sound sound, MovableEntity entity, float gain) {
        SoundSource source = source(sound, entity.getPosition(), gain);

        if (source == null) {
            // Nof free sound sources
            return null;
        }

        return source.setVelocity(entity.getVelocity()).setDirection(entity.getViewingDirection()).play();
    }
    
    public static SoundSource playMusic(String name) {
        SoundPool pool = AudioManager.getInstance().getSoundPool("music");
        SoundSource source = pool.getSource(AudioManager.sound(name));

        if(source == null) { // no free music slots
            return null;
        }

        return source.setGain(0.1f).play();
    }
}
