// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.utilities.gson.Vector2iTypeAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RegisterAssetFileFormat
public class AtlasFormat extends AbstractAssetFileFormat<AtlasData> {
    public static final float BORDER_SIZE = 0.0001f;

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

    private void process(FreeformDefinition freeform, Texture texture, Vector2ic size, Map<Name, SubtextureData> out) {
        String freeformName = freeform.getName();
        if (freeformName == null || freeformName.isEmpty()) {
            logger.error("Bad subimage definition - missing mandatory property name");
            return;
        }
        if (freeform.getMin() == null) {
            logger.error("Bad subimage definition '{}' - missing mandatory property min", freeformName);
            return;
        }
        if (freeform.getSize() == null && freeform.getMax() == null) {
            logger.error("Bad subimage definition '{}' - requires one of max or size", freeformName);
            return;
        }
        Vector2f min = new Vector2f((float) freeform.getMin().x / size.x(), (float) freeform.getMin().y / size.y());
        if (freeform.getSize() != null) {
            Vector2f itemSize = new Vector2f((float) freeform.getSize().x / size.x(),
                (float) freeform.getSize().y / size.y());

            out.put(new Name(freeformName), new SubtextureData(texture,
                shrinkRegion(new Rectanglef(min, min).setSize(itemSize))));
        } else if (freeform.getMax() != null) {
            Vector2f max = new Vector2f((float) freeform.getMax().x / size.x(), (float) freeform.getMax().y / size.y());
            out.put(new Name(freeformName), new SubtextureData(texture, shrinkRegion(new Rectanglef(min, max))));
        }
    }

    private void process(GridDefinition grid, Texture texture, Vector2ic size, Map<Name, SubtextureData> out) {
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
            offset.set((float) grid.getGridOffset().x / size.x(), (float) grid.getGridOffset().y / size.y());
        }
        Vector2f tileSize = new Vector2f((float) grid.getTileSize().x / size.x(),
            (float) grid.getTileSize().y / size.y());
        int tileX = 0;
        int tileY = 0;
        for (String name : grid.getTileNames()) {
            if (!name.isEmpty()) {
                Vector2f pos = new Vector2f(offset);
                pos.x += tileX * tileSize.x;
                pos.y += tileY * tileSize.y;
                Rectanglef tileLocation = new Rectanglef(offset.x + tileX * tileSize.x, offset.y + tileY * tileSize.y,
                        0.0f, 0.0f).setSize(tileSize.x, tileSize.y);
                out.put(new Name(name), new SubtextureData(texture, shrinkRegion(tileLocation)));
            }

            tileX++;
            if (tileX == grid.getGridDimensions().x) {
                tileX = 0;
                tileY++;
            }
        }
    }

    /**
     * Make the region slightly smaller to make sure the adjacent pixels don't leak in.
     */
    private Rectanglef shrinkRegion(Rectanglef region) {
        float x = region.minX() + region.getSizeX() * BORDER_SIZE;
        float y = region.minY() + region.getSizeY() * BORDER_SIZE;
        float sizeX = region.getSizeX() * (1 - 2 * BORDER_SIZE);
        float sizeY = region.getSizeY() * (1 - 2 * BORDER_SIZE);
        return region.setMin(x, y).setSize(sizeX, sizeY);
    }
}
