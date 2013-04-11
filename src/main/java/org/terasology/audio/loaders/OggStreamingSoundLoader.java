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

package org.terasology.audio.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.audio.AudioManager;
import org.terasology.audio.openAL.OggStreamingSound;
import org.terasology.audio.Sound;
import org.terasology.game.CoreRegistry;

/**
 * @author Immortius
 */
public class OggStreamingSoundLoader implements AssetLoader<Sound> {

    @Override
    public Sound load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        // TODO: Use a different sound loader rather than hacking in a check here
        AudioManager audioManager = CoreRegistry.get(AudioManager.class);
        return audioManager.loadStreamingSound(uri, urls);
    }
}
