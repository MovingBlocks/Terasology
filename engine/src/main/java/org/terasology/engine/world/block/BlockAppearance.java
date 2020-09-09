// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.terasology.engine.world.block.shapes.BlockMeshPart;
import org.terasology.math.geom.Vector2f;

import java.util.EnumMap;
import java.util.Map;

/**
 * A block's appearance.
 */
public class BlockAppearance {

    private final Map<BlockPart, BlockMeshPart> blockParts;
    private Map<BlockPart, Vector2f> textureAtlasPos = new EnumMap<>(BlockPart.class);

    public BlockAppearance() {
        blockParts = Maps.newEnumMap(BlockPart.class);
        textureAtlasPos = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            textureAtlasPos.put(part, new Vector2f());
        }
    }

    public BlockAppearance(Map<BlockPart, BlockMeshPart> blockParts, Map<BlockPart, Vector2f> textureAtlasPos) {
        Preconditions.checkNotNull(blockParts);
        Preconditions.checkNotNull(textureAtlasPos);
        this.blockParts = blockParts;
        this.textureAtlasPos.putAll(textureAtlasPos);
        for (BlockPart part : BlockPart.values()) {
            Preconditions.checkNotNull(textureAtlasPos.get(part), "Missing texture atlas position for part " + part);
        }
    }

    public BlockMeshPart getPart(BlockPart part) {
        return blockParts.get(part);
    }

    public Vector2f getTextureAtlasPos(BlockPart part) {
        return new Vector2f(textureAtlasPos.get(part));
    }

}
