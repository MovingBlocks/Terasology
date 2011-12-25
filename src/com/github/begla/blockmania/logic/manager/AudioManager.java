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
package com.github.begla.blockmania.logic.manager;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.MathHelper;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

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
    }

    private void loadMusic() {
        _audioFiles.put("Sunrise", loadMusic("Sunrise"));
        _audioFiles.put("Afternoon", loadMusic("Afternoon"));
        _audioFiles.put("Sunset", loadMusic("Sunset"));
    }

    /**
     * Loads the sound file with the given name.
     *
     * @param s The name of the audio file
     * @return The loaded audio file
     */
    public Audio loadSound(String s) {
        try {
            return AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/" + s + ".ogg"));
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.getLocalizedMessage());
        }

        return null;
    }

    public void playVaryingSound(String s, float freq, float amp) {
        AudioManager.getInstance().getAudio(s).playAsSoundEffect(freq + (float) MathHelper.fastAbs(_rand.randomDouble()) + (freq * 0.10f), amp + (float) MathHelper.fastAbs(_rand.randomDouble()) * (amp * 0.10f), false);
    }

    /**
     * Loads the music file with the given name.
     *
     * @param s The name of the music file
     * @return The loaded music file
     */
    public Audio loadMusic(String s) {
        try {
            return AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/music/" + s + ".ogg"));
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.getLocalizedMessage());
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
