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
package org.terasology.world.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.terasology.math.geom.Vector2f;
import org.terasology.world.block.shapes.BlockMeshPart;

import java.util.EnumMap;
import java.util.Map;

/**
 * A block's appearance.
 *
 */
public class BlockAppearance {

    private Map<BlockPart, BlockMeshPart> blockParts;
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
            Preconditions.checkNotNull("Missing texture atlas position for part " + part, textureAtlasPos.get(part));
        }
    }

    public BlockMeshPart getPart(BlockPart part) {
        return blockParts.get(part);
    }

    public Vector2f getTextureAtlasPos(BlockPart part) {
        return new Vector2f(textureAtlasPos.get(part));
    }

}
