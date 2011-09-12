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
package com.github.begla.blockmania.audio;

import com.github.begla.blockmania.main.Game;
import javolution.util.FastMap;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.ResourceLoader;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AudioManager {

    private FastMap<String, Audio> _audioFiles = new FastMap();
    private static AudioManager _instance = null;

    /**
     * Returns (and creates – if necessary) the static instance
     * of this helper class.
     *
     * @return The instance
     */
    public static AudioManager getInstance() {
        if (_instance == null) {
            _instance = new AudioManager();
        }

        return _instance;
    }

    private AudioManager() {
        loadAudioFiles();
    }

    private void loadAudioFiles() {
        try {
            _audioFiles.put("FootGrass1", AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/FootGrass1.ogg")));
            _audioFiles.put("FootGrass2", AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/FootGrass2.ogg")));
            _audioFiles.put("FootGrass3", AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/FootGrass3.ogg")));
            _audioFiles.put("FootGrass4", AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/FootGrass4.ogg")));
            _audioFiles.put("FootGrass5", AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/FootGrass5.ogg")));
            _audioFiles.put("PlaceRemoveBlock", AudioLoader.getAudio("OGG", ResourceLoader.getResourceAsStream("com/github/begla/blockmania/data/sounds/PlaceRemoveBlock.ogg")));
        } catch (IOException e) {
            Game.getInstance().getLogger().log(Level.SEVERE, e.getLocalizedMessage());
        }
    }

    public Audio getAudio(String s) {
        return _audioFiles.get(s);
    }
}
