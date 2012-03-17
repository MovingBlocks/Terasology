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

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.audio.OpenALManager;
import org.terasology.audio.Sound;
import org.terasology.audio.SoundPool;
import org.terasology.audio.SoundSource;
import org.terasology.components.BlockComponent;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.structures.BlockPosition;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.net.URL;
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

    public final static float MAX_DISTANCE = 50.0f;

    public final static int PRIORITY_LOCKED = Integer.MAX_VALUE;
    public final static int PRIORITY_HIGHEST = 100;
    public final static int PRIORITY_HIGH = 10;
    public final static int PRIORITY_NORMAL = 5;
    public final static int PRIORITY_LOW = 3;
    public final static int PRIORITY_LOWEST = 1;

    protected Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    private static AudioManager _instance = null;

    protected Map<String, Sound> _audio = new HashMap<String, Sound>();

    protected Map<String, SoundPool> _pools = new HashMap<String, SoundPool>();


    protected AudioManager() {
    }

    protected abstract Sound createAudio(String name, URL source);

    protected abstract Sound createStreamingAudio(String name, URL source);

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

                this._audio.put(name.toLowerCase(), createAudio(name, getSoundAsset(sound)));
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
                this._audio.put(name.toLowerCase(), createStreamingAudio(name, getSoundAsset(sound)));
            } catch (IOException e) {
                logger.info("Failed to load music asset '" + sound + "'");
            }
        }
    }

    private URL getSoundAsset(String fileName) throws IOException {
        return AssetManager.asset(fileName);
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
                sound = this.createAudio(name, getSoundAsset("sounds/" + name + ".ogg"));
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
                sound = this.createStreamingAudio(name, getSoundAsset("music/" + name + ".ogg"));
                _audio.put(name.toLowerCase(), sound);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load sound " + name + " - " + e.getMessage(), e);
            }
        }

        return sound;
    }

    /**
     * Initializes AudioManager
     */
    public abstract void initialize();

    /**
     * Update AudioManager sound sources
     * <p/>
     * Should be called in main game loop
     */
    public abstract void update();

    /**
     * Gracefully destroy audio subsystem
     */
    public abstract void destroy();

    protected abstract boolean checkDistance(Vector3d soundSource);


    /**
     * Returns sound pool with specified name
     * <b>WARNING! Method will throw IllegalArgumentException if specified sound pool is not found</b>
     *
     * @param pool Sound pool name
     * @return Sound pool object
     */
    public SoundPool getSoundPool(String pool) {
        SoundPool soundPool = this._pools.get(pool);

        if (soundPool == null) {
            throw new IllegalArgumentException("Unknown pool '" + pool + "', typo? Available pools: " + _pools.keySet());
        }

        return soundPool;
    }


    public SoundSource getSoundSource(String pool, String sound, int priority) {
        return getSoundSource(pool, getSound(sound), priority);
    }

    public SoundSource getSoundSource(String pool, Sound sound, int priority) {
        return getSoundPool(pool).getSource(sound, priority);
    }

    /**
     * Stops all playback.
     */
    public void stopAllSounds() {
        for (SoundPool pool : _pools.values()) {
            pool.stopAll();
        }
    }

    /**
     * Returns AudioManager instance
     *
     * @return
     */
    public static AudioManager getInstance() {
        if (_instance == null) {
            _instance = new OpenALManager();
        }

        return _instance;
    }

    /**
     * Returns sound with specified name
     * Method will return null if sound is not found
     *
     * @param name
     * @return
     */
    public static Sound sound(String name) {
        return getInstance().getSound(name);
    }

    /**
     * Returns sounds with specified names
     *
     * @param names
     * @return
     */
    public static Sound[] sounds(String... names) {
        Sound[] sounds = new Sound[names.length];

        int i = 0;
        for (String name : names) {
            sounds[i++] = getInstance().getSound(name);
        }

        return sounds;
    }

    /**
     * Returns sound source for specified sound from "sfx" pool with normal priority
     *
     * @param name Sound name
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(String name) {
        return source(name, PRIORITY_NORMAL);
    }

    /**
     * Returns sound source for specified sound from "sfx" pool
     *
     * @param name
     * @param priority Priority
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(String name, int priority) {
        return getInstance().getSoundSource("sfx", name, priority);
    }

    /**
     * Returns sound source for specified sound from "sfx" pool with normal priority
     *
     * @param sound Sound object
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(Sound sound) {
        return source(sound, PRIORITY_NORMAL);
    }

    /**
     * Returns sound source for specified sound from "sfx" pool with normal priority
     *
     * @param sound    Sound object
     * @param priority Sound priority
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(Sound sound, int priority) {
        return getInstance().getSoundSource("sfx", sound, priority);
    }

    /**
     * Returns sound source from "sfx" pool configured for specified sound, position and gain
     *
     * @param name Sound name
     * @param pos  Sound source position
     * @param gain Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(String name, Vector3d pos, float gain, int priority) {
        SoundSource source = source(name, priority);
        if (source == null) {
            return null;
        }

        return (pos != null ? source.setPosition(pos).setAbsolute(true) : source).setGain(gain);
    }

    /**
     * Returns sound source from "sfx" pool configured for specified sound, position and gain
     *
     * @param sound Sound object
     * @param pos   Sound source position
     * @param gain  Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(Sound sound, Vector3d pos, float gain, int priority) {
        SoundSource source = source(sound, priority);

        if (source == null) {
            return null;
        }

        if (pos != null) {
            if (!getInstance().checkDistance(pos)) {
                return null;
            }

            source.setPosition(pos).setAbsolute(true);
        }

        return source.setGain(gain);
    }

    /**
     * Plays specified sound with gain = 1.0f
     *
     * @param name Sound name
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(String name) {
        return play(name, null, 1.0f, PRIORITY_NORMAL);
    }

    /**
     * Plays specified sound with specified gain
     *
     * @param name Sound name
     * @param gain Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(String name, float gain) {
        return play(name, null, gain, PRIORITY_NORMAL);
    }

    /**
     * Plays specified sound at specified position and with specified gain
     *
     * @param name Sound name
     * @param pos  Sound source position
     * @param gain Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(String name, Vector3d pos, float gain, int priority) {
        SoundSource source = source(name, pos, gain, priority);

        if (source == null) {
            return null;
        }

        return source.play();
    }

    /**
     * Plays specified sound at specified position and with specified gain
     *
     * @param sound Sound object
     * @param pos   Sound source position
     * @param gain  Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(Sound sound, Vector3d pos, float gain, int priority) {
        SoundSource source = source(sound, pos, gain, priority);

        if (source == null) {
            return null;
        }

        return source.setGain(gain).play();
    }

    /**
     * Plays specified sound at specified position and with specified gain
     *
     * @param sound Sound object
     * @param pos   Sound source position
     * @param gain  Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(Sound sound, Vector3f pos, float gain, int priority) {
        SoundSource source = source(sound, new Vector3d(pos), gain, priority);

        if (source == null) {
            return null;
        }

        return source.setGain(gain).play();
    }

    /**
     * Plays specified sound tuned for specified entity
     *
     * @param sound    Sound object
     * @param entity   Entity sounding
     * @param gain     Sound source gain
     * @param priority Sound priority
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(Sound sound, EntityRef entity, float gain, int priority) {
        Vector3f pos = getEntityPosition(entity);
        if (pos == null) return null;
        
        SoundSource source = source(sound, new Vector3d(pos), gain, priority);

        if (source == null) {
            // Nof free sound sources
            return null;
        }

        return source.setVelocity(new Vector3d(getEntityVelocity(entity))).setDirection(new Vector3d(getEntityDirection(entity))).play();
    }
    
    private static Vector3f getEntityPosition(EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        if (blockComp != null) {
            return blockComp.getPosition().toVector3f();
        }
        return null;
    }

    private static Vector3f getEntityDirection(EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            Quat4f rot = loc.getWorldRotation();
            Vector3f dir = new Vector3f(0,0,-1);
            QuaternionUtil.quatRotate(rot, dir, dir);
            return dir;
        }
        return new Vector3f();
    }
    
    private static Vector3f getEntityVelocity(EntityRef entity) {
        CharacterMovementComponent charMove = entity.getComponent(CharacterMovementComponent.class);
        if (charMove != null) {
            return charMove.getVelocity();
        }
        return new Vector3f();
    }

    /**
     * Plays specified music
     *
     * @param name Music name
     * @return Sound source object, or null if there is no free sound sources in music pool
     */
    public static SoundSource playMusic(String name) {
        SoundPool pool = AudioManager.getInstance().getSoundPool("music");

        pool.stopAll();

        SoundSource source = pool.getSource(AudioManager.sound(name));

        if (source == null) { // no free music slots
            return null;
        }

        return source.setGain(0.1f).play();
    }
}
