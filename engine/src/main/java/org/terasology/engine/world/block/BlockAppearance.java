// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.engine.world.block.shapes.BlockMeshPart;

import java.util.EnumMap;
import java.util.Map;

/**
 * A block's appearance.
 *
 */
public class BlockAppearance {

    private Map<BlockPart, BlockMeshPart> blockParts;
    private Map<BlockPart, Vector2fc> textureAtlasPos = new EnumMap<>(BlockPart.class);

    public BlockAppearance() {
        blockParts = Maps.newEnumMap(BlockPart.class);
        textureAtlasPos = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            textureAtlasPos.put(part, new Vector2f());
        }
    }

    public BlockAppearance(Map<BlockPart, BlockMeshPart> blockParts, Map<BlockPart, ? extends Vector2fc> textureAtlasPos) {
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

    public Vector2fc getTextureAtlasPos(BlockPart part) {
        return new Vector2f(textureAtlasPos.get(part));
    }
}
