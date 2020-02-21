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

import org.terasology.math.Side;
import org.terasology.math.geom.Vector2f;
import org.terasology.world.block.shapes.BlockMeshPart;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A block's appearance.
 *
 */
public class BlockAppearance {

    private Map<String, BlockMeshPart> blockParts = new HashMap<>();
    private Map<String, Vector2f> textureAtlasPos = new HashMap<>();
    private Map<Side, List<BlockMeshPart>> blockSides = Maps.newEnumMap(Side.class);

    public BlockAppearance() {
    }

    public BlockAppearance(Map<String, BlockMeshPart> blockParts, Map<Side, List<BlockMeshPart>> blockSide, Map<String, Vector2f> textureAtlasPos) {
        Preconditions.checkNotNull(blockParts);
        Preconditions.checkNotNull(blockSide);
        Preconditions.checkNotNull(textureAtlasPos);
        this.blockParts = blockParts;
        this.textureAtlasPos.putAll(textureAtlasPos);
        this.blockSides = blockSide;
    }

    public BlockMeshPart getPart(String name) {
        return blockParts.get(name);
    }

    public List<BlockMeshPart> getParts(Side side) {
        return blockSides.get(side);
    }

    public Vector2f getTextureAtlasPos(String name) {
        return new Vector2f(textureAtlasPos.get(name));
    }

}
