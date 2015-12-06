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
package org.terasology.rendering.md5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.collada.ColladaLoader;
import org.terasology.rendering.collada.ColladaParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Importer for Collada data exchange model files.  Supports skeletal mesh data
 * <p>
 * The development of this loader was greatly influenced by
 * http://www.wazim.com/Collada_Tutorial_1.htm
 *
 */
@RegisterAssetFileFormat
public class ColladaSkeletalMeshLoader extends AbstractAssetFileFormat<SkeletalMeshData> {

    private static final Logger logger = LoggerFactory.getLogger(ColladaSkeletalMeshLoader.class);

    public ColladaSkeletalMeshLoader() {
        super("dae");
    }

    @Override
    public SkeletalMeshData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        logger.info("Loading skeletal mesh for " + urn);


        try (InputStream stream = inputs.get(0).openStream()) {
            ColladaLoader loader = new ColladaLoader();
            return loader.parseSkeletalMeshData(stream);
        } catch (ColladaParseException e) {
            throw new IOException("Unable to load skeletal mesh for " + urn, e);
        }
    }
}
