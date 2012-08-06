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
package org.terasology.model.shapes;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.AssetManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to block shapes by their title
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockShapeManager {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final char SEPARATOR_CHAR = ':';
    private static final String DEFAULT_PACKAGE = "engine:";

    /* SINGLETON */
    private static BlockShapeManager instance;

    /* BLOCKS */
    private final HashMap<String, BlockShape> blockShapeByTitle = new HashMap<String, BlockShape>(128);

    public static BlockShapeManager getInstance() {
        if (instance == null)
            instance = new BlockShapeManager();
        return instance;
    }

    private BlockShapeManager() {
    }

    public void reload() {
        JsonBlockShapePersister persister = new JsonBlockShapePersister();
        blockShapeByTitle.clear();
        for (AssetUri shapeUri : AssetManager.getInstance().listAssets(AssetType.SHAPE)) {
            try {
                logger.log(Level.FINE, "Loading " + shapeUri.toString());
                BlockShape shape = persister.load(shapeUri.getPackage() + SEPARATOR_CHAR + shapeUri.getAssetName(), AssetManager.assetStream(shapeUri));
                blockShapeByTitle.put(shape.getTitle().toLowerCase(Locale.ENGLISH), shape);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to load " + shapeUri.toString(), ioe);
            }
        }
    }

    public BlockShape getBlockShape(String title) {
        if (title.indexOf(SEPARATOR_CHAR) == -1) {
            return blockShapeByTitle.get(DEFAULT_PACKAGE + title.toLowerCase(Locale.ENGLISH));
        }
        return blockShapeByTitle.get(title.toLowerCase(Locale.ENGLISH));
    }
}
