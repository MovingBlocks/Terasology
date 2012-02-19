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

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;
import org.terasology.game.Terasology;
import org.terasology.math.TeraMath;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Simple managing class for loading and accessing audio files.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AudioManager {

    private final HashMap<String, Audio> _audioFiles = new HashMap<String, Audio>();
    private static AudioManager _instance = null;
    private static final FastRandom _rand = new FastRandom();

    public static AudioManager getInstance() {
        if (_instance == null) {
            _instance = new AudioManager();
        }

        return _instance;
    }

    private AudioManager() {
        loadSound();
        loadMusic();
    }

    private void loadSound() {
        _audioFiles.put("PlaceBlock", loadSound("PlaceBlock"));
        _audioFiles.put("RemoveBlock", loadSound("RemoveBlock"));
        _audioFiles.put("Dig", loadSound("Dig"));

        _audioFiles.put("Slime1", loadSound("Slime1"));
        _audioFiles.put("Slime2", loadSound("Slime2"));
        _audioFiles.put("Slime3", loadSound("Slime3"));
        _audioFiles.put("Slime4", loadSound("Slime4"));
        _audioFiles.put("Slime5", loadSound("Slime5"));
        _audioFiles.put("Slime6", loadSound("Slime6"));

        _audioFiles.put("Explode1", loadSound("Explode1"));
        _audioFiles.put("Explode2", loadSound("Explode2"));
        _audioFiles.put("Explode3", loadSound("Explode3"));
        _audioFiles.put("Explode4", loadSound("Explode4"));
        _audioFiles.put("Explode5", loadSound("Explode5"));
    }

    private void loadMusic() {
        _audioFiles.put("Sunrise", loadMusic("Sunrise"));
        _audioFiles.put("Afternoon", loadMusic("Afternoon"));
        _audioFiles.put("Sunset", loadMusic("Sunset"));
        _audioFiles.put("Dimlight", loadMusic("Dimlight"));
    }

    /**
     * Return an audio file, loading it from disk if it isn't in the cache yet
     *
     * @param s The name of the audio file
     * @return The loaded audio file
     */
    public Audio loadSound(String s) {
        try {
            Audio a = _audioFiles.get(s);
            if (a == null) {
                a = AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("org/terasology/data/sounds/" + s + ".ogg"));
                _audioFiles.put(s, a);
            }
            return a;
        } catch (IOException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.getLocalizedMessage());
        }

        return null;
    }

    public void playVaryingSound(String s, float freq, float amp) {
        AudioManager.getInstance().getAudio(s).playAsSoundEffect(freq + (float) TeraMath.fastAbs(_rand.randomDouble()) + (freq * 0.10f), amp + (float) TeraMath.fastAbs(_rand.randomDouble()) * (amp * 0.10f), false);
    }

    public void playVaryingPositionedSound(Vector3d relativeEntityPosition, Audio sound) {
        double distance = relativeEntityPosition.length();

        // No sounds so far away!
        if (distance > 64.0)
            return;


        float loudness = 0.05f + (float) TeraMath.fastAbs(_rand.randomDouble()) * 0.05f;

        if (distance > 1.0) {
            loudness /= distance;
            relativeEntityPosition.normalize();
        }

        sound.playAsSoundEffect(0.9f +
                (float) TeraMath.fastAbs(_rand.randomDouble()) * 0.1f,
                loudness,
                false,
                (float) relativeEntityPosition.x,
                (float) relativeEntityPosition.y,
                (float) relativeEntityPosition.z);
    }

    /**
     * Loads the music file with the given name.
     *
     * @param s The name of the music file
     * @return The loaded music file
     */
    public Audio loadMusic(String s) {
        try {
            return AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("org/terasology/data/music/" + s + ".ogg"));
        } catch (IOException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Stops all playback.
     */
    public void stopAllSounds() {
        for (Audio a : _audioFiles.values()) {
            a.stop();
        }
    }

    /**
     * Returns the loaded audio file with the given name.
     *
     * @param s The name of the audio file
     * @return The loaded audio file
     */
    public Audio getAudio(String s) {
        return _audioFiles.get(s);
    }
}
