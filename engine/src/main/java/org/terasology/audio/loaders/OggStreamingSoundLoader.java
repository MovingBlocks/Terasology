/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.asset.AssetLoader;
import org.terasology.audio.StreamingSoundData;
import org.terasology.module.Module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * @author Immortius
 */
public class OggStreamingSoundLoader implements AssetLoader<StreamingSoundData> {
    @Override
    public StreamingSoundData load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException {
        return new OggStreamingSoundData(urls.get(0));
    }
}
