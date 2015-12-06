/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.atlas;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.naming.Name;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.utilities.gson.Vector2iTypeAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 */
@RegisterAssetFileFormat
public class AtlasFormat extends AbstractAssetFileFormat<AtlasData> {

    private static final Logger logger = LoggerFactory.getLogger(AtlasFormat.class);

    private AssetManager assetManager;
    private Gson gson;

    public AtlasFormat(AssetManager assetManager) {
        super("atlas");
        this.assetManager = assetManager;
        gson = new GsonBuilder().registerTypeAdapter(Vector2i.class, new Vector2iTypeAdapter()).create();
    }

    @Override
    public AtlasData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (Reader reader = new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8)) {
            AtlasDefinition def = gson.fromJson(reader, AtlasDefinition.class);
            Optional<? extends Texture> texture = assetManager.getAsset(def.getTexture(), Texture.class);

            if (texture.isPresent()) {
                Vector2i size = def.getTextureSize();
                if (size == null) {
                    size = new Vector2i(texture.get().getWidth(), texture.get().getHeight());
                }
                Map<Name, SubtextureData> result = Maps.newHashMap();
                if (def.getGrid() != null) {
                    process(def.getGrid(), texture.get(), size, result);
                }
                if (def.getGrids() != null) {
                    for (GridDefinition grid : def.getGrids()) {
                        process(grid, texture.get(), size, result);
                    }
                }
                if (def.getSubimage() != null) {
                    process(def.getSubimage(), texture.get(), size, result);
                }
                if (def.getSubimages() != null) {
                    for (FreeformDefinition freeform : def.getSubimages()) {
                        process(freeform, texture.get(), size, result);
                    }
                }
                return new AtlasData(result);
            }
            return null;
        }

    }

    private void process(FreeformDefinition freeform, Texture texture, Vector2i size, Map<Name, SubtextureData> out) {
        if (freeform.getName() == null || freeform.getName().isEmpty()) {
            logger.error("Bad subimage definition - missing mandatory property name");
            return;
        }
        if (freeform.getMin() == null) {
            logger.error("Bad subimage definition '{}' - missing mandatory property min", freeform.getName());
            return;
        }
        if (freeform.getSize() == null && freeform.getMax() == null) {
            logger.error("Bad subimage definition '{}' - requires one of max or size", freeform.getName());
            return;
        }
        Vector2f min = new Vector2f((float) freeform.getMin().x / size.x, (float) freeform.getMin().y / size.y);
        if (freeform.getSize() != null) {
            Vector2f itemSize = new Vector2f((float) freeform.getSize().x / size.x, (float) freeform.getSize().y / size.y);
            out.put(new Name(freeform.getName()), new SubtextureData(texture, Rect2f.createFromMinAndSize(min, itemSize)));
        } else if (freeform.getMax() != null) {
            Vector2f max = new Vector2f((float) freeform.getMax().x / size.x, (float) freeform.getMax().y / size.y);
            out.put(new Name(freeform.getName()), new SubtextureData(texture, Rect2f.createFromMinAndMax(min, max)));
        }
    }

    private void process(GridDefinition grid, Texture texture, Vector2i size, Map<Name, SubtextureData> out) {
        if (grid.getTileSize() == null) {
            logger.error("Bad grid definition - missing mandatory property tileSize");
            return;
        }
        if (grid.getGridDimensions() == null) {
            logger.error("Bad grid definition - missing mandatory property gridDimensions");
            return;
        }

        Vector2f offset = new Vector2f(0, 0);
        if (grid.getGridOffset() != null) {
            offset.set((float) grid.getGridOffset().x / size.x, (float) grid.getGridOffset().y / size.y);
        }
        Vector2f tileSize = new Vector2f((float) grid.getTileSize().x / size.x, (float) grid.getTileSize().y / size.y);
        int tileX = 0;
        int tileY = 0;
        for (String name : grid.getTileNames()) {
            if (!name.isEmpty()) {
                Vector2f pos = new Vector2f(offset);
                pos.x += tileX * tileSize.x;
                pos.y += tileY * tileSize.y;
                Rect2f tileLocation = Rect2f.createFromMinAndSize(offset.x + tileX * tileSize.x, offset.y + tileY * tileSize.y, tileSize.x, tileSize.y);
                out.put(new Name(name), new SubtextureData(texture, tileLocation));
            }

            tileX++;
            if (tileX == grid.getGridDimensions().x) {
                tileX = 0;
                tileY++;
            }
        }
    }

}
