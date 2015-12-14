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

package org.terasology.audio.formats;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.audio.StreamingSoundData;

import java.io.IOException;
import java.util.List;

/**
 */
@RegisterAssetFileFormat
public class OggStreamingSoundFormat extends AbstractAssetFileFormat<StreamingSoundData> {

    public OggStreamingSoundFormat() {
        super("ogg");
    }

    @Override
    public StreamingSoundData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        return new OggStreamingSoundData(inputs.get(0));
    }
}
