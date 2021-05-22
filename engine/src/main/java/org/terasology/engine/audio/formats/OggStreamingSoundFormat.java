// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.formats;

import org.terasology.engine.audio.StreamingSoundData;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

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
