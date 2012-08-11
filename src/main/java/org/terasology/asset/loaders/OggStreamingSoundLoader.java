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

package org.terasology.asset.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.audio.OggStreamingSound;
import org.terasology.audio.Sound;

/**
 * @author Immortius
 */
public class OggStreamingSoundLoader implements AssetLoader<Sound> {
    @Override
    public Sound load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException {
        return new OggStreamingSound(uri, urls.get(0));
    }
}
