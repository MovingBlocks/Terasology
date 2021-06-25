// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import com.google.common.base.Preconditions;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.engine.world.block.shapes.BlockMeshPart;

import java.util.Map;

/**
 * A block's appearance.
 *
 */
public class BlockAppearance {
    private final BlockMeshInfo[] meshInfo = new BlockMeshInfo[BlockPart.values().length];

    public BlockAppearance() {
        for (int x = 0; x < meshInfo.length; x++) {
            meshInfo[x] = new BlockMeshInfo();
            meshInfo[x].textureAtlasPosition = new Vector2f();
        }
    }

    public BlockAppearance(Map<BlockPart, BlockMeshPart> blockParts, Map<BlockPart, ? extends Vector2fc> textureAtlasPos) {
        Preconditions.checkNotNull(blockParts);
        Preconditions.checkNotNull(textureAtlasPos);
        for (BlockPart part : BlockPart.values()) {
            Preconditions.checkNotNull(textureAtlasPos.get(part), "Missing texture atlas position for part " + part);
        }

        for (BlockPart part : BlockPart.values()) {
            meshInfo[part.ordinal()] = new BlockMeshInfo();
            meshInfo[part.ordinal()].textureAtlasPosition = textureAtlasPos.get(part);
            if (blockParts.containsKey(part)) {
                meshInfo[part.ordinal()].part = blockParts.get(part);
            }
        }
    }

    public BlockMeshPart getPart(BlockPart part) {
        BlockMeshInfo info = meshInfo[part.ordinal()];
        if (info == null) {
            return null;
        }
        return info.part;
    }

    public Vector2fc getTextureAtlasPos(BlockPart part) {
        BlockMeshInfo info = meshInfo[part.ordinal()];
        if (info == null) {
            return null;
        }
        return info.textureAtlasPosition;
    }

    private static class BlockMeshInfo {
        BlockMeshPart part;
        Vector2fc textureAtlasPosition;
    }

}
