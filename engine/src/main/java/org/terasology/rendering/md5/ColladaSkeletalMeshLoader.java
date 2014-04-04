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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.engine.module.Module;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.collada.ColladaLoader;

/**
 * Importer for Collada data exchange model files.  Supports skeletal mesh data
 * 
 * The development of this loader was greatly influenced by 
 * http://www.wazim.com/Collada_Tutorial_1.htm
 *
 * @author mkienenb@gmail.com
 */

public class ColladaSkeletalMeshLoader extends ColladaLoader implements AssetLoader<SkeletalMeshData> {

    private static final Logger logger = LoggerFactory.getLogger(ColladaSkeletalMeshLoader.class);

    @Override
    public SkeletalMeshData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        logger.info("Loading skeletal mesh for " + urls);

        try {
            parseSkeletalMeshData(stream);
        } catch (ColladaParseException e) {
            logger.error("Unable to load skeletal mesh for " + urls, e);
            return null;
        }

        SkeletalMeshData skeletalMesh = skeletonBuilder.build();

        return skeletalMesh;
    }

}
