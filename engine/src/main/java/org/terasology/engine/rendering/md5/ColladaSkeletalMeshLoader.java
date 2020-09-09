// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.md5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.engine.rendering.collada.ColladaLoader;
import org.terasology.engine.rendering.collada.ColladaParseException;

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
