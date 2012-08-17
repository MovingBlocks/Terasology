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
package org.terasology.logic.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.audio.OpenALManager;
import org.terasology.audio.Sound;
import org.terasology.audio.SoundPool;
import org.terasology.audio.SoundSource;
import org.terasology.world.block.BlockComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.physics.character.CharacterMovementComponent;

import com.bulletphysics.linearmath.QuaternionUtil;

/**
 * Simple managing class for loading and accessing audio files.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author t3hk0d3 <contact@tehkode.ru>
 */
public abstract class AudioManager implements SoundManager {

    protected Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    private static AudioManager _instance = null;

    protected Map<String, Sound> _audio = new HashMap<String, Sound>();

    protected Map<String, SoundPool> _pools = new HashMap<String, SoundPool>();


    protected AudioManager() {
    }

    protected abstract boolean checkDistance(Vector3d soundSource);

    /**
     * Returns sound pool with specified name
     * <b>WARNING! Method will throw IllegalArgumentException if specified sound pool is not found</b>
     *
     * @param pool Sound pool name
     * @return Sound pool object
     */
    @Override
    public SoundPool getSoundPool(String pool) {
        SoundPool soundPool = _pools.get(pool);

        if (soundPool == null) {
            throw new IllegalArgumentException("Unknown pool '" + pool + "', typo? Available pools: " + _pools.keySet());
        }

        return soundPool;
    }

    @Override
    public SoundSource getSoundSource(String pool, Sound sound, int priority) {
        return getSoundPool(pool).getSource(sound, priority);
    }

    @Override
    public SoundSource getSoundSource(String pool, AssetUri soundUri, int priority) {
        Sound sound = (Sound) AssetManager.load(soundUri);
        if (sound != null) {
            return getSoundPool(pool).getSource(sound, priority);
        }
        return null;
    }

    /**
     * Stops all playback.
     */
    @Override
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
     * Returns sound source for specified sound from "sfx" pool with normal priority
     *
     * @param uri Sound uri
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(AssetUri uri) {
        return source(uri, PRIORITY_NORMAL);
    }

    /**
     * Returns sound source for specified sound from "sfx" pool
     *
     * @param uri
     * @param priority Priority
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(AssetUri uri, int priority) {
        return getInstance().getSoundSource("sfx", uri, priority);
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
     * @param uri  Sound uri
     * @param pos  Sound source position
     * @param gain Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource source(AssetUri uri, Vector3d pos, float gain, int priority) {
        SoundSource source = source(uri, priority);
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
     * @param uri Sound uri
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(AssetUri uri) {
        return play(uri, null, 1.0f, PRIORITY_NORMAL);
    }

    /**
     * Plays specified sound with specified gain
     *
     * @param uri  Sound uri
     * @param gain Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(AssetUri uri, float gain) {
        return play(uri, null, gain, PRIORITY_NORMAL);
    }

    /**
     * Plays specified sound at specified position and with specified gain
     *
     * @param uri  Sound uri
     * @param pos  Sound source position
     * @param gain Sound source gain
     * @return Sound source object, or null if there is no free sound sources in effects pool
     */
    public static SoundSource play(AssetUri uri, Vector3d pos, float gain, int priority) {
        SoundSource source = source(uri, pos, gain, priority);

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
        return null;
    }

    private static Vector3f getEntityDirection(EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            Quat4f rot = loc.getWorldRotation();
            Vector3f dir = new Vector3f(0, 0, -1);
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
     * @param uri Music uri
     * @return Sound source object, or null if there is no free sound sources in music pool
     */
    public static SoundSource playMusic(AssetUri uri) {
        SoundPool pool = AudioManager.getInstance().getSoundPool("music");

        pool.stopAll();

        Sound sound = (Sound) AssetManager.load(uri);
        if (sound == null)
            return null;

        SoundSource source = pool.getSource(sound);

        if (source == null) { // no free music slots
            return null;
        }

        return source.setGain(0.1f).play();
    }

    public static SoundSource playMusic(String shortUri) {
        AssetUri uri = new AssetUri(AssetType.MUSIC, shortUri);
        return playMusic(uri);
    }
}
